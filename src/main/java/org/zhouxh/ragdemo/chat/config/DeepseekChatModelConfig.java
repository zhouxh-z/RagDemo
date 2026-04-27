package org.zhouxh.ragdemo.chat.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeepseekChatModelConfig {

    @Value("${deepseek.api.url}")
    String apiUrl;
    @Value("${deepseek.api.key}")
    String apiKey;

    @Bean("deepseekStreamingChatModel")
    public StreamingChatLanguageModel  streamingChatLanguageModel() {
        return OpenAiStreamingChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName("deepseek-v4-flash").build();
    }

    @Bean("deepseekChatModel")
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(apiUrl)
                .apiKey(apiKey)
                .modelName("deepseek-v4-flash").build();
    }
}
