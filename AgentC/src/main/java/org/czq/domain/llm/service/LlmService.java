package org.czq.domain.llm.service;

import org.czq.domain.llm.model.LlmRequest;
import org.czq.domain.llm.model.LlmResponse;

public interface LlmService {

    LlmResponse chat(LlmRequest request);
    
    String simpleChat(String text);
    
    String getProviderName();
    
    String getDefaultModel();
}
