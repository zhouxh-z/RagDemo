package org.zhouxh.ragdemo.chat.controller;

import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.zhouxh.ragdemo.chat.service.ChatService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Resource
    private ChatService chatService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestParam("memoryId") String memoryId, @RequestParam("question") String question) {
        logger.info("Starting stream chat for memoryId: {}, question: {}", memoryId, question);
        SseEmitter emitter = new SseEmitter();
        Flux<String> flux = chatService.streamChat(memoryId, question);
        flux.subscribe(
            data -> {
                try {
                    emitter.send(SseEmitter.event().data(data));
                } catch (Exception e) {
                    logger.error("Error sending SSE data for memoryId: {}", memoryId, e);
                    emitter.completeWithError(e);
                }
            },
            error -> {
                logger.error("Flux error for memoryId: {}", memoryId, error);
                emitter.completeWithError(error);
            },
            () -> {
                logger.info("Stream completed for memoryId: {}", memoryId);
                emitter.complete();
            }
        );
        return emitter;
    }

    @GetMapping(value = "/query")
    public String chat(@RequestParam("memoryId") String memoryId, @RequestParam("question") String question) {
        logger.info("Processing chat query for memoryId: {}, question: {}", memoryId, question);
        try {
            String response = chatService.chat(memoryId, question);
            logger.info("Chat query completed for memoryId: {}", memoryId);
            return response;
        } catch (Exception e) {
            logger.error("Error processing chat query for memoryId: {}", memoryId, e);
            throw e;
        }
    }
}
