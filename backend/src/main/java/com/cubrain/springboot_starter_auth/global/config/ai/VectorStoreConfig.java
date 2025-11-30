package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/**
 * Configuration class for setting up the EmbeddingStore using PgVector.
 * <p>
 * This class manually parses the JDBC URL from Spring's datasource properties to extract
 * the host, port, and database name, which are required by the PgVectorEmbeddingStore builder.
 * Manual parsing is necessary because the PgVectorEmbeddingStore does not accept a JDBC URL
 * or a Spring DataSource directly.
 * <p>
 * <b>Limitations:</b>
 * <ul>
 *   <li>This parsing logic only supports standard JDBC URLs of the form {@code jdbc:postgresql://host:port/db}.</li>
 *   <li>JDBC URLs with query parameters (e.g., {@code ?sslmode=require}) or non-standard formats are not supported.</li>
 *   <li>If the URL format changes, this logic may break or extract incorrect values.</li>
 * </ul>
 * Consider improving this logic or using a more robust parsing library if advanced JDBC URL features are needed.
 */
public class VectorStoreConfig {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        // Parse host, port, database from JDBC URL (jdbc:postgresql://host:port/db)
        // Simple parsing logic for standard JDBC URL
        String cleanUrl = dbUrl.replace("jdbc:postgresql://", "");
        String[] hostPortDb = cleanUrl.split("/");
        String hostPort = hostPortDb[0];
        String database = hostPortDb.length > 1 ? hostPortDb[1] : "postgres";

        String host = "localhost";
        int port = 5432;

        if (hostPort.contains(":")) {
            String[] hp = hostPort.split(":");
            host = hp[0];
            try {
                port = Integer.parseInt(hp[1]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number in JDBC URL: " + hp[1], e);
            }
        } else {
            host = hostPort;
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
