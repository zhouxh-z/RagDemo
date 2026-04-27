package org.zhouxh.ragdemo.data.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingMatch;

import java.util.List;

/**
 * 向量化接口
 */
public interface EmbeddingService<T extends Enum<T>> {
    void embedded(List<TextSegment> segments);
    List<String> search(String query);
    List<EmbeddingMatch<TextSegment>> search(Query query);
    T getType();
}
