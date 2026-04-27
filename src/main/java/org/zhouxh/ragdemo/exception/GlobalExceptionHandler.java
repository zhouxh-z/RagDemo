package org.zhouxh.ragdemo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中的异常，返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理 IO 异常（如文件上传失败）
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException e) {
        logger.error("IO exception occurred", e);
        Map<String, Object> error = new HashMap<>();
        error.put("error", "File operation failed");
        error.put("message", e.getMessage());
        error.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("Invalid argument: {}", e.getMessage());
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid request");
        error.put("message", e.getMessage());
        error.put("code", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理 SSE 异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        logger.error("Unexpected error occurred", e);
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Internal server error");
        error.put("message", "An unexpected error occurred");
        error.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 处理 SSE 相关的异常，返回错误事件
     */
    @ExceptionHandler(RuntimeException.class)
    public SseEmitter handleSseException(RuntimeException e) {
        logger.error("SSE error occurred", e);
        SseEmitter emitter = new SseEmitter();
        try {
            emitter.send(SseEmitter.event().name("error").data("An error occurred during streaming"));
            emitter.complete();
        } catch (IOException ioException) {
            logger.error("Failed to send error event", ioException);
            emitter.completeWithError(ioException);
        }
        return emitter;
    }
}
