package com.cubrain.springboot_starter_auth.domain.pdf;

import com.cubrain.springboot_starter_auth.domain.job.JobManager;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class PdfIngestionService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final JobManager jobManager;
    private final int chunkSize;
    private final int chunkOverlap;

    public PdfIngestionService(
            EmbeddingModel embeddingModel,
            EmbeddingStore<TextSegment> embeddingStore,
            JobManager jobManager,
            @Value("${langchain4j.document-splitter.chunk-size}") int chunkSize,
            @Value("${langchain4j.document-splitter.chunk-overlap}") int chunkOverlap) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.jobManager = jobManager;
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
    }

    public String ingestPdfAsync(MultipartFile file) {
        String jobId = jobManager.createJob();

        CompletableFuture.runAsync(() -> {
            try {
                ingestPdf(file, jobId);
                jobManager.completeJob(jobId);
            } catch (Exception e) {
                log.error("Async ingestion failed for job: {}", jobId, e);
                jobManager.failJob(jobId);
            }
        });

        return jobId;
    }

    public void ingestPdf(MultipartFile file) {
        ingestPdf(file, null);
    }
    
    public void ingestPdf(MultipartFile file, String jobId) {
        log.info("Starting PDF ingestion for file: {}", file.getOriginalFilename());

        try (InputStream inputStream = file.getInputStream()) {
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            Document document = parser.parse(inputStream);

            // Add source filename to metadata
            document.metadata().put("file_name", file.getOriginalFilename());

            log.info("Parsed PDF. Pages: {}, Metadata: {}", document.metadata().getString("page_count"),
                    document.metadata().toMap());

            DocumentSplitter splitter = DocumentSplitters.recursive(chunkSize, chunkOverlap,
                    new OpenAiTokenizer("gpt-4"));
            List<TextSegment> segments = splitter.split(document);
            int total = segments.size();
            log.info("Split PDF into {} segments", total);

            for (int i = 0; i < total; i++) {
                TextSegment segment = segments.get(i);
                Embedding embedding = embeddingModel.embed(segment).content();
                embeddingStore.add(embedding, segment);

                if (jobId != null) {
                    int progress = (int) (((double) (i + 1) / total) * 100);
                    jobManager.updateProgress(jobId, progress);
                }
            }

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
