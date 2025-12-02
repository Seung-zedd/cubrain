<script>
    import { fade, fly } from 'svelte/transition';
    import { API_BASE_URL } from '$lib/config';

    let showButton = false;
    let btnX = 0;
    let btnY = 0;
    let selectedText = '';
    let isLoading = false;
    let flashcard = null;
    let currentContext = null;
    let demoContainer; // Reference to the container
    let errorMessage = '';
    let errorTimeout;

    const MAX_LOCAL_CONTEXT_LENGTH = 2000;
    const MAX_GLOBAL_CONTEXT_LENGTH = 4000;
    const WORD_BOUNDARY_THRESHOLD = 0.9;

    // ==================== HELPER FUNCTIONS ====================

    function getNearestElement(node) {
        return node.nodeType === Node.ELEMENT_NODE ? node : node.parentElement;
    }

    function getSemanticContext(element) {
        const semanticTags = ['P', 'DIV', 'ARTICLE', 'SECTION', 'LI', 'TD', 'BLOCKQUOTE', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6'];
        let current = element;
        while (current && !semanticTags.includes(current.tagName)) {
            current = current.parentElement;
        }
        return current || element;
    }

    function truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text || '';
        const truncated = text.substring(0, maxLength);
        const lastSpaceIndex = truncated.lastIndexOf(' ');
        if (lastSpaceIndex > maxLength * WORD_BOUNDARY_THRESHOLD) {
            return truncated.substring(0, lastSpaceIndex) + '...';
        }
        return truncated + '...';
    }

    function captureContext(range, text) {
        const commonAncestor = range.commonAncestorContainer;
        const ancestorElement = getNearestElement(commonAncestor);

        if (!ancestorElement) return null;

        const semanticContainer = getSemanticContext(ancestorElement);
        // Ensure we are still within our demo container
        if (!demoContainer.contains(semanticContainer)) {
             return null;
        }

        const localContext = (semanticContainer.textContent || semanticContainer.innerText || '').trim();
        const globalContext = (demoContainer.textContent || demoContainer.innerText || '').trim();

        return {
            selection: text,
            localContext: truncateText(localContext, MAX_LOCAL_CONTEXT_LENGTH),
            globalContext: truncateText(globalContext, MAX_GLOBAL_CONTEXT_LENGTH)
        };
    }

    // ==================== EVENT HANDLERS ====================

    function handleMouseUp() {
        const selection = window.getSelection();
        if (selection.isCollapsed) {
            hideButton();
            return;
        }

        const text = selection.toString().trim();
        if (!text) return;

        // Check if selection is inside our demo container
        if (!demoContainer.contains(selection.anchorNode)) {
            hideButton();
            return;
        }

        const range = selection.getRangeAt(0);
        const rect = range.getBoundingClientRect();
        const containerRect = demoContainer.getBoundingClientRect();

        currentContext = captureContext(range, text);
        if (!currentContext) return;

        if (import.meta.env.LOCAL) {
            console.log("🔍 Captured Context Data:", {
                Layer1_Selection: currentContext.selection,
                Layer2_LocalContext: currentContext.localContext,
                Layer3_GlobalContext: currentContext.globalContext
            });
        }

        selectedText = text;
        
        // Calculate position relative to the container
        // We use the container's rect to offset the button's position
        btnX = rect.left - containerRect.left + (rect.width / 2);
        btnY = Math.max(0, rect.top - containerRect.top - 50); // 50px above, clamped to 0
        
        showButton = true;
    }

    function handleMouseDown(event) {
        // Hide if clicking outside the button
        if (event.target.closest('.cubrain-float-btn')) return;
        hideButton();
    }

    function hideButton() {
        showButton = false;
    }

    function showError(message) {
        errorMessage = message;
        if (errorTimeout) clearTimeout(errorTimeout);
        errorTimeout = setTimeout(() => {
            errorMessage = '';
        }, 5000);
    }

    async function createFlashcard() {
        if (!selectedText) return;

        isLoading = true;
        flashcard = null;

        try {
            // Use environment variable if set, otherwise default based on mode
            // Use shared API URL configuration
            const apiBaseUrl = API_BASE_URL;

            const response = await fetch(`${apiBaseUrl}/api/cards/generate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(currentContext)
            });

            if (response.ok) {
                flashcard = await response.json();
                // Clear selection
                window.getSelection().removeAllRanges();
                showButton = false;
            } else {
                let errorMsg = 'Failed to generate flashcard. Please try again.';
                try {
                    const errorData = await response.json();
                    if (errorData && errorData.error) {
                        errorMsg = errorData.error;
                    }
                } catch (e) {
                    // Ignore JSON parse errors
                }
                switch (response.status) {
                    case 400:
                        showError('Bad request: ' + errorMsg);
                        break;
                    case 401:
                        showError('Unauthorized: Please log in.');
                        break;
                    case 403:
                        showError('Forbidden: You do not have permission to perform this action.');
                        break;
                    case 404:
                        showError('Not found: The requested resource could not be found.');
                        break;
                    case 500:
                        showError('Server error: Please try again later.');
                        break;
                    default:
                        showError(errorMsg);
                }
            }
        } catch (error) {
            console.error('Error:', error);
            showError('Network error: Could not connect to server.');
        } finally {
            isLoading = false;
        }
    }
</script>

<div 
    class="demo-wrapper glass-panel" 
    bind:this={demoContainer}
    on:mouseup={handleMouseUp}
    on:mousedown={handleMouseDown}
    role="presentation"
>
    <div class="demo-header">
        <span class="badge">Interactive Demo</span>
        <h3>Try it yourself</h3>
    </div>
    
    <div class="demo-content">
        <p>
            Artificial Intelligence (AI) is the simulation of human intelligence processes by machines, especially computer systems. These processes include learning (the acquisition of information and rules for using the information), reasoning (using rules to reach approximate or definite conclusions), and self-correction.
        </p>
        <p>
            Particular applications of AI include expert systems, speech recognition, and machine vision. AI can be categorized as either weak or strong. Weak AI, also known as narrow AI, is an AI system that is designed and trained for a particular task. Strong AI, also known as artificial general intelligence, is an AI system with generalized human cognitive abilities.
        </p>
        <p class="instruction-text">
            <span role="img" aria-label="Pointing up">👆</span> <strong>Highlight any text above to generate a flashcard!</strong>
        </p>
    </div>

    {#if showButton}
        <button 
            class="cubrain-float-btn" 
            style="left: {btnX}px; top: {btnY}px;"
            on:click={createFlashcard}
            disabled={isLoading}
            transition:fade={{ duration: 150 }}
        >
            {#if isLoading}
                <span class="spinner"></span> Generating...
            {:else}
                ⚡ Make Flashcard
            {/if}
        </button>
    {/if}

    {#if flashcard}
        <div class="result-card" transition:fly={{ y: 20, duration: 300 }}>
            <div class="card-header">
                <span class="icon">✨</span> Generated Flashcard
                <button class="close-btn" on:click={() => flashcard = null}>×</button>
            </div>
            <div class="card-body">
                <div class="qa-pair">
                    <div class="label">Q</div>
                    <div class="text">{flashcard.question}</div>
                </div>
                <div class="qa-pair">
                    <div class="label">A</div>
                    <div class="text">{flashcard.answer}</div>
                </div>
            </div>
        </div>
    {/if}

    {#if errorMessage}
        <div class="error-toast" transition:fade={{ duration: 200 }}>
            <span class="icon">⚠️</span>
            {errorMessage}
            <button class="close-btn-small" on:click={() => errorMessage = ''}>×</button>
        </div>
    {/if}
</div>

<style>
    .demo-wrapper {
        position: relative; /* Critical for absolute positioning of button */
        padding: 2rem;
        max-width: 800px;
        margin: 2rem auto;
        border: 1px solid var(--glass-border);
        background: rgba(0, 0, 0, 0.3);
    }

    .demo-header {
        display: flex;
        align-items: center;
        gap: 1rem;
        margin-bottom: 1.5rem;
    }

    .badge {
        background: var(--primary-gradient);
        color: white;
        padding: 0.25rem 0.75rem;
        border-radius: 1rem;
        font-size: 0.8rem;
        font-weight: bold;
    }

    .demo-content p {
        margin-bottom: 1rem;
        line-height: 1.6;
        color: #e5e7eb;
    }

    .instruction-text {
        margin-top: 2rem;
        color: #fbbf24 !important; /* Gold color */
        text-align: center;
        font-size: 0.9rem;
    }

    /* Floating Button */
    .cubrain-float-btn {
        position: absolute;
        transform: translateX(-50%);
        background: #fbbf24; /* Gold */
        color: #000;
        border: none;
        padding: 0.5rem 1rem;
        border-radius: 2rem;
        font-weight: bold;
        cursor: pointer;
        box-shadow: 0 4px 15px rgba(251, 191, 36, 0.3);
        z-index: 100;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        transition: transform 0.2s, background 0.2s;
        white-space: nowrap;
    }

    .cubrain-float-btn:hover {
        transform: translateX(-50%) scale(1.05);
        background: #f59e0b;
    }

    /* Result Card */
    .result-card {
        margin-top: 2rem;
        background: #1a1a1a;
        border: 1px solid #fbbf24;
        border-radius: 0.75rem;
        overflow: hidden;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
    }

    .card-header {
        background: rgba(251, 191, 36, 0.1);
        padding: 0.75rem 1rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid rgba(251, 191, 36, 0.2);
        color: #fbbf24;
        font-weight: bold;
    }

    .close-btn {
        background: none;
        border: none;
        color: #fbbf24;
        font-size: 1.5rem;
        cursor: pointer;
        padding: 0;
        line-height: 1;
    }

    .card-body {
        padding: 1.5rem;
    }

    .qa-pair {
        display: flex;
        gap: 1rem;
        margin-bottom: 1rem;
    }

    .qa-pair:last-child {
        margin-bottom: 0;
    }

    .label {
        background: #333;
        color: #fbbf24;
        width: 2rem;
        height: 2rem;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-weight: bold;
        flex-shrink: 0;
    }

    .text {
        color: #fff;
        line-height: 1.5;
        padding-top: 0.2rem;
    }

    .spinner {
        width: 1rem;
        height: 1rem;
        border: 2px solid #000;
        border-top-color: transparent;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        display: inline-block;
    }

    @keyframes spin {
        to { transform: rotate(360deg); }
    }

    .error-toast {
        position: absolute;
        bottom: 1rem;
        left: 50%;
        transform: translateX(-50%);
        background: rgba(220, 38, 38, 0.9);
        color: white;
        padding: 0.75rem 1rem;
        border-radius: 0.5rem;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
        z-index: 200;
        width: 90%;
        max-width: 400px;
        font-size: 0.9rem;
        border: 1px solid rgba(255, 255, 255, 0.1);
    }

    .close-btn-small {
        background: none;
        border: none;
        color: white;
        margin-left: auto;
        cursor: pointer;
        font-size: 1.2rem;
        padding: 0 0.25rem;
        opacity: 0.8;
    }
    
    .close-btn-small:hover {
        opacity: 1;
    }
</style>
