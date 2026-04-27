package org.zhouxh.ragdemo.data.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zhouxh.ragdemo.data.enums.ParseType;
import org.zhouxh.ragdemo.data.service.DocumentParseService;
import org.zhouxh.ragdemo.data.util.StandardHierarchicalSplitter;
import java.io.InputStream;
import java.util.List;

/**
 * text 文档解析实现类
 */
@Service
public class TextDocumentParseServiceImpl implements DocumentParseService<ParseType> {

    private static final Logger logger = LoggerFactory.getLogger(TextDocumentParseServiceImpl.class);

    // 官方原生文本解析器，完全不修改
    private static final TextDocumentParser PARSER = new TextDocumentParser();
    // 清洗后调用官方分词器
    private static final StandardHierarchicalSplitter SPLITTER = new StandardHierarchicalSplitter();


    /**
     * 解析文本文件
     * @param doc
     * @return
     */
    @Override
    public Document parseDocument(InputStream doc) {
        logger.debug("Parsing text document");
        Document document = PARSER.parse(doc);
        // 解析过的文件后续保存到数据库，同名文件避免重复解析，通过文件指纹判断是否已经解析过了
        logger.info("Document parsed successfully");
        return document;
    }

    /**
     * 清洗文本、分块文本
     * @param document
     * @return
     */
    @Override
    public List<TextSegment> splitDocument(Document document) {
        // 直接调用官方分块器，100% 原生行为
        logger.debug("Splitting document into segments");
        List<TextSegment> segments = SPLITTER.split(document);
        logger.info("Document split into {} segments", segments.size());
        return segments;
    }

    @Override
    public ParseType getType() {
        return ParseType.TEXT;
    }
}