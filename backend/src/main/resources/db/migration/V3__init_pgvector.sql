-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Create embeddings table
CREATE TABLE IF NOT EXISTS embeddings (
    id UUID PRIMARY KEY,
    embedding vector(768), -- Gemini 1.5 Flash/Pro embedding dimension is 768
    text TEXT,
    metadata JSONB
);

-- Create index for faster similarity search (IVFFlat or HNSW)
-- HNSW is generally better for performance/recall trade-off
-- NOTE: HNSW indexes are best created after data has been inserted into the table.
-- Creating the index on an empty table may not be optimal for performance.
-- If you bulk-load data after table creation, consider recreating this index afterwards for best results.
CREATE INDEX IF NOT EXISTS embedding_hnsw_idx ON embeddings USING hnsw (embedding vector_cosine_ops);
