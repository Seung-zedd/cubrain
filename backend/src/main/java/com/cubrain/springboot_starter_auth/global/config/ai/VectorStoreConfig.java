package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration class for setting up the EmbeddingStore using PgVector.
 * <p>
 * This class parses the JDBC URL from Spring's datasource properties using {@link java.net.URI}
 * to extract the host, port, and database name, which are required by the PgVectorEmbeddingStore builder.
 * Using {@code java.net.URI} ensures robust handling of JDBC URLs with query parameters.
 */
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
        // Parse host, port, database from JDBC URL using java.net.URI
        // This properly handles query parameters (e.g., jdbc:postgresql://host:port/db?ssl=true)
        String host = "localhost";
        int port = 5432;
        String database = "postgres";

        try {
            // Remove "jdbc:" prefix to get a valid URI
            String uriString = dbUrl.replaceFirst("^jdbc:", "");
            URI uri = new URI(uriString);

            if (uri.getHost() != null) {
                host = uri.getHost();
            }

            if (uri.getPort() != -1) {
                port = uri.getPort();
            }

            if (uri.getPath() != null && uri.getPath().length() > 1) {
                // Remove leading slash from path and extract only the first segment as database name
                String path = uri.getPath().substring(1);
                int slashIndex = path.indexOf('/');
                database = slashIndex != -1 ? path.substring(0, slashIndex) : path;
            }
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid JDBC URL format: " + dbUrl, e);
        }

        return PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .database(database)
                .user(dbUser)
                .password(dbPassword)
                .table("embeddings")
                .dimension(768) // Gemini 1.5 Flash/Pro
                .build();
    }
}
