package com.epam.training.gen.ai.config;

import java.util.concurrent.ExecutionException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

@Configuration
public class QdrantConfiguration {

    @Bean
    public QdrantClient qdrantClient(EmbeddingsProperties properties) throws InterruptedException, ExecutionException {
        var client = new QdrantClient(
                QdrantGrpcClient.newBuilder("localhost", 6334, false).build());
        ensureCollectionExists(client, properties.getCollectionName());
        return client;
    }

    private void ensureCollectionExists(QdrantClient client, String collectionName) throws InterruptedException, ExecutionException {
        var exists = client.collectionExistsAsync(collectionName).get();
        if (!exists) {
            client.createCollectionAsync(collectionName,
                    VectorParams.newBuilder()
                            .setDistance(Distance.Cosine)
                            .setSize(4)
                            .build())
                    .get();
        }
    }
}
