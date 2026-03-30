<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/layout/GlobalHeader.vue'
import { Layout } from 'ant-design-vue'

const { Header, Content, Footer } = Layout
</script>

<template>
  <Layout class="basic-layout">
    <Header class="header">
      <GlobalHeader />
    </Header>
    <Content class="content">
      <RouterView />
    </Content>
    <Footer class="footer">
      <span> LI AI </span>
    </Footer>
  </Layout>
</template>

<style scoped>
.basic-layout {
  min-height: 100vh;
}

.header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.content {
  margin: 24px 16px;
  padding: 24px;
  background: #fff;
  min-height: calc(100vh - 112px);
}

.footer {
  text-align: center;
  background: #f5f5f5;
}

@media (max-width: 768px) {
  .content {
    margin: 12px 8px;
    padding: 16px;
    min-height: calc(100vh - 96px);
  }
}
</style>
