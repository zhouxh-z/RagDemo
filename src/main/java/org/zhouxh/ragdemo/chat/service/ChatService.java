package org.zhouxh.ragdemo.chat.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatMemoryProvider = "redisChatMemory",
        streamingChatModel = "deepseekStreamingChatModel",
        chatModel = "deepseekChatModel",
        contentRetriever = "contentAndEmbeddingRetriever"
)
public interface ChatService {

    @SystemMessage("你是基于LangChain4j构建的RAG问答系统，结合了文本数据和向量数据库，能够理解用户的问题并从相关文档中提取信息来回答问题。请根据用户的问题，从文本数据和向量数据库中检索相关信息，并生成一个准确、简洁的回答。")
    Flux<String> streamChat(@MemoryId String memoryId,@UserMessage String question);

    @SystemMessage("你是基于LangChain4j构建的RAG问答系统，结合了文本数据和向量数据库，能够理解用户的问题并从相关文档中提取信息来回答问题。请根据用户的问题，从文本数据和向量数据库中检索相关信息，并生成一个准确、简洁的回答。")
    String chat(@MemoryId String memoryId,@UserMessage String question);

}
