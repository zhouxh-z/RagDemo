package org.zhouxh.ragdemo.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 限流配置
 */
@Configuration
public class RateLimitConfig {

    /**
     * 上传限流：每分钟最多 10 次上传
     */
    @Bean
    public Bucket uploadBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * 搜索限流：每分钟最多 100 次搜索
     */
    @Bean
    public Bucket searchBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }
}
