package org.czq.infrastructure.integration.llm.siliconflow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.czq.domain.llm.model.LlmMessage;
import org.czq.domain.llm.model.LlmRequest;
import org.czq.domain.llm.model.LlmResponse;
import org.czq.infrastructure.integration.llm.AbstractLlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class SiliconFlowLlmService extends AbstractLlmService {

    private final Logger logger = LoggerFactory.getLogger(SiliconFlowLlmService.class);

    public SiliconFlowLlmService(
            @Value("${llm.provider.providers.siliconflow.name:SiliconFlow}") String providerName,
            @Value("${llm.provider.providers.siliconflow.api-url:https://api.siliconflow.cn/v1/chat/completions}") String apiUrl,
            @Value("${llm.provider.providers.siliconflow.api-key:}") String apiKey,
            @Value("${llm.provider.providers.siliconflow.model:llama3}") String defaultModel,
            @Value("${llm.provider.providers.siliconflow.timeout:30000}") int timeout) {
        super(providerName, apiUrl, apiKey, defaultModel, timeout);

        if (apiKey == null || apiKey.isEmpty()) {
            logger.warn("SiliconFlow API密钥未配置，请通过环境变量SILICONFLOW_API_KEY设置");
        } else {
            logger.info("初始化SiliconFlow服务，默认模型: {}", defaultModel);
        }
    }

    @Override
    public LlmResponse chat(LlmRequest request) {
        if (request.getModel() == null || "default".equals(request.getModel())) {
            logger.info("未指定模型或使用默认模型，使用配置的默认模型: {}", getDefaultModel());
            request.setModel(getDefaultModel());
        }
        
        try {
            logger.info("发送请求到SiliconFlow服务, 模型: {}, 消息数: {}",
                    request.getModel(), request.getMessages().size());

            String requestBody = prepareRequestBody(request);
            String responseBody = sendHttpRequest(requestBody);

            logger.debug("SiliconFlow服务响应长度: {}", responseBody.length());
            LlmResponse response = parseResponse(responseBody);
            response.setProvider(getProviderName());
            response.setModel(request.getModel());
            return response;
        }catch (Exception e){
            logger.error("调用SiliconFlow服务出错", e);
            LlmResponse errorResponse = new LlmResponse("调用服务时发生错误: " + e.getMessage());
            errorResponse.setProvider(getProviderName());
            errorResponse.setModel(request.getModel());
            return errorResponse;
        }
    }

    @Override
    protected String prepareRequestBody(LlmRequest request) {
        JSONObject requestJson = new JSONObject();
        requestJson.put("model", request.getModel());

        // 转换消息格式
        JSONArray messagesJson = new JSONArray();
        List<LlmMessage> messages = request.getMessages();
        for (LlmMessage message : messages) {
            JSONObject messageJson = new JSONObject();
            messageJson.put("role", message.getRole());
            messageJson.put("content", message.getContent());
            messagesJson.add(messageJson);
        }
        requestJson.put("messages", messagesJson);

        // 添加其他参数
        if (request.getTemperature() != null) {
            requestJson.put("temperature", request.getTemperature());
        } else {
            requestJson.put("temperature", 0.7); // 默认温度
        }

        if (request.getMaxTokens() != null) {
            requestJson.put("max_tokens", request.getMaxTokens());
        }

        if (request.getStream() != null) {
            requestJson.put("stream", request.getStream());
        } else {
            requestJson.put("stream", false); // 默认非流式
        }

        return requestJson.toJSONString();
    }

    @Override
    protected LlmResponse parseResponse(String responseBody) {
        LlmResponse response = new LlmResponse();
        response.setProvider(getProviderName());
        response.setModel(getDefaultModel());

        try {
            if (responseBody == null || responseBody.isBlank()) {
                response.setContent("SiliconFlow返回空响应");
                return response;
            }

            JSONObject responseJson = JSON.parseObject(responseBody);

            // SiliconFlow 常见错误格式：{"code":20012,"message":"...","data":null}
            if (!responseJson.containsKey("choices")
                    && responseJson.containsKey("code")
                    && responseJson.containsKey("message")) {
                response.setContent("SiliconFlow请求失败["
                        + responseJson.getString("code") + "]: "
                        + responseJson.getString("message"));
                return response;
            }

            // OpenAI 风格错误：{"error": {...}}
            if (responseJson.containsKey("error")) {
                JSONObject error = responseJson.getJSONObject("error");
                String message = error == null ? null : error.getString("message");
                response.setContent("SiliconFlow请求失败: " + (message == null ? truncate(responseBody, 512) : message));
                return response;
            }

            JSONArray choicesArray = responseJson.getJSONArray("choices");
            if (choicesArray == null || choicesArray.isEmpty()) {
                response.setContent("SiliconFlow响应缺少choices: " + truncate(responseBody, 512));
                return response;
            }

            JSONObject firstChoice = choicesArray.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message == null ? null : message.getString("content");
            if (content == null) {
                content = firstChoice.getString("text");
            }
            if (content == null) {
                response.setContent("SiliconFlow响应缺少content: " + truncate(responseBody, 512));
                return response;
            }

            response.setContent(content);
            response.setFinishReason(firstChoice.getString("finish_reason"));

            if (responseJson.containsKey("usage")) {
                JSONObject usage = responseJson.getJSONObject("usage");
                response.setTokenUsage(usage.getInteger("total_tokens"));
                logger.info("请求消耗Token: {}", response.getTokenUsage());
            }

            return response;
        } catch (Exception e) {
            logger.error("解析SiliconFlow响应出错", e);
            response.setContent("解析服务响应时发生错误: " + e.getMessage());
            return response;
        }
    }

    private String sendHttpRequest(String requestBody) throws IOException {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();

        HttpPost httpPost = new HttpPost(apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + apiKey);

        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        logger.debug("发送HTTP请求到: {}", apiUrl);

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();
            logger.debug("HTTP响应状态码: {}", statusCode);

            HttpEntity responseEntity = response.getEntity();
            String responseBody = responseEntity == null ? "" : EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

            if (statusCode < 200 || statusCode >= 300) {
                String errorMessage = extractErrorMessage(responseBody);
                logger.error("HTTP请求失败，状态码: {}, 错误信息: {}, 响应体: {}",
                        statusCode, errorMessage, truncate(responseBody, 2048));
                throw new IOException("SiliconFlow HTTP " + statusCode + ": " + errorMessage);
            }

            return responseBody;
        } finally {
            httpClient.close();
        }
    }

    private static String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty response body";
        }
        try {
            JSONObject json = JSON.parseObject(responseBody);
            if (json.containsKey("message")) {
                String message = json.getString("message");
                if (message != null && !message.isBlank()) {
                    return message;
                }
            }
            if (json.containsKey("error")) {
                JSONObject error = json.getJSONObject("error");
                String message = error == null ? null : error.getString("message");
                if (message != null && !message.isBlank()) {
                    return message;
                }
            }
        } catch (Exception ignored) {
            // ignore parsing failures, fall through
        }
        return truncate(responseBody, 256);
    }

    private static String truncate(String text, int maxChars) {
        if (text == null) {
            return null;
        }
        if (maxChars <= 0 || text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...(truncated)";
    }

}
