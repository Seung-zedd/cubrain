import { IS_DEV_MODE } from "$lib/utils/env";

// 1. Preserve existing API logic (Convert to TS)
const envUrl = import.meta.env.VITE_API_BASE_URL;

export const API_BASE_URL = envUrl
  ? envUrl
  : IS_DEV_MODE
    ? "https://api-dev.cubrain.app"
    : "https://api.cubrain.app";

// 2. Add Launch Switch
// Toggle this to FALSE when the first 100 spots are filled.
export const IS_LAUNCH_SALE = true;
