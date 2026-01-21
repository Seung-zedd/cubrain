import { browser } from "$app/environment";
import { afterNavigate } from "$app/navigation";

/**
 * Tracks a custom event in Hotjar.
 * @param eventName - The name of the event to track.
 * @param props - Optional properties associated with the event.
 */
export function trackEvent(eventName: string, props?: Record<string, unknown>) {
  if (browser && window.hj) {
    window.hj("event", eventName, props);
    if (import.meta.env.DEV) {
      console.log(`[Telemetry] Event: ${eventName}`, props);
    }
  }
}

/**
 * Initializes telemetry tracking for SPA navigation.
 * Should be called in the root layout's onMount or script tag.
 */
export function initTelemetry() {
  if (browser) {
    afterNavigate(({ to }) => {
      if (window.hj && to) {
        // Track page view on navigation
        window.hj("stateChange", to.url.pathname);
        if (import.meta.env.DEV) {
          console.log(`[Telemetry] Page View: ${to.url.pathname}`);
        }
      }
    });
  }
}
