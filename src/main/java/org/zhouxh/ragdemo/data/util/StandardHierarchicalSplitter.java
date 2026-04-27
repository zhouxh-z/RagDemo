package org.zhouxh.ragdemo.data.util;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 纯官方原生 Hierarchical 递归分块
 * 仅做【安全空白清洗】，绝不破坏段落结构
 */
public class StandardHierarchicalSplitter implements DocumentSplitter {

    private static final StandardHierarchicalSplitter INSTANCE  = new StandardHierarchicalSplitter();

    // 生产级标准参数（字符级别）
    private static final int MAX_CHUNK_SIZE = 800;
    private static final int OVERLAP_SIZE = 100;

    // 官方原生分块器，完全不修改
    private final DocumentSplitter delegate = DocumentSplitters.recursive(MAX_CHUNK_SIZE, OVERLAP_SIZE);

    public static List<TextSegment> splits(Document document)  {
        return INSTANCE.split(document);
    }


    @Override
    public List<TextSegment> split(Document document) {
        if (document == null || StringUtils.isBlank(document.text())) {
            return List.of();
        }

        // 【安全操作】仅清洗多余空白，不修改段落、不删换行
        String cleanedText = cleanTextSafely(document.text());

        // 完全交给官方分块，100% 原生行为
        return delegate.split(Document.from(cleanedText, document.metadata()));
    }

    /**
     * 安全清洗：只清理 多余空格、多余空行
     */
    private String cleanTextSafely(String text) {
        if (text == null) return "";

        return text
                .replaceAll("[ \\t\\u00A0]+", " ")  // 清理多余空格/制表符
                .replaceAll("\\n{3,}", "\n\n")     // 保留段落分隔，不破坏
                .trim();
    }
}