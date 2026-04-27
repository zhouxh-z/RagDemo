package org.zhouxh.ragdemo.data.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;

import java.io.InputStream;
import java.util.List;

public interface DocumentParseService<T extends Enum<T>> {

    T getType();

    Document parseDocument(InputStream doc);

    List<TextSegment> splitDocument(Document document);
}
