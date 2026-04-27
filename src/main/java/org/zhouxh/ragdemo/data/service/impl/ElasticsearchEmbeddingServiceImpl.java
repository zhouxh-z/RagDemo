package org.zhouxh.ragdemo.data.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.zhouxh.ragdemo.data.entity.RegDocument;
import org.zhouxh.ragdemo.data.enums.EmbeddingType;
import org.zhouxh.ragdemo.data.service.EmbeddingService;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchEmbeddingServiceImpl implements EmbeddingService<EmbeddingType> {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchEmbeddingServiceImpl.class);

    @Resource(name = "esEmbeddingStore")
    private EmbeddingStore<TextSegment> embeddingStore;

    @Resource(name = "embeddingModel")
    private EmbeddingModel embeddingModel;

    @Resource(name = "embeddingCache")
    private Cache<String, String> embeddingCache;

    @Resource(name = "searchCache")
    private Cache<String, String> searchCache;

    /**
     * 向量化 - 写入向量化数据库
     * @param segments
     * @return
     */
    @Override
    public void embedded(List<TextSegment> segments) {
        logger.info("Starting embedding for {} segments", segments.size());

        List<Embedding> embeddings = new ArrayList<>();
        List<TextSegment> segmentsToEmbed = new ArrayList<>();

        for (TextSegment segment : segments) {
            String text = segment.text();
            String cachedEmbedding = embeddingCache.getIfPresent(text);
            if (cachedEmbedding != null) {
                logger.debug("Using cached embedding for segment: {}", text.substring(0, Math.min(50, text.length())));
                // 解析缓存的向量（假设以逗号分隔的浮点数）
                String[] values = cachedEmbedding.split(",");
                float[] vector = new float[values.length];
                for (int i = 0; i < values.length; i++) {
                    vector[i] = Float.parseFloat(values[i]);
                }
                embeddings.add(Embedding.from(vector));
            } else {
                segmentsToEmbed.add(segment);
            }
        }

        // 计算未缓存的嵌入
        if (!segmentsToEmbed.isEmpty()) {
            logger.debug("Computing embeddings for {} uncached segments", segmentsToEmbed.size());
            Response<List<Embedding>> listResponse = embeddingModel.embedAll(segmentsToEmbed);
            List<Embedding> newEmbeddings = listResponse.content();
            embeddings.addAll(newEmbeddings);

            // 缓存新计算的嵌入
            for (int i = 0; i < segmentsToEmbed.size(); i++) {
                TextSegment segment = segmentsToEmbed.get(i);
                Embedding embedding = newEmbeddings.get(i);
                // 将向量转换为字符串存储
                String vectorStr = embedding.vectorAsList().stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> a + "," + b)
                        .orElse("");
                embeddingCache.put(segment.text(), vectorStr);
            }
        }

        // 保存到向量存储
        List<String> vectorIds = embeddingStore.addAll(embeddings, segments);
        logger.info("Embedded {} segments, generated {} vector IDs", segments.size(), vectorIds.size());
    }

    /**
     * 从向量数据库查询
     * @param query
     * @return
     */
    @Override
    public List<String> search(String query) {
        logger.debug("Searching embeddings for query: {}", query);
        // 检查缓存
        String cachedResult = searchCache.getIfPresent(query);
        if (cachedResult != null) {
            logger.info("Returning cached search result for query: {}", query);
            return List.of(cachedResult.split("\\|")); // 假设缓存以 | 分隔
        }

        List<String> ans = new ArrayList<>();
        Response<Embedding> embed = embeddingModel.embed(query);
        EmbeddingSearchRequest embeddingSearchRequest =
                EmbeddingSearchRequest
                        .builder()
                        .queryEmbedding(embed.content())
                        .maxResults(10)
                        .minScore(0.8)
                        .build();

        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(embeddingSearchRequest);
        search.matches().forEach(o -> {
            TextSegment embedded = o.embedded();
            ans.add(embedded.text());
        });
        logger.info("Search completed, found {} matches", ans.size());

        // 缓存结果
        searchCache.put(query, String.join("|", ans));
        return ans;
    }

    @Override
    public List<EmbeddingMatch<TextSegment>> search(Query query) {
        logger.debug("Searching embeddings for query object: {}", query.text());
        Response<Embedding> embed = embeddingModel.embed(query.text());
        EmbeddingSearchRequest embeddingSearchRequest =
                EmbeddingSearchRequest
                        .builder()
                        .queryEmbedding(embed.content())
                        .maxResults(10)
                        .minScore(0.8)
                        .build();

        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(embeddingSearchRequest);
        logger.info("Query search completed, found {} matches", search.matches().size());
        return search.matches();
    }

    @Override
    public EmbeddingType getType() {
        return EmbeddingType.ELASTICSEARCH;
    }


    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<EmbeddingMatch<TextSegment>> contentSearch(String query){
        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(q -> q.match(m -> m
                        .field("text")
                        .query(query)
                )).build();

        SearchHits<RegDocument> search = elasticsearchTemplate.search(nativeQuery, RegDocument.class);
        return toMatches(search);
    }

    private List<EmbeddingMatch<TextSegment>> toMatches(SearchHits<RegDocument> search) {

        return search.map(document -> new EmbeddingMatch<>(
                (double) document.getScore(),
                document.getId(),
                new Embedding(document.getContent().getVector()),
                TextSegment.from(document.getContent().getText(), new Metadata(document.getContent().getMetadata()))
        )).toList();
    }
}
