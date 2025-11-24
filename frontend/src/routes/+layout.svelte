<script lang="ts">
	import '../app.css';
	import { injectSpeedInsights } from '@vercel/speed-insights/sveltekit';

	injectSpeedInsights();

	let { children } = $props();
</script>

<svelte:head>
	<link rel="icon" href="/favicon.ico" />
</svelte:head>

{@render children()}
