package org.zhouxh.ragdemo.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 缓存配置
 */
@Configuration
public class CacheConfig {

    /**
     * 嵌入缓存：缓存文本段到向量的映射，避免重复计算
     */
    @Bean
    public Cache<String, String> embeddingCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000) // 最大缓存 1000 个条目
                .expireAfterWrite(Duration.ofHours(1)) // 1 小时后过期
                .build();
    }

    /**
     * 搜索结果缓存：缓存查询到结果的映射
     */
    @Bean
    public Cache<String, String> searchCache() {
        return Caffeine.newBuilder()
                .maximumSize(500) // 最大缓存 500 个查询
                .expireAfterWrite(Duration.ofMinutes(30)) // 30 分钟后过期
                .build();
    }
}
