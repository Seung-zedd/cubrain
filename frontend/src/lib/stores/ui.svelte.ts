/**
 * Svelte 5 UI State Store
 * Manages global UI states like study mode and transitions.
 */
class UIState {
  isStudyMode = $state(false);
  isTransitioning = $state(false);

  setStudyMode(value: boolean) {
    this.isStudyMode = value;
  }

  setTransitioning(value: boolean) {
    this.isTransitioning = value;
  }
}

export const uiState = new UIState();
