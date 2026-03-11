package com.gongan;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class A {
    public static void main(String[] args) throws IOException {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(600000L, TimeUnit.MILLISECONDS)
                .readTimeout(600000L, TimeUnit.MILLISECONDS)
                .writeTimeout(600000L, TimeUnit.MILLISECONDS)
                .build();


        String embeddingJson = """
                {
                    "model": "qwen3-embedding:0.6b",
                    "input": "将这段话转换为向量"
                }
                """;

        Request request = new Request.Builder()
                .url("http://localhost:11434/v1/embeddings")
                .post(RequestBody.create(embeddingJson, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            // 这里会返回一个包含 "embedding": [0.12, -0.05, ...] 的 JSON
            System.out.println(response.body().string());
        }

        String json = """
                {
                    "model": "qwen3.5:9b",
                    "messages": [{"role": "user", "content": "静夜思全文"}],
                    "stream": false
                }
                """;

        Request request2 = new Request.Builder()
                .url("http://localhost:11434/v1/chat/completions")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request2).execute()) {
            System.out.println(response.body().string());
        }
    }
}
