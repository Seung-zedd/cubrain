export interface SseEvent {
  status: string;
  progress: number;
  results?: unknown;
  message?: string;
  metadata?: Record<string, unknown>;
}

export interface SseHandlers {
  onInit?: (data: SseEvent) => void;
  onProgress?: (data: SseEvent) => void;
  onComplete?: (data: SseEvent) => void;
  onError?: (error: unknown) => void;
}

export function subscribeToJob(
  jobId: string,
  handlers: SseHandlers,
  token?: string
) {
  const controller = new AbortController();
  const signal = controller.signal;

  const headers: HeadersInit = {
    Accept: "text/event-stream",
  };

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  const url = `/api/v1/sse/subscribe/${jobId}`;

  async function connect() {
    try {
      const response = await fetch(url, {
        headers,
        signal,
      });

      if (!response.ok) {
        throw new Error(`SSE connection failed: ${response.statusText}`);
      }

      const reader = response.body?.getReader();
      if (!reader) {
        throw new Error("Failed to get reader from response body");
      }

      const decoder = new TextDecoder();
      let buffer = "";

      while (true) {
        const { done, value } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() || "";

        let currentEvent = "";

        for (const line of lines) {
          if (line.startsWith("event:")) {
            currentEvent = line.replace("event:", "").trim();
          } else if (line.startsWith("data:")) {
            const dataStr = line.replace("data:", "").trim();
            try {
              const data = JSON.parse(dataStr) as SseEvent;
              handleEvent(currentEvent, data);
            } catch (e) {
              console.error("Failed to parse SSE data:", dataStr, e);
            }
            currentEvent = "";
          }
        }
      }
    } catch (error: unknown) {
      if (error instanceof Error && error.name === "AbortError") {
        return;
      }
      console.error("SSE Connection Error:", error);
      handlers.onError?.(error);

      // Simple auto-reconnect logic (only if not aborted)
      if (!signal.aborted) {
        setTimeout(connect, 3000);
      }
    }
  }

  function handleEvent(event: string, data: SseEvent) {
    switch (event) {
      case "INIT":
        handlers.onInit?.(data);
        break;
      case "PROGRESS":
        handlers.onProgress?.(data);
        break;
      case "COMPLETE":
        handlers.onComplete?.(data);
        break;
      case "ERROR":
        handlers.onError?.(data);
        break;
    }
  }

  connect();

  return () => {
    controller.abort();
  };
}
