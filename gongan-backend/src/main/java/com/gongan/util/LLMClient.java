package com.gongan.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.gongan.config.LLMProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * LLM人员端（兼容OpenAI和Ollama接口）
 *
 * @author Risk Management System
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LLMClient {

    private final LLMProperties llmProperties;
    private OkHttpClient httpClient;

    private OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(llmProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .readTimeout(llmProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .writeTimeout(llmProperties.getTimeout(), TimeUnit.MILLISECONDS)
                    .build();
        }
        return httpClient;
    }

    /**
     * 判断是否使用 Ollama 原生 API
     * 如果 URL 包含 /v1 则使用 OpenAI 兼容模式，否则使用 Ollama 原生 API
     */
    private boolean useOllamaNativeApi() {
        String apiUrl = llmProperties.getApiUrl();
        if (apiUrl == null) {
            return false;
        }
        // 如果包含 /v1，使用 OpenAI 兼容模式
        if (apiUrl.contains("/v1")) {
            return false;
        }
        // 如果包含 /api 或端口 11434，使用 Ollama 原生 API
        return apiUrl.contains(":11434") || apiUrl.contains("/api");
    }

    /**
     * 聊天补全
     */
    public String chat(List<Map<String, String>> messages) {
        return chat(messages, null);
    }

    /**
     * 聊天补全（带系统提示）
     */
    public String chat(List<Map<String, String>> messages, String systemPrompt) {
        if (useOllamaNativeApi()) {
            return chatOllama(messages, systemPrompt);
        }
        return chatOpenAI(messages, systemPrompt);
    }

    /**
     * JSON结构化输出专用：temperature=0，强制JSON格式（用于意图识别等分类任务）
     */
    public String chatJson(List<Map<String, String>> messages, String systemPrompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("max_tokens", 8192); // 增加 token 限制，适应批量关键词生成
            requestBody.put("temperature", 0.0);
            requestBody.put("response_format", new JSONObject().fluentPut("type", "json_object"));

            JSONArray messagesArray = new JSONArray();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject sysMsg = new JSONObject();
                sysMsg.put("role", "system");
                sysMsg.put("content", systemPrompt);
                messagesArray.add(sysMsg);
            }
            for (Map<String, String> message : messages) {
                JSONObject msg = new JSONObject();
                msg.put("role", message.get("role"));
                msg.put("content", message.get("content"));
                messagesArray.add(msg);
            }
            requestBody.put("messages", messagesArray);

            String url = llmProperties.getApiUrl();
            if (!url.endsWith("/")) url += "/";
            url += "chat/completions";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("LLM JSON请求失败: " + response.code());
                }
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            log.error("LLM JSON请求异常", e);
            throw new RuntimeException("LLM JSON请求异常: " + e.getMessage());
        }
    }

    /**
     * OpenAI 格式的聊天补全
     */
    private String chatOpenAI(List<Map<String, String>> messages, String systemPrompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("max_tokens", llmProperties.getMaxTokens());
            requestBody.put("temperature", llmProperties.getTemperature());

            JSONArray messagesArray = new JSONArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.add(systemMessage);
            }

            for (Map<String, String> message : messages) {
                JSONObject msg = new JSONObject();
                msg.put("role", message.get("role"));
                msg.put("content", message.get("content"));
                messagesArray.add(msg);
            }

            requestBody.put("messages", messagesArray);

            String url = llmProperties.getApiUrl();
            if (!url.endsWith("/")) url += "/";
            url += "chat/completions";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("LLM请求失败: " + response.code());
                }
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");
            }
        } catch (IOException e) {
            log.error("LLM请求异常", e);
            throw new RuntimeException("LLM请求异常: " + e.getMessage());
        }
    }

    /**
     * Ollama 格式的聊天补全
     */
    private String chatOllama(List<Map<String, String>> messages, String systemPrompt) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("stream", false);
            requestBody.put("options", new JSONObject()
                    .fluentPut("temperature", llmProperties.getTemperature())
                    .fluentPut("num_predict", llmProperties.getMaxTokens()));

            JSONArray messagesArray = new JSONArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.add(systemMessage);
            }

            for (Map<String, String> message : messages) {
                JSONObject msg = new JSONObject();
                msg.put("role", message.get("role"));
                msg.put("content", message.get("content"));
                messagesArray.add(msg);
            }

            requestBody.put("messages", messagesArray);

            String url = llmProperties.getApiUrl();
            if (url.contains("/v1")) {
                url = url.replace("/v1", "/api");
            }
            if (!url.endsWith("/")) url += "/";
            url += "chat";

            log.debug("Ollama request: {} -> {}", url, requestBody.toJSONString());

            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new RuntimeException("Ollama请求失败: " + response.code() + " - " + errorBody);
                }
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                JSONObject message = jsonResponse.getJSONObject("message");
                if (message != null) {
                    return message.getString("content");
                }
                // 兼容旧版 /api/generate 格式
                return jsonResponse.getString("response");
            }
        } catch (IOException e) {
            log.error("Ollama请求异常", e);
            throw new RuntimeException("Ollama请求异常: " + e.getMessage());
        }
    }

    /**
     * 流式聊天补全
     */
    public void chatStream(List<Map<String, String>> messages, Consumer<String> onChunk) {
        chatStream(messages, null, onChunk);
    }

    /**
     * 流式聊天补全（带系统提示）
     */
    public void chatStream(List<Map<String, String>> messages, String systemPrompt, Consumer<String> onChunk) {
        if (useOllamaNativeApi()) {
            chatStreamOllama(messages, systemPrompt, onChunk);
        } else {
            chatStreamOpenAI(messages, systemPrompt, onChunk);
        }
    }

    /**
     * OpenAI 格式的流式聊天补全
     */
    private void chatStreamOpenAI(List<Map<String, String>> messages, String systemPrompt, Consumer<String> onChunk) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("max_tokens", llmProperties.getMaxTokens());
            requestBody.put("temperature", llmProperties.getTemperature());
            requestBody.put("stream", true);

            JSONArray messagesArray = new JSONArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.add(systemMessage);
            }

            for (Map<String, String> message : messages) {
                JSONObject msg = new JSONObject();
                msg.put("role", message.get("role"));
                msg.put("content", message.get("content"));
                messagesArray.add(msg);
            }

            requestBody.put("messages", messagesArray);

            String url = llmProperties.getApiUrl();
            if (!url.endsWith("/")) url += "/";
            url += "chat/completions";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .header("Accept", "text/event-stream")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            EventSource.Factory factory = EventSources.createFactory(getHttpClient());
            factory.newEventSource(request, new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equals(data)) {
                        return;
                    }
                    try {
                        JSONObject json = JSON.parseObject(data);
                        JSONArray choices = json.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                            if (delta != null && delta.containsKey("content")) {
                                String content = delta.getString("content");
                                if (content != null) {
                                    onChunk.accept(content);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析SSE数据失败: {}", data);
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    log.error("SSE连接失败", t);
                }
            });
        } catch (Exception e) {
            log.error("流式LLM请求异常", e);
            throw new RuntimeException("流式LLM请求异常: " + e.getMessage());
        }
    }

    /**
     * Ollama 格式的流式聊天补全
     */
    private void chatStreamOllama(List<Map<String, String>> messages, String systemPrompt, Consumer<String> onChunk) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getModel());
            requestBody.put("stream", true);
            requestBody.put("options", new JSONObject()
                    .fluentPut("temperature", llmProperties.getTemperature())
                    .fluentPut("num_predict", llmProperties.getMaxTokens()));

            JSONArray messagesArray = new JSONArray();

            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", systemPrompt);
                messagesArray.add(systemMessage);
            }

            for (Map<String, String> message : messages) {
                JSONObject msg = new JSONObject();
                msg.put("role", message.get("role"));
                msg.put("content", message.get("content"));
                messagesArray.add(msg);
            }

            requestBody.put("messages", messagesArray);

            String url = llmProperties.getApiUrl();
            if (url.contains("/v1")) {
                url = url.replace("/v1", "/api");
            }
            if (!url.endsWith("/")) url += "/";
            url += "chat";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            EventSource.Factory factory = EventSources.createFactory(getHttpClient());
            factory.newEventSource(request, new EventSourceListener() {
                private final StringBuilder buffer = new StringBuilder();

                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    try {
                        JSONObject json = JSON.parseObject(data);
                        JSONObject message = json.getJSONObject("message");
                        if (message != null) {
                            String content = message.getString("content");
                            if (content != null && !content.isEmpty()) {
                                onChunk.accept(content);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("解析Ollama SSE数据失败: {}", data);
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    log.error("Ollama SSE连接失败", t);
                }
            });
        } catch (Exception e) {
            log.error("流式Ollama请求异常", e);
            throw new RuntimeException("流式Ollama请求异常: " + e.getMessage());
        }
    }

    /**
     * 获取文本嵌入向量
     */
    public List<Float> getEmbedding(String text) {
        if (useOllamaNativeApi()) {
            return getEmbeddingOllama(text);
        }
        return getEmbeddingOpenAI(text);
    }

    /**
     * OpenAI 格式的嵌入向量
     */
    private List<Float> getEmbeddingOpenAI(String text) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getEmbeddingModel());
            requestBody.put("input", text);

            String url = llmProperties.getApiUrl();
            if (!url.endsWith("/")) url += "/";
            url += "embeddings";

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + llmProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Embedding请求失败: " + response.code());
                }
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getJSONArray("data")
                        .getJSONObject(0)
                        .getList("embedding", Float.class);
            }
        } catch (IOException e) {
            log.error("Embedding请求异常", e);
            throw new RuntimeException("Embedding请求异常: " + e.getMessage());
        }
    }

    /**
     * Ollama 格式的嵌入向量
     */
    private List<Float> getEmbeddingOllama(String text) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", llmProperties.getEmbeddingModel());
            requestBody.put("prompt", text);

            String url = llmProperties.getApiUrl();
            if (url.contains("/v1")) {
                url = url.replace("/v1", "/api");
            }
            if (!url.endsWith("/")) url += "/";
            url += "embeddings";

            log.debug("Ollama embedding request: {} -> {}", url, text.substring(0, Math.min(50, text.length())));

            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody.toJSONString(), MediaType.parse("application/json")))
                    .build();

            try (Response response = getHttpClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "";
                    throw new RuntimeException("Ollama Embedding请求失败: " + response.code() + " - " + errorBody);
                }
                String responseBody = response.body().string();
                JSONObject jsonResponse = JSON.parseObject(responseBody);
                return jsonResponse.getList("embedding", Float.class);
            }
        } catch (IOException e) {
            log.error("Ollama Embedding请求异常", e);
            throw new RuntimeException("Ollama Embedding请求异常: " + e.getMessage());
        }
    }
}
