package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class VectorStoreConfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        try {
            // Strip "jdbc:" prefix and parse as standard URI
            URI uri = new URI(dbUrl.replaceFirst("^jdbc:", ""));

            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;

            // Extract database name from path (remove leading slash)
            String path = uri.getPath();
            String database = (path != null && path.length() > 1)
                    ? path.substring(1).split("/")[0] // Handle edge cases with multiple slashes
                    : "postgres";

            return PgVectorEmbeddingStore.builder()
                    .host(host)
                    .port(port)
                    .database(database)
                    .user(dbUser)
                    .password(dbPassword)
                    .table("embeddings")
                    .dimension(768)
                    .build();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Malformed JDBC URL: " + dbUrl, e);
        }
    }
}