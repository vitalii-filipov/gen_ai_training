package com.epam.training.gen.ai.services;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import com.epam.training.gen.ai.config.EmbeddingsProperties;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;

@Component
public class QdrantInitializer {
        private static final Logger logger = LoggerFactory.getLogger(QdrantInitializer.class);

    @Autowired
    private QdrantClient client;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private EmbeddingsProperties properties;

    @EventListener
    public void initQdrant(ApplicationReadyEvent event) throws InterruptedException, ExecutionException {
        logger.info("Starting Qdrant initialization");
        ensureCollectionExists();
        logger.info("Finished Qdrant initialization");
    }

        private void ensureCollectionExists()
            throws InterruptedException, ExecutionException {
        var exists = client.collectionExistsAsync(properties.getCollectionName()).get();
        if (!exists) {
            client.createCollectionAsync(properties.getCollectionName(),
                    VectorParams.newBuilder()
                            .setDistance(Distance.Cosine)
                            .setSize(properties.getVectorSize())
                            .build())
                    .get();
            populateCollection();
        }
    }

    private void populateCollection() {
        var contents = readFiles(properties.getInitDocsPath());
        contents.forEach(content ->
            embeddingService.buildEmbeddingAndStore(content).subscribe()
        );
    }

    public static List<String> readFiles(String path) {
        List<String> contents = new ArrayList<>();
        try {
            PathMatchingResourcePatternResolver scanner = new PathMatchingResourcePatternResolver();
            Resource[] resources = scanner.getResources(path);

            if (resources == null || resources.length == 0) {
                logger.warn("Warning: could not find any resources in this scanned package: " + path);
                return contents;
            }

            for (Resource resource : resources) {
                logger.info(resource.getFilename());
                contents.add(resource.getContentAsString(Charset.defaultCharset()));
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read the resources folder: " + e.getMessage(), e);
        }
        return contents;
    }
}