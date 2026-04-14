<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <div class="hero-section">
        <div class="hero-badge">AI Website Builder</div>
        <h1 class="hero-title">AI 应用生成平台</h1>
        <p class="hero-description">一句话轻松创建网站应用</p>
      </div>

      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
        </div>
      </div>

      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <div class="section">
        <div class="section-header">
          <h2 class="section-title">我的作品</h2>
          <span class="section-meta">我的应用资产与草稿</span>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <div class="section">
        <div class="section-header">
          <h2 class="section-title">精选案例</h2>
          <span class="section-meta">优质示例与灵感参考</span>
        </div>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0 auto;
  min-height: calc(100vh - 72px);
  position: relative;
  color: var(--color-text-primary);
  overflow: clip;
}

#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background:
    radial-gradient(circle at 20% 10%, color-mix(in srgb, var(--color-primary) 14%, transparent), transparent 28%),
    radial-gradient(circle at 80% 0%, color-mix(in srgb, var(--color-success) 10%, transparent), transparent 26%),
    linear-gradient(180deg, color-mix(in srgb, var(--color-bg-subtle) 60%, transparent), transparent 55%);
  pointer-events: none;
  opacity: 0.9;
}

#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
    560px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
    color-mix(in srgb, var(--color-primary) 12%, transparent) 0%,
    color-mix(in srgb, var(--color-primary) 4%, transparent) 42%,
    transparent 78%
  );
  pointer-events: none;
  opacity: 0.8;
}

.container {
  max-width: 1240px;
  margin: 0 auto;
  padding: var(--space-3);
  position: relative;
  z-index: 2;
  box-sizing: border-box;
}

.hero-section {
  text-align: center;
  padding: var(--space-10) 0 var(--space-8);
  margin-bottom: var(--space-4);
  position: relative;
}

.hero-badge {
  display: inline-flex;
  padding: var(--space-1) var(--space-3);
  border-radius: 999px;
  font-size: var(--font-size-caption);
  color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-primary) 28%, transparent);
}

.hero-title {
  font-size: clamp(32px, 4vw, 50px);
  font-weight: 700;
  margin: var(--space-4) 0 var(--space-3);
  line-height: 1.15;
  color: var(--color-text-primary);
}

.hero-description {
  font-size: 18px;
  margin: 0;
  color: var(--color-text-secondary);
}

.input-section {
  position: relative;
  margin: 0 auto var(--space-6);
  max-width: 800px;
}

.prompt-input {
  border-radius: var(--radius-lg);
  font-size: 16px;
  padding: var(--space-6) 68px var(--space-6) var(--space-5);
  border: 1px solid var(--color-border-default);
  background: color-mix(in srgb, var(--color-bg-surface) 95%, transparent);
  box-shadow: var(--shadow-md);
  color: var(--color-text-primary);
  transition: border-color 0.2s ease;
}

:deep(.prompt-input.ant-input:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-primary) 20%, transparent);
}

.input-actions {
  position: absolute;
  bottom: var(--space-4);
  right: var(--space-4);
  display: flex;
  gap: var(--space-2);
  align-items: center;
}

.input-actions .ant-btn {
  width: 40px;
  height: 40px;
  border-radius: 999px;
}

.quick-actions {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  margin-bottom: var(--space-10);
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 999px;
  padding: var(--space-2) var(--space-4);
  height: auto;
  background: color-mix(in srgb, var(--color-bg-surface) 90%, transparent);
  border: 1px solid var(--color-border-default);
  color: var(--color-text-secondary);
  transition: all 0.2s ease;
}

.quick-actions .ant-btn:hover {
  background: color-mix(in srgb, var(--color-bg-surface) 96%, transparent);
  border-color: color-mix(in srgb, var(--color-primary) 45%, var(--color-border-default));
  color: var(--color-primary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.section {
  margin-bottom: var(--space-10);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--color-border-default);
  background: color-mix(in srgb, var(--color-bg-surface) 92%, transparent);
  box-shadow: var(--shadow-sm);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--space-6);
  gap: var(--space-3);
}

.section-meta {
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
}

.section-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-primary);
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: var(--space-2);
}

:deep(.ant-pagination .ant-pagination-item) {
  border-color: var(--color-border-default);
  background: color-mix(in srgb, var(--color-bg-surface) 90%, transparent);
}

:deep(.ant-pagination .ant-pagination-item-active) {
  border-color: var(--color-primary);
}

:deep(.ant-pagination .ant-pagination-item-active a) {
  color: var(--color-primary);
}

@media (max-width: 768px) {
  .hero-description {
    font-size: 16px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    justify-content: center;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
