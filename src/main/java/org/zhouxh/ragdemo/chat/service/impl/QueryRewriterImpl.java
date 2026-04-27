package org.zhouxh.ragdemo.chat.service.impl;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zhouxh.ragdemo.chat.service.QueryRewriter;

import java.util.Map;

/**
 * 查询改写服务实现
 */
@Service
public class QueryRewriterImpl implements QueryRewriter {

    private static final Logger logger = LoggerFactory.getLogger(QueryRewriterImpl.class);

    @Resource(name = "deepseekChatModel")
    private ChatLanguageModel chatModel;

    private static final String REWRITE_PROMPT_TEMPLATE =
            """
            请根据用户的问题，改写为更清晰、准确的查询语句，以便在文档中检索相关信息。保持简洁，避免添加无关内容。

            原始问题: {{originalQuery}}

            改写后的问题:
            """;

    @Override
    public String rewriteQuery(String originalQuery) {
        logger.debug("Rewriting query: {}", originalQuery);

        try {
            PromptTemplate promptTemplate = PromptTemplate.from(REWRITE_PROMPT_TEMPLATE);
            Prompt prompt = promptTemplate.apply(Map.of("originalQuery", originalQuery));

            String rewrittenQuery = chatModel.generate(prompt.text());
            logger.info("Query rewritten from '{}' to '{}'", originalQuery, rewrittenQuery.trim());
            return rewrittenQuery.trim();
        } catch (Exception e) {
            logger.warn("Failed to rewrite query: {}, using original", originalQuery, e);
            return originalQuery;  // 失败时返回原始查询
        }
    }
}
