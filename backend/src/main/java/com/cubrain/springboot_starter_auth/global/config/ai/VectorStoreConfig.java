package com.cubrain.springboot_starter_auth.global.config.ai;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            port = Integer.parseInt(hp[1]);
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
