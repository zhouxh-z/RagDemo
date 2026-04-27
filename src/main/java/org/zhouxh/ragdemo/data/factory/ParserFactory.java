package org.zhouxh.ragdemo.data.factory;

import org.springframework.stereotype.Component;
import org.zhouxh.ragdemo.data.enums.ParseType;
import org.zhouxh.ragdemo.data.service.DocumentParseService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ParserFactory {

    public final Map<ParseType, DocumentParseService<ParseType>> parseMap;


    // 构造器自动注入所有实现
    public ParserFactory(List<DocumentParseService<ParseType>> parseList) {
        this.parseMap = parseList.stream()
                .collect(Collectors.toMap(
                        DocumentParseService::getType,  // key：type
                        s -> s     // value：实现类
                ));
    }

    public DocumentParseService<ParseType> getParseService(ParseType type) {
        return this.parseMap.getOrDefault(type, this.parseMap.get(ParseType.TEXT)); // 默认返回 文本 解析器
    }
}
