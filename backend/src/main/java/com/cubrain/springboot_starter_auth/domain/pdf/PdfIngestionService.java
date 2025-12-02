package com.cubrain.springboot_starter_auth.domain.pdf;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
// @Service
public class PdfIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final int chunkSize;
    private final int chunkOverlap;

    public PdfIngestionService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            @Value("${langchain4j.document-splitter.chunk-size}") int chunkSize,
            @Value("${langchain4j.document-splitter.chunk-overlap}") int chunkOverlap) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public void ingestPdf(MultipartFile file) {
        log.info("Starting PDF ingestion for file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);

            // Add source filename to metadata
            document.metadata().put("file_name", file.getOriginalFilename());

            log.info("Parsed PDF. Pages: {}, Metadata: {}", document.metadata().getString("page_count"),
                    document.metadata().toMap());

            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);

            log.info("Successfully ingested PDF: {}", file.getOriginalFilename());

        } catch (IOException e) {
            log.error("Failed to ingest PDF", e);
            throw new PdfIngestionException("Failed to process PDF file", e);
        }
    }

    public static class PdfIngestionException extends RuntimeException {
        public PdfIngestionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
