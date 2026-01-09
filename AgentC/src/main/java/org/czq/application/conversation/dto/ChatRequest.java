package org.czq.application.conversation.dto;

import javax.validation.constraints.NotBlank;

public class ChatRequest {
    
    @NotBlank(message = "消息不能为空")
    private String message;
    
    private String sessionId;
    
    private String provider;
    
    private String model;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
