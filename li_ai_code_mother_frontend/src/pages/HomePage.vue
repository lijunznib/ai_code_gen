<script setup lang="ts">
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useRouter } from 'vue-router'
import {
  AppstoreOutlined,
  RocketOutlined,
  ThunderboltOutlined,
  CodeOutlined,
  CloudSyncOutlined,
  SafetyOutlined,
  ArrowRightOutlined,
} from '@ant-design/icons-vue'

interface Feature {
  title: string
  description: string
  icon: any
  color: string
}

const loginUserStore = useLoginUserStore()
const router = useRouter()

// 计算属性：是否已登录
const isLoggedIn = computed(() => !!loginUserStore.loginUser?.id)

// 计算属性：登录用户显示名称
const userDisplayName = computed(() => loginUserStore.loginUser?.userName ?? '访客')

// 计算属性：用户头像 URL
const userAvatar = computed(() => loginUserStore.loginUser?.userAvatar || undefined)

// 核心功能列表
const coreFeatures: Feature[] = [
  {
    title: '智能代码生成',
    description:
      '基于先进 AI 技术，理解您的需求描述，自动生成完整的前后端代码，支持 Vue、React、Spring Boot 等多种主流框架。',
    icon: AppstoreOutlined,
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  },
  {
    title: '快速部署上线',
    description:
      '一键部署到主流云平台，支持 Docker 容器化，自动配置 CI/CD 流水线，让您的应用快速稳定上线。',
    icon: RocketOutlined,
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  },
  {
    title: '高性能架构',
    description: '采用最新技术栈和最佳实践，代码结构清晰，性能卓越，轻松应对高并发场景。',
    icon: ThunderboltOutlined,
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  },
  {
    title: '可视化编辑',
    description: '拖拽式界面设计，实时预览效果，无需编写代码即可完成页面搭建和交互设计。',
    icon: CodeOutlined,
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
  },
  {
    title: '云端同步',
    description: '项目自动保存到云端，支持多设备协作，随时随地继续您的工作。',
    icon: CloudSyncOutlined,
    color: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  },
  {
    title: '安全可靠',
    description: '企业级安全防护，数据加密存储，权限精细管控，让您的项目安全无忧。',
    icon: SafetyOutlined,
    color: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
  },
]

// 使用流程
const steps = [
  {
    title: '描述需求',
    description: '用自然语言描述您想要的应用功能',
    number: '01',
  },
  {
    title: 'AI 生成',
    description: 'AI 智能分析并生成完整代码',
    number: '02',
  },
  {
    title: '预览调整',
    description: '实时预览效果，随时调整优化',
    number: '03',
  },
  {
    title: '部署发布',
    description: '一键部署到生产环境',
    number: '04',
  },
]

// 开始使用
const handleGetStarted = () => {
  if (isLoggedIn.value) {
    router.push('/features')
  } else {
    router.push('/user/register')
  }
}
</script>

<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-text">
          <h1 class="hero-title">
            <span class="gradient-text">不写一行代码</span>
            <br />
            生成完整应用
          </h1>
          <p class="hero-description">
            Lij AI 应用生成平台，基于先进的人工智能技术，
            <br class="hidden-mobile" />
            让您无需编写代码，即可快速构建专业级应用
          </p>
          <div class="hero-actions">
            <a-button type="primary" size="large" class="btn-get-started" @click="handleGetStarted">
              {{ isLoggedIn ? '开始使用' : '免费注册' }}
              <ArrowRightOutlined style="margin-left: 8px" />
            </a-button>
            <a-button size="large" type="primary" class="btn-learn-more" href="#features">
              了解更多
            </a-button>
          </div>
          <div v-if="isLoggedIn" class="user-welcome">
            <a-space>
              <a-avatar :src="userAvatar" :size="40" />
              <div class="welcome-info">
                <p class="welcome-text">欢迎回来</p>
                <p class="user-name">{{ userDisplayName }}</p>
              </div>
            </a-space>
          </div>
        </div>
        <div class="hero-image">
          <div class="code-block">
            <div class="code-header">
              <span class="dot red"></span>
              <span class="dot yellow"></span>
              <span class="dot green"></span>
            </div>
            <pre class="code-content">
&lt;App&gt;
  &lt;Header /&gt;
  &lt;Main&gt;
    &lt;Feature name="AI 生成" /&gt;
    &lt;Feature name="快速部署" /&gt;
  &lt;/Main&gt;
  &lt;Footer /&gt;
&lt;/App&gt;</pre
            >
          </div>
        </div>
      </div>
      <!-- 波浪分隔 -->
      <div class="hero-wave">
        <svg viewBox="0 0 1440 120" preserveAspectRatio="none">
          <path d="M0,64 C480,160 960,-32 1440,64 L1440,120 L0,120 Z" fill="#fff"></path>
        </svg>
      </div>
    </section>

    <!-- 核心功能区域 -->
    <section id="features" class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">核心功能</h2>
          <p class="section-subtitle">强大的功能集合，助您快速实现创意</p>
        </div>
        <a-row :gutter="[24, 24]">
          <a-col v-for="(feature, index) in coreFeatures" :key="index" :xs="24" :sm="12" :lg="8">
            <a-card class="feature-card" hoverable>
              <template #cover>
                <div class="feature-icon-wrapper" :style="{ background: feature.color }">
                  <component :is="feature.icon" class="feature-icon" />
                </div>
              </template>
              <a-card-meta>
                <template #title>
                  <span class="feature-title">{{ feature.title }}</span>
                </template>
                <template #description>
                  <p class="feature-description">{{ feature.description }}</p>
                </template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </section>

    <!-- 使用流程区域 -->
    <section class="workflow-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">如何使用</h2>
          <p class="section-subtitle">简单的四步，开启您的 AI 创作之旅</p>
        </div>
        <a-row :gutter="[16, 16]" class="workflow-steps">
          <a-col v-for="(step, index) in steps" :key="index" :xs="24" :sm="12" :md="6">
            <div class="step-item">
              <div class="step-number">{{ step.number }}</div>
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-description">{{ step.description }}</p>
            </div>
          </a-col>
        </a-row>
      </div>
    </section>

    <!-- CTA 行动号召区域 -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-content">
          <h2 class="cta-title">准备好开始了吗？</h2>
          <p class="cta-description">立即体验 AI 驱动的应用开发新方式</p>
          <a-button type="primary" size="large" class="btn-cta" @click="handleGetStarted">
            {{ isLoggedIn ? '创建我的第一个应用' : '立即免费注册' }}
          </a-button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.home-page {
  overflow: hidden;
}

/* Hero 区域 */
.hero-section {
  position: relative;
  min-height: 90vh;
  background: linear-gradient(135deg, #1a1c2e 0%, #2d1b4e 50%, #1a1c2e 100%);
  display: flex;
  flex-direction: column;
}

.hero-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 24px 60px;
  gap: 60px;
}

.hero-text {
  flex: 1;
  max-width: 600px;
}

.hero-title {
  font-size: 56px;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 24px;
  color: #fff;
}

.gradient-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-description {
  font-size: 18px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
}

.btn-get-started {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.btn-get-started:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.btn-learn-more {
  height: 48px;
  padding: 0 32px;
  font-size: 16px;
  border-radius: 24px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #fff;
  background: transparent;
}

.btn-learn-more:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: #fff;
}

.user-welcome {
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.welcome-info {
  text-align: left;
}

.welcome-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.user-name {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.hero-image {
  flex: 1;
  max-width: 500px;
  display: flex;
  justify-content: center;
}

.code-block {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 24px;
  backdrop-filter: blur(10px);
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.3);
}

.code-header {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.code-header .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.code-header .dot.red {
  background: #ff5f56;
}

.code-header .dot.yellow {
  background: #ffbd2e;
}

.code-header .dot.green {
  background: #27ca40;
}

.code-content {
  color: #a8b5d2;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}

.hero-wave {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
}

.hero-wave svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* 功能区域 */
.features-section {
  padding: 100px 24px;
  background: #fff;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
}

.section-header {
  text-align: center;
  margin-bottom: 60px;
}

.section-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1c2e;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 16px;
  color: #666;
  max-width: 600px;
  margin: 0 auto;
}

.feature-card {
  height: 100%;
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);
}

.feature-icon-wrapper {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.feature-icon {
  font-size: 64px;
  color: #fff;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1c2e;
}

.feature-description {
  font-size: 14px;
  line-height: 1.8;
  color: #666;
  margin: 0;
}

/* 工作流程区域 */
.workflow-section {
  padding: 100px 24px;
  background: linear-gradient(180deg, #f8f9fa 0%, #fff 100%);
}

.workflow-steps {
  margin-top: 40px;
}

.step-item {
  text-align: center;
  padding: 40px 24px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
}

.step-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.1);
}

.step-number {
  display: inline-block;
  width: 56px;
  height: 56px;
  line-height: 56px;
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  margin-bottom: 20px;
}

.step-title {
  font-size: 20px;
  font-weight: 600;
  color: #1a1c2e;
  margin-bottom: 12px;
}

.step-description {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* CTA 区域 */
.cta-section {
  padding: 100px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.cta-content {
  text-align: center;
  max-width: 600px;
  margin: 0 auto;
}

.cta-title {
  font-size: 36px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16px;
}

.cta-description {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 32px;
}

.btn-cta {
  height: 56px;
  padding: 0 48px;
  font-size: 18px;
  font-weight: 600;
  border-radius: 28px;
  background: #fff;
  color: #667eea;
  border: none;
}

.btn-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

/* 响应式 */
@media (max-width: 992px) {
  .hero-content {
    flex-direction: column;
    text-align: center;
    padding-top: 40px;
  }

  .hero-text {
    max-width: 100%;
  }

  .hero-title {
    font-size: 40px;
  }

  .hero-description {
    font-size: 16px;
  }

  .hero-actions {
    justify-content: center;
    flex-wrap: wrap;
  }

  .hero-image {
    width: 100%;
    max-width: 400px;
  }

  .user-welcome {
    display: inline-block;
  }
}

@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }

  .section-title {
    font-size: 28px;
  }

  .cta-title {
    font-size: 28px;
  }

  .hidden-mobile {
    display: none;
  }

  .hero-actions {
    flex-direction: column;
    align-items: center;
  }

  .btn-get-started,
  .btn-learn-more,
  .btn-cta {
    //width: 100%;
    //max-width: 280px;
    height: 48px;
    padding: 0 32px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: none;
  }
}
</style>
