<template>
  <a-layout class="basic-layout">
    <GlobalHeader />
    <a-layout-content class="main-content">
      <div :class="isChatPage ? 'content-shell chat-full-bleed' : 'content-shell'">
        <router-view />
      </div>
    </a-layout-content>
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isChatPage = computed(() => route.path.startsWith('/app/chat/'))
</script>

<style scoped>
.basic-layout {
  min-height: 100vh;
  background: transparent;
}

.main-content {
  width: 100%;
  padding: var(--space-6);
  margin: 0 auto;
}

.content-shell {
  width: min(1280px, 100%);
  margin: 0 auto;
  padding: var(--space-2);
}

.chat-full-bleed {
  width: 100%;
  max-width: none;
  padding: 0;
}
</style>
