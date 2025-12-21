# Flashcard Generation Verification Plan

This document outlines the test scenarios for verifying the "PDF to Q&A Flashcards" feature, specifically focusing on the **User Tier** logic (Guest vs. Free User).

## 🔌 API Endpoint
- **URL**: `http://localhost:8080/api/cards/from-pdf`
- **Method**: `POST`
- **Content-Type**: `multipart/form-data`

---

## 🧪 Test Scenarios

### Scenario 1: Guest User (The "3-Page Limit" Test)
**Goal**: Verify that a Guest user ONLY gets flashcards from the first 3 pages.

1.  **Prepare a Test PDF**:
    -   Use a PDF that has at least 5 pages.
    -   **Page 1**: Highlight a sentence (e.g., "Java is an object-oriented language.").
    -   **Page 3**: Highlight a sentence (e.g., "Spring Boot makes it easy to create stand-alone applications.").
    -   **Page 5**: Highlight a sentence (e.g., "Docker containers wrap up software and its dependencies.").
2.  **Send Request (Apidog)**:
    -   **File**: Upload the PDF prepared above.
    -   **Param**: `userTier` = `GUEST`
3.  **Expected Result**:
    -   **Status**: `200 OK`
    -   **Response**: JSON Array with **2 items**.
        -   ✅ Flashcard from Page 1.
        -   ✅ Flashcard from Page 3.
        -   ❌ **NO** Flashcard from Page 5 (it should be filtered out).

### Scenario 2: Default Behavior (Implicit Guest)
**Goal**: Verify that if no `userTier` is provided, it defaults to GUEST.

1.  **Send Request**:
    -   **File**: Same PDF as Scenario 1.
    -   **Param**: **DO NOT** send `userTier` (leave it empty).
2.  **Expected Result**:
    -   **Status**: `200 OK`
    -   **Response**: Same as Scenario 1 (Only Page 1 & 3 processed).

### Scenario 3: Free User (No Page Limit)
**Goal**: Verify that a logged-in user (Free Tier) gets flashcards from ALL pages.

1.  **Send Request**:
    -   **File**: Same PDF as Scenario 1.
    -   **Param**: `userTier` = `FREE_USER`
2.  **Expected Result**:
    -   **Status**: `200 OK`
    -   **Response**: JSON Array with **3 items**.
        -   ✅ Flashcard from Page 1.
        -   ✅ Flashcard from Page 3.
        -   ✅ Flashcard from Page 5.

### Scenario 4: Legacy Demo (Single Selection)
**Goal**: Verify the landing page demo still works.

-   **URL**: `http://localhost:8080/api/cards/generate`
-   **Method**: `POST`
-   **Body (JSON)**:
    ```json
    {
      "selection": "Artificial Intelligence is simulating human intelligence.",
      "localContext": "AI is a broad field.",
      "globalContext": "Chapter 1: Intro to AI"
    }
    ```
-   **Expected Result**:
    -   **Status**: `200 OK`
    -   **Response**: A single JSON object (not an array).
    ```json
    {
      "question": "What is the core definition of Artificial Intelligence?",
      "answer": "Simulating human intelligence."
    }
    ```

---

## 🐞 Troubleshooting
-   **500 Error?**: Check the backend logs (`Debug: CubrainApplication` terminal).
-   **Empty List `[]`?**:
    -   Did you actually highlight text in the PDF? (Note: "Comments" or "Sticky Notes" are NOT highlights).
    -   Is the PDF text readable? (Scanned images without OCR won't work).
