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

import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class RagConfig {
    @Resource
    EmbeddingFactory embeddingFactory;

    @Resource
    QueryRewriter queryRewriter;

    // 1. 定义检索器：告诉系统怎么查数据
    @Bean("contentRetriever")
    public ContentRetriever contentRetriever() {
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
}
