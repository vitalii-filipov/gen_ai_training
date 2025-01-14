package com.epam.training.gen.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ConfigurationProperties(prefix = "embeddings")
@Getter
@Setter
@ToString
public class EmbeddingsProperties {
    
    private String collectionName;
    
    private String deploymentName;

    private int vectorSize;

    private int searchLimit;

    private String initDocsPath;
}
