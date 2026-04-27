package org.zhouxh.ragdemo.chat.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zhouxh.ragdemo.chat.service.QueryRewriter;
import org.zhouxh.ragdemo.data.enums.EmbeddingType;
import org.zhouxh.ragdemo.data.factory.EmbeddingFactory;
import org.zhouxh.ragdemo.data.service.EmbeddingService;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class RagConfig {
    @Resource
    EmbeddingFactory embeddingFactory;

    @Resource
    QueryRewriter queryRewriter;

    // 1. 定义检索器：告诉系统怎么查数据
    @Bean(name = "embeddingRetriever")
    public ContentRetriever embeddingRetriever() {
        return query -> {
            // 改写查询以提高准确性
            String rewrittenQueryText = queryRewriter.rewriteQuery(query.text());
            Query rewrittenQuery = Query.from(rewrittenQueryText);

            EmbeddingService<EmbeddingType> embeddingService = embeddingFactory.getEmbeddingService(EmbeddingType.ELASTICSEARCH);

            List<EmbeddingMatch<TextSegment>> search = embeddingService.search(rewrittenQuery);

            return search.stream()
                    .map(
                            embeddingMatch ->
                                    Content.from(embeddingMatch.embedded()))
                    .collect(Collectors.toList());
        };
    }
    // 1. 定义检索器：告诉系统怎么查数据
    @Bean(name = "contentAndEmbeddingRetriever")
    public ContentRetriever contentAndEmbeddingRetriever() {
        return new ContentRetriever(){
            // 官方推荐的系数值
            private static final int K = 60;
            // 返回结果多少条
            private static final int limit = 10;
            @Override
            public List<Content> retrieve(Query query) {
                // 改写查询以提高准确性
                String rewrittenQueryText = queryRewriter.rewriteQuery(query.text());
                Query rewrittenQuery = Query.from(rewrittenQueryText);

                EmbeddingService<EmbeddingType> embeddingService = embeddingFactory.getEmbeddingService(EmbeddingType.ELASTICSEARCH);

                // 向量检索
                List<EmbeddingMatch<TextSegment>> embeddingSearch = embeddingService.search(rewrittenQuery);
                // 关键字检索
                List<EmbeddingMatch<TextSegment>> textSegments = embeddingService.contentSearch(rewrittenQueryText);
                // 做RRF
                List<Content> rrf = rrf(embeddingSearch, textSegments, limit);
                // todo 再做 重排序

                return rrf;
            }

            private List<Content> rrf(List<EmbeddingMatch<TextSegment>> embeddingSearch, List<EmbeddingMatch<TextSegment>> textSegments,int limit) {
                Map<String, Double> embeddingMap = new HashMap<>();
                // 1. 将两种检索结果合并，去重
                Map<String, Content> segmentMap = new HashMap<>();
                embeddingSearch.forEach(embeddingMatch -> segmentMap.put(embeddingMatch.embeddingId(),Content.from(embeddingMatch.embedded())));
                textSegments.forEach(embeddingMatch -> segmentMap.put(embeddingMatch.embeddingId(),Content.from(embeddingMatch.embedded())));
                // 2. 根据RRF算法重新排序
                for (int i = 0; i < embeddingSearch.size(); i++) {
                    String s = embeddingSearch.get(i).embeddingId();
                    embeddingMap.put(s,1.0 / (K + i + 1.0));
                }

                for (int i = 0; i < textSegments.size(); i++) {
                    String s = textSegments.get(i).embeddingId();
                    if (embeddingMap.containsKey(s)) {
                        Double v = embeddingMap.get(s);
                        embeddingMap.put(s,v +  1.0 / (K + i + 1.0));
                    }
                }
                // 3. 返回最终的Content列表
                return embeddingMap.entrySet().stream()
                        // 1. 降序排序（分数高的在前）
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        // 2. 只取前 10 条
                        .limit(limit)
                        // 3. 根据你的 docId (entry.getKey()) 去原始的文档库里获取对应的 TextSegment
                        // 这里假设你有一个 allDocuments Map 用于根据 ID 查找文档
                        .map(entry -> segmentMap.get(entry.getKey()))
                        // 4. 过滤掉可能为 null 的数据（防御性编程）
                        .filter(Objects::nonNull)
                        // 5. 收集成 List
                        .collect(Collectors.toList());
            }
        };
    }

}
