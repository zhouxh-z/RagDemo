package org.zhouxh.ragdemo.data.factory;

import org.springframework.stereotype.Component;
import org.zhouxh.ragdemo.data.enums.EmbeddingType;
import org.zhouxh.ragdemo.data.service.EmbeddingService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmbeddingFactory {

    public final Map<EmbeddingType, EmbeddingService<EmbeddingType>> embeddingMap;


    // 构造器自动注入所有实现
    public EmbeddingFactory(List<EmbeddingService<EmbeddingType>> embeddingList) {
        this.embeddingMap = embeddingList.stream()
                .collect(Collectors.toMap(
                        s -> s.getType(),  // key：type
                        s -> s     // value：实现类
                ));
    }

    public EmbeddingService<EmbeddingType> getEmbeddingService(EmbeddingType type) {
        return this.embeddingMap.getOrDefault(type, this.embeddingMap.get(EmbeddingType.ELASTICSEARCH)); // 默认返回 文本 解析器
    }
}
