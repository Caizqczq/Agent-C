package org.czq.application.conversation.dto;

import javax.validation.constraints.NotBlank;

public class ChatRequest {
    @NotBlank()
    private String message;
    
}
