// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces
declare global {
  namespace App {
    // interface Error {}
    // interface Locals {}
    // interface PageData {}
    // interface PageState {}
    // interface Platform {}
  }
}

declare global {
  interface Window {
    LemonSqueezy: {
      Url: {
        Open: (url: string) => void;
      };
      Setup: (options: {
        eventHandler: (event: { event: string; data: unknown }) => void;
      }) => void;
      Refresh: () => void;
    };
    createLemonSqueezy?: () => void;
    hj?: (command: string, ...args: unknown[]) => void;
  }
}

export {};
