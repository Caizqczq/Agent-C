package org.czq.infrastructure.config;

import org.czq.domain.llm.service.LlmService;
import org.czq.infrastructure.integration.llm.siliconflow.SiliconFlowLlmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LlmConfig {

    @Value("${llm.provider.default:siliconflow}")
    private String defaultProvider;

    @Bean
    @Primary
    public LlmService defaultLlmService(SiliconFlowLlmService siliconFlowLlmService) {
        return siliconFlowLlmService;
    }

    @Bean
    public Map<String, LlmService> llmServiceMap(SiliconFlowLlmService siliconFlowLlmService) {
        Map<String, LlmService> serviceMap = new HashMap<>();
        // 确保键名与defaultProvider + "LlmService"匹配
        serviceMap.put("siliconflowLlmService", siliconFlowLlmService);
        return serviceMap;
    }
}