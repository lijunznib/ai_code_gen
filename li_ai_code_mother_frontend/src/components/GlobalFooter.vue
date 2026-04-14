<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        Crafted by
        <a
          href="https://github.com/lijunznib"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          LI NoCode
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: transparent;
  text-align: center;
  padding: var(--space-8) var(--space-6) var(--space-6);
  margin-top: var(--space-10);
}

.footer-content {
  width: min(1280px, 100%);
  margin: 0 auto;
  padding: var(--space-5);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-default);
  background: color-mix(in srgb, var(--color-bg-elevated) 90%, transparent);
  box-shadow: var(--shadow-sm);
}

.copyright {
  margin: 0;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
}

.author-link {
  margin-left: var(--space-1);
  color: var(--color-text-secondary);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease;
}

.author-link:hover {
  color: var(--color-primary);
}
</style>
