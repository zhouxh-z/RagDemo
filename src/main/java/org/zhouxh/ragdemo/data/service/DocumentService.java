package org.zhouxh.ragdemo.data.service;

import org.springframework.web.multipart.MultipartFile;
import org.zhouxh.ragdemo.data.enums.ParseType;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 文档解析接口
 * @author zhouxh
 * @date 2024/6/9 17:01
 */
public interface DocumentService {
    CompletableFuture<Void> upload(MultipartFile file, ParseType parseType) throws IOException;
    List<String> search(String question);
}
