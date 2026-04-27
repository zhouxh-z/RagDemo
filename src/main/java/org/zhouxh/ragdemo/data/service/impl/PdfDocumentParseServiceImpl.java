package org.zhouxh.ragdemo.data.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
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
 * PDF 文档解析实现类
 */
@Service
public class PdfDocumentParseServiceImpl implements DocumentParseService<ParseType> {

    private static final Logger logger = LoggerFactory.getLogger(PdfDocumentParseServiceImpl.class);

    // 使用 Apache PDFBox 解析器
    private static final ApachePdfBoxDocumentParser PARSER = new ApachePdfBoxDocumentParser();
    // 清洗后调用官方分词器
    private static final StandardHierarchicalSplitter SPLITTER = new StandardHierarchicalSplitter();

    /**
     * 解析 PDF 文件
     * @param doc 输入流
     * @return 文档对象
     */
    @Override
    public Document parseDocument(InputStream doc) {
        logger.debug("Parsing PDF document");
        Document document = PARSER.parse(doc);
        logger.info("PDF document parsed successfully");
        return document;
    }

    /**
     * 清洗文本、分块文本
     * @param document 文档对象
     * @return 文本段列表
     */
    @Override
    public List<TextSegment> splitDocument(Document document) {
        logger.debug("Splitting PDF document into segments");
        List<TextSegment> segments = SPLITTER.split(document);
        logger.info("PDF document split into {} segments", segments.size());
        return segments;
    }

    @Override
    public ParseType getType() {
        return ParseType.PDF;
    }
}
