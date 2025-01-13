package com.epam.training.gen.ai.services;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.training.gen.ai.config.EmbeddingsProperties;
import com.epam.training.gen.ai.models.ScoreResult;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithPayloadSelectorFactory;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.SearchPoints;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private QdrantClient qdrantClient;

    @Autowired
    private EmbeddingsProperties properties;

    @Autowired
    private OpenAIAsyncClient openAiClient;

    @Autowired
    private TaskExecutor taskExecutor;

    public Flux<List<Float>> buildEmbedding(String text) {
        var options = new EmbeddingsOptions(List.of(normalize(text)));
        options.setDimensions(properties.getVectorSize());

        // import static convenience methods
        return openAiClient.getEmbeddings(properties.getDeploymentName(), options)
                .flatMapIterable(embedding -> embedding.getData())
                .map(item -> item.getEmbedding())
                .doOnError(t -> logger.error("Exception thrown during the embedding creation", t));
    }

    public Flux<String> buildEmbeddingAndStore(String text) {
        return buildEmbedding(text)
                .map(vector -> PointStruct
                        .newBuilder()
                        .setId(id(UUID.nameUUIDFromBytes(text.getBytes())))
                        .setVectors(vectors(vector))
                        .putPayload("text", value(text))
                        .build())
                .buffer()
                .map(points -> qdrantClient.upsertAsync(properties.getCollectionName(), points))
                .flatMap(this::toMono)
                .map(result -> result.getStatus().name());
    }

    public Flux<ScoreResult> searchEmbedding(String text) {
        return buildEmbedding(text)
                .map(this::createSearchPoints)
                .map(points -> qdrantClient.searchAsync(points))
                .flatMap(this::toMono)
                .flatMapIterable(points -> points)
                .map(point -> new ScoreResult(
                        point.getId().getUuid(),
                        point.getScore(),
                        point.getPayloadOrDefault("text", value("(no data)")).getStringValue()));
    }

    private SearchPoints createSearchPoints(List<Float> vector) {
        return SearchPoints
                .newBuilder()
                .setCollectionName(properties.getCollectionName())
                .addAllVector(vector)
                .setLimit(properties.getSearchLimit())
                .setWithPayload(WithPayloadSelectorFactory.enable(true))
                .build();
    }

    private <T> Mono<T> toMono(ListenableFuture<T> future) {
        return Mono.create(sink -> {
            Futures.addCallback(future, new FutureCallback<T>() {

                @Override
                public void onSuccess(T result) {
                    sink.success(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    logger.error("Unable to store data: ", t);
                    sink.error(t);
                }
            },
                    taskExecutor);
        });
    }

    private String normalize(String input) {
        return input.replace("\n", "");
    }
}
