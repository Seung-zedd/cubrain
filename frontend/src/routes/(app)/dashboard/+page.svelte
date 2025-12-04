<div class="space-y-6">
  <div>
    <h1 class="text-3xl font-bold text-white tracking-tight">Dashboard</h1>
    <p class="text-zinc-400 mt-2">
      Welcome back, Student! Here's what's happening with your learning.
    </p>
  </div>

  <div class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
    <!-- Placeholder Cards -->
    {#each Array(3) as _, i}
      <div
        class="rounded-xl border border-zinc-800 bg-zinc-900/50 p-6 shadow-sm hover:border-amber-500/30 transition-colors"
      >
        <div
          class="h-10 w-10 rounded-lg bg-amber-500/10 flex items-center justify-center mb-4"
        >
          <span class="text-xl">📚</span>
        </div>
        <h3 class="text-lg font-semibold text-zinc-100">Recent Set {i + 1}</h3>
        <p class="text-sm text-zinc-400 mt-2">Last studied 2 hours ago</p>
      </div>
    {/each}
  </div>
</div>
