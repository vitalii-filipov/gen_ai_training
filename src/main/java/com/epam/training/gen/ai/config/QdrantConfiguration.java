package com.epam.training.gen.ai.config;

import java.util.concurrent.ExecutionException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

@Configuration
public class QdrantConfiguration {

    @Bean
    public QdrantClient qdrantClient() throws InterruptedException, ExecutionException {
        var client = new QdrantClient(
                QdrantGrpcClient.newBuilder("localhost", 6334, false).build());
        return client;
    }
}
