package org.czq.infrastructure.integration.llm;

import org.czq.domain.llm.model.LlmRequest;
import org.czq.domain.llm.model.LlmResponse;
import org.czq.domain.llm.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLlmService implements LlmService {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final String providerName;

    protected final String apiUrl;

    protected final String apiKey;

    protected final String defaultModel;

    protected final int timeout;

    public AbstractLlmService(String providerName, String apiUrl, String apiKey, String defaultModel, int timeout) {
        this.providerName = providerName;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.timeout = timeout;
    }
    

    @Override
    public String simpleChat(String text) {
        LlmRequest request = new LlmRequest();
        request.setModel(getDefaultModel());
        request.addUserMessage(text);
        
        LlmResponse response = chat(request);
        return response.getContent();
    }

    @Override
    public String getProviderName() {
        return providerName;
    }

    @Override
    public String getDefaultModel() {
        return defaultModel;
    }

    protected abstract String prepareRequestBody(LlmRequest request);

    protected abstract LlmResponse parseResponse(String responseBody);
}
