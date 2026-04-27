package org.zhouxh.ragdemo.data.service.impl;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhouxh.ragdemo.data.enums.EmbeddingType;
import org.zhouxh.ragdemo.data.enums.ParseType;
import org.zhouxh.ragdemo.data.factory.EmbeddingFactory;
import org.zhouxh.ragdemo.data.factory.ParserFactory;
import org.zhouxh.ragdemo.data.service.DocumentParseService;
import org.zhouxh.ragdemo.data.service.DocumentService;
import org.zhouxh.ragdemo.data.service.EmbeddingService;
import org.zhouxh.ragdemo.data.util.FileFingerprintUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class DocumentServiceImpl implements DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    @Resource
    EmbeddingFactory embeddingFactory;
    @Resource
    ParserFactory parserFactory;

    @Resource
    private StringRedisTemplate redisTemplate;

    private static final String FINGERPRINT_KEY_PREFIX = "file:fingerprint:";

    @Override
    @Async
    public CompletableFuture<Void> upload(MultipartFile file, ParseType parseType) throws IOException {
        logger.info("Starting async document upload for file: {}, parseType: {}", file.getOriginalFilename(), parseType);

        try {
            // 读取文件内容到字节数组，用于计算指纹和解析
            byte[] fileBytes = file.getBytes();
            String fingerprint = FileFingerprintUtil.getFileSha256(new ByteArrayInputStream(fileBytes));
            String redisKey = FINGERPRINT_KEY_PREFIX + fingerprint;

            // 检查 Redis 中是否已有该指纹
            Boolean exists = redisTemplate.hasKey(redisKey);
            if (exists) {
                logger.info("File already exists with fingerprint: {}, skipping upload", fingerprint);
                return CompletableFuture.completedFuture(null);
            }

            // 解析文档
            DocumentParseService<ParseType> parseService = parserFactory.getParseService(parseType);
            logger.debug("Parsing document: {}", file.getOriginalFilename());
            Document document = parseService.parseDocument(new ByteArrayInputStream(fileBytes));

            // 切分文档
            logger.debug("Splitting document into segments");
            List<TextSegment> segments = parseService.splitDocument(document);
            logger.info("Document split into {} segments", segments.size());

            // 向量化
            logger.debug("Embedding segments");
            EmbeddingService<EmbeddingType> embeddingService = embeddingFactory.getEmbeddingService(EmbeddingType.ELASTICSEARCH);
            embeddingService.embedded(segments);
            logger.info("Document upload completed for: {}", file.getOriginalFilename());

            // 存储指纹到 Redis，设置过期时间（例如 30 天）
            String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            redisTemplate.opsForValue().set(redisKey, filename, Duration.ofDays(30));
            logger.info("Fingerprint stored in Redis: {}", fingerprint);

            return CompletableFuture.completedFuture(null);
        } catch (IOException e) {
            logger.error("IO error during async upload for file: {}", file.getOriginalFilename(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during async upload for file: {}", file.getOriginalFilename(), e);
            throw new IOException("Upload failed", e);
        }
    }

    @Override
    public List<String> search(String question) {
        logger.info("Searching for question: {}", question);
        EmbeddingService<EmbeddingType> embeddingService = embeddingFactory.getEmbeddingService(EmbeddingType.ELASTICSEARCH);
        List<String> search = embeddingService.search(question);
        logger.info("Search returned {} results", search.size());
        return search;
    }


}
