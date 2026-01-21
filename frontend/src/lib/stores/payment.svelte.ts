import { authFetch } from "$lib/api";
import { IS_DEV_MODE } from "$lib/utils/env";

export interface LemonSqueezyConfig {
  productId: string;
  checkoutUrl: string;
  monthlyVariantId: string;
  yearlyVariantId: string;
}

class PaymentStore {
  config = $state<LemonSqueezyConfig | null>(null);
  isLoading = $state(false);
  error = $state<string | null>(null);

  async fetchConfig() {
    // Return cached config if available
    if (this.config) return this.config;

    this.isLoading = true;
    this.error = null;

    try {
      const response = await authFetch("/api/v1/payments/lemonsqueezy/config");
      if (!response.ok) {
        throw new Error("Failed to fetch payment configuration");
      }

      const data = await response.json();
      this.config = data;

      if (IS_DEV_MODE) {
        console.log("💳 [PaymentStore] Lemon Squeezy config loaded:", data);
      }

      return data;
    } catch (err) {
      const message = err instanceof Error ? err.message : "Unknown error";
      this.error = message;
      if (IS_DEV_MODE) {
        console.error("❌ [PaymentStore] Error fetching config:", message);
      }
      return null;
    } finally {
      this.isLoading = false;
    }
  }
}

export const paymentStore = new PaymentStore();
