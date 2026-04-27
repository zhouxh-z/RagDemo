package org.zhouxh.ragdemo.chat.service;

import org.springframework.stereotype.Service;

/**
 * 查询改写服务
 * 用于改写和增强用户输入的问题，提高检索准确性
 */
@Service
public interface QueryRewriter {

    /**
     * 改写用户问题
     * @param originalQuery 原始用户问题
     * @return 改写后的查询
     */
    String rewriteQuery(String originalQuery);
}
