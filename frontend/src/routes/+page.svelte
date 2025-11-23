<script>
    import heroImage from '$lib/assets/hero.png';

    let email = '';
    let status = 'idle'; // 'idle' | 'loading' | 'success' | 'error'
    let message = '';

    async function joinWaitlist() {
        if (!email || !email.includes('@')) {
            status = 'error';
            message = 'Please enter a valid email address.';
            return;
        }

        status = 'loading';
        message = '';

        try {
            const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
            const response = await fetch(`${apiBaseUrl}/api/waitlist`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email })
            });

            if (response.ok) {
                status = 'success';
                message = 'Thanks for joining! We’ll be in touch soon.';
                email = '';
            } else {
                const text = await response.text();
                status = 'error';
                try {
                    const errorData = JSON.parse(text);
                    message = errorData.message || errorData.details || 'Something went wrong. Please try again.';
                } catch (e) {
                    message = text || 'Something went wrong. Please try again.';
                }
            }
        } catch (err) {
            console.error(err);
            status = 'error';
            message = 'Failed to connect to the server. Please check your connection.';
        }
    }
</script>

<div class="landing-page">
    <nav class="glass-panel nav">
        <div class="logo">Cubrain</div>
        <div class="links">
            <a href="#features">Features</a>
            <a href="#waitlist" class="cta-button small">Get Early Access</a>
        </div>
    </nav>

    <header class="hero">
        <div class="hero-content">
            <h1 class="gradient-text">Turn Your PDFs into Flashcards in Seconds.</h1>
            <p class="subheadline">Zero hallucinations. Zero manual entry. <br> Every flashcard is backed by a direct link to the source text, turning your PDFs into an interactive knowledge base.</p>
            <div class="cta-group">
                <a href="#waitlist" class="cta-button primary">Join the Waitlist</a>
                <button class="cta-button secondary">Learn More</button>
            </div>
        </div>
        <div class="hero-image">
            <img src={heroImage} alt="Cubrain Interface" />
            <div class="glow"></div>
        </div>
    </header>

    <section id="features" class="features">
        <h2 class="section-title">Why Cubrain?</h2>
        <div class="feature-grid">
            <div class="feature-card glass-panel">
                <h3><span class="feature-icon">⚡</span> <span class="feature-title">Instant Capture</span></h3>
                <p>Highlight any text on the web and instantly turn it into a flashcard. No context switching.</p>
            </div>
            <div class="feature-card glass-panel">
                <h3><span class="feature-icon">🧠</span> <span class="feature-title">Context Aware</span></h3>
                <p>Cubrain understands the surrounding text to create better, more meaningful questions.</p>
            </div>
            <div class="feature-card glass-panel">
                <h3><span class="feature-icon">🔄</span> <span class="feature-title">Seamless Sync</span></h3>
                <p>Automatically syncs with Anki, Notion, and other tools you already use.</p>
            </div>
            <div class="feature-card glass-panel">
                <h3><span class="feature-icon">✅</span> <span class="feature-title">AI Grading</span></h3>
                <p>Get instant feedback on your answers. The AI acts as your personal tutor, explaining why you were right or wrong.</p>
            </div>
        </div>
    </section>

    <section id="waitlist" class="waitlist-section">
        <div class="glass-panel waitlist-container">
            <h2>Join the Waitlist</h2>
            <p>Be the first to experience the future of learning.</p>
            <form class="waitlist-form" onsubmit={(e) => { e.preventDefault(); joinWaitlist(); }}>
                <input 
                    type="email" 
                    placeholder="Enter your email" 
                    required 
                    bind:value={email} 
                    disabled={status === 'loading' || status === 'success'}
                />
                <button 
                    type="submit" 
                    class="cta-button primary" 
                    disabled={status === 'loading' || status === 'success'}
                >
                    {#if status === 'loading'}
                        Joining...
                    {:else if status === 'success'}
                        ✓ Joined!
                    {:else}
                        Join the Waitlist
                    {/if}
                </button>
            </form>
            {#if message}
                <p style="margin-top: 1rem; color: {status === 'success' ? '#4ade80' : '#f87171'};">
                    {message}
                </p>
            {/if}
        </div>
    </section>

    <footer class="footer">
        <p>&copy; 2025 Cubrain. All rights reserved.</p>
    </footer>
</div>

<style>
    :global(html) {
        scroll-behavior: smooth;
    }

    .landing-page {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
    }

    .nav {
        position: fixed;
        top: 1rem;
        left: 50%;
        transform: translateX(-50%);
        width: 90%;
        max-width: 1200px;
        padding: 1rem 2rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
        z-index: 100;
    }

    .logo {
        font-weight: 800;
        font-size: 1.5rem;
        background: var(--primary-gradient);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
    }

    .links {
        display: flex;
        gap: 2rem;
        align-items: center;
    }

    .hero {
        padding: 8rem 1rem 4rem;
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 4rem;
        max-width: 1200px;
        margin: 0 auto;
        align-items: center;
        min-height: 90vh;
    }

    .hero-content h1 {
        font-size: 3.5rem;
        line-height: 1.1;
        margin-bottom: 1.5rem;
        font-weight: 800;
    }

    .subheadline {
        font-size: 1.25rem;
        color: var(--text-muted);
        margin-bottom: 2.5rem;
        max-width: 500px;
    }

    .cta-group {
        display: flex;
        gap: 1rem;
    }

    .cta-button {
        padding: 0.75rem 1.5rem;
        border-radius: 0.5rem;
        font-weight: 600;
        transition: transform 0.2s, box-shadow 0.2s;
        border: none;
        text-decoration: none;
        display: inline-block;
    }

    .cta-button.small {
        padding: 0.5rem 1rem;
        font-size: 0.9rem;
        background: var(--primary-gradient);
        color: white;
    }

    .cta-button:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
    }

    .cta-button.primary {
        background: var(--primary-gradient);
        color: white;
    }

    .cta-button.secondary {
        background: rgba(255, 255, 255, 0.1);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.2);
    }

    .hero-image {
        position: relative;
    }

    .hero-image img {
        width: 100%;
        border-radius: 1rem;
        box-shadow: 0 20px 50px rgba(0,0,0,0.5);
        border: 1px solid var(--glass-border);
    }

    .glow {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 120%;
        height: 120%;
        background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, rgba(0,0,0,0) 70%);
        z-index: -1;
        pointer-events: none;
    }

    .features {
        padding: 4rem 1rem;
        max-width: 1200px;
        margin: 0 auto;
    }

    .section-title {
        text-align: center;
        font-size: 2.5rem;
        margin-bottom: 3rem;
    }

    .feature-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 2rem;
        max-width: 900px;
        margin: 0 auto;
    }

    .feature-card {
        padding: 2rem;
        height: 100%;
        display: flex;
        flex-direction: column;
    }

    .feature-card h3 {
        font-size: 1.5rem;
        margin-bottom: 1rem;
        width: fit-content;
    }

    .feature-title {
        background: var(--primary-gradient);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        color: transparent;
    }

    .feature-icon {
        color: #fbbf24;
        margin-right: 0.5rem;
    }

    .waitlist-section {
        padding: 4rem 1rem;
        display: flex;
        justify-content: center;
    }

    .waitlist-container {
        padding: 3rem;
        text-align: center;
        max-width: 600px;
        width: 100%;
    }

    .waitlist-container h2 {
        font-size: 2rem;
        margin-bottom: 1rem;
    }

    .waitlist-container p {
        color: var(--text-muted);
        margin-bottom: 2rem;
    }

    .waitlist-form {
        display: flex;
        gap: 1rem;
    }

    .waitlist-form input {
        flex: 1;
        padding: 0.75rem;
        border-radius: 0.5rem;
        border: 1px solid var(--glass-border);
        background: rgba(255, 255, 255, 0.05);
        color: white;
    }

    .footer {
        text-align: center;
        padding: 2rem;
        color: var(--text-muted);
        margin-top: auto;
    }

    @media (max-width: 768px) {
        .hero {
            grid-template-columns: 1fr;
            text-align: center;
            padding-top: 8rem;
        }

        .feature-grid {
            grid-template-columns: 1fr;
        }

        .hero-content h1 {
            font-size: 2.5rem;
        }

        .subheadline {
            margin: 0 auto 2rem;
        }

        .cta-group {
            justify-content: center;
        }

        .nav {
            width: 95%;
            padding: 1rem;
        }
        
        .links {
            display: none;
        }
    }
</style>
