package org.czq.domain.llm.model;

public class LlmMessage {
    
    private String role;
    
    private String content;

    public LlmMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public LlmMessage() {
    }

    public static LlmMessage ofUser(String content) {
        return new LlmMessage("user", content);
    }
    
    public static LlmMessage ofAssistant(String content) {
        return new LlmMessage("assistant", content);
    }

    public static LlmMessage ofSystem(String content) {
        return new LlmMessage("system", content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
}
