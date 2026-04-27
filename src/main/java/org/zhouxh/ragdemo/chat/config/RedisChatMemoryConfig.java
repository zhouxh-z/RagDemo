package org.zhouxh.ragdemo.chat.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import dev.langchain4j.store.memory.chat.redis.RedisChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisChatMemoryConfig {


    /**
     * 1. 存储层：Redis 持久化（重启不丢、多实例共享）
     */
    @Bean
    public ChatMemoryStore chatMemoryStore(RedisConnectionFactory factory) {
        return RedisChatMemoryStore
                .builder()
                .host("127.0.0.1")
                .port(6379)
                .build();
    }

    /**
     * 2. 记忆提供者：给 @AiService 使用
     * 每个用户/会话自动创建独立记忆
     */
    @Bean("redisChatMemory")
    public ChatMemoryProvider chatMemoryProvider(ChatMemoryStore store) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(15)  // 每个会话保留 15 条消息（7轮对话）
                .chatMemoryStore(store) // 关键：持久化到 Redis
                .build();
    }
}
