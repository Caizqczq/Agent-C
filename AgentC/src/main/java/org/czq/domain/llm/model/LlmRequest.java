package org.czq.domain.llm.model;

import java.util.ArrayList;
import java.util.List;

public class LlmRequest {

    private String model;

    private List<LlmMessage> messages;

    private Double temperature;

    private Integer maxTokens;

    private Boolean stream;

    public LlmRequest() {
        this.messages = new ArrayList<>();
        this.temperature = 0.7;
        this.stream = false;
    }

    public LlmRequest addMessage(LlmMessage message) {
        this.messages.add(message);
        return this;
    }

    public LlmRequest addUserMessage(String content) {
        return addMessage(LlmMessage.ofUser(content));
    }
    
    public LlmRequest addAssistantMessage(String content) {
        return addMessage(LlmMessage.ofAssistant(content));
    }
    
    public LlmRequest addSystemMessage(String content) {
        return addMessage(LlmMessage.ofSystem(content));
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<LlmMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LlmMessage> messages) {
        this.messages = messages;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }
}
