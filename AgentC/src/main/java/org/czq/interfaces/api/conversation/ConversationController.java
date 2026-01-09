package org.czq.interfaces.api.conversation;

import org.czq.application.conversation.dto.ChatRequest;
import org.czq.application.conversation.dto.ChatResponse;
import org.czq.application.conversation.service.ConversationService;
import org.czq.interfaces.api.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/conversation")
public class ConversationController {

    private final Logger logger = LoggerFactory.getLogger(ConversationController.class);

    @Resource
    private ConversationService conversationService;

    @PostMapping("/chat")
    public Result<ChatResponse> chat(@RequestBody @Validated ChatRequest request) {
        logger.info("收到聊天请求: {}, 服务商: {}, 模型: {}",
                request.getMessage(),
                request.getProvider() != null ? request.getProvider() : "默认",
                request.getModel() != null ? request.getModel() : "默认");

        // 如果请求没有指定服务商，默认使用SiliconFlow
        if (request.getProvider() == null || request.getProvider().isEmpty()) {
            request.setProvider("siliconflow");
        }

        try {
            ChatResponse response = conversationService.chat(request);
            return Result.success(response);
        } catch (Exception e) {
            logger.error("处理聊天请求异常", e);
            return Result.serverError("处理请求失败: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public Result<Object> health() {
        return Result.success("服务正常运行中");
    }
}
