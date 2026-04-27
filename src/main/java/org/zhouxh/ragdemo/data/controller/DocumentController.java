package org.zhouxh.ragdemo.data.controller;

import io.github.bucket4j.Bucket;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhouxh.ragdemo.data.enums.ParseType;
import org.zhouxh.ragdemo.data.service.DocumentService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/doc")
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    @Resource
    DocumentService documentService;

    @Resource(name = "uploadBucket")
    private Bucket uploadBucket;

    @Resource(name = "searchBucket")
    private Bucket searchBucket;

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 限流检查
        if (!uploadBucket.tryConsume(1)) {
            logger.warn("Upload rate limit exceeded for file: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("Too many upload requests. Please try again later.");
        }

        logger.info("Uploading file: {}, size: {}", file.getOriginalFilename(), file.getSize());
        ParseType parseType = getParseTypeFromFile(file);
        if (parseType == null) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getOriginalFilename());
        }
        try {
            // 异步上传
            CompletableFuture<Void> future = documentService.upload(file, parseType);
            // 由于异步，这里不能立即知道是否跳过，但可以返回通用消息
            logger.info("File upload initiated asynchronously: {}", file.getOriginalFilename());
            return "Upload initiated successfully";
        } catch (IOException e) {
            logger.error("Failed to initiate upload for file: {}", file.getOriginalFilename(), e);
            throw e;
        }
    }

    private ParseType getParseTypeFromFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null) return null;
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "txt":
                return ParseType.TEXT;
            case "pdf":
                return ParseType.PDF;
            case "doc":
            case "docx":
                return ParseType.WORD;
            case "html":
            case "htm":
                return ParseType.HTML;
            default:
                return null;
        }
    }

    @GetMapping("/search")
    public List<String> search(String question) {
        // 限流检查
        if (!searchBucket.tryConsume(1)) {
            logger.warn("Search rate limit exceeded for question: {}", question);
            throw new IllegalArgumentException("Too many search requests. Please try again later.");
        }

        logger.info("Searching documents for question: {}", question);
        try {
            List<String> results = documentService.search(question);
            logger.info("Search completed, found {} results", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error during search for question: {}", question, e);
            throw e;
        }
    }

}
