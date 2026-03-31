<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, deleteMyApp, listGoodAppsByPage, listMyAppsByPage } from '@/api/appController'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const isLoggedIn = computed(() => !!loginUserStore.loginUser?.id)

const initPrompt = ref('')
const creating = ref(false)

const myApps = ref<API.App[]>([])
const goodApps = ref<API.App[]>([])

const myTotal = ref(0)
const goodTotal = ref(0)

const myAppName = ref('')
const myPagination = reactive({
  current: 1,
  pageSize: 12,
})

const goodAppName = ref('')
const goodPagination = reactive({
  current: 1,
  pageSize: 12,
})

const loadMyApps = async () => {
  try {
    const res = await listMyAppsByPage({
      pageNum: myPagination.current,
      pageSize: myPagination.pageSize,
      appName: myAppName.value || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myTotal.value = res.data.data.totalRow || 0
    } else {
      myApps.value = []
      myTotal.value = 0
      message.error(res.data.message || '获取我的应用失败')
    }
  } catch (e) {
    console.error(e)
    message.error('获取我的应用失败，请稍后重试')
  }
}

const handleMySearch = () => {
  myPagination.current = 1
  loadMyApps()
}

const loadGoodApps = async () => {
  try {
    const res = await listGoodAppsByPage({
      pageNum: goodPagination.current,
      pageSize: goodPagination.pageSize,
      appName: goodAppName.value || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records || []
      goodTotal.value = res.data.data.totalRow || 0
    } else {
      goodApps.value = []
      goodTotal.value = 0
      message.error(res.data.message || '获取精选应用失败')
    }
  } catch (e) {
    console.error(e)
    message.error('获取精选应用失败，请稍后重试')
  }
}

const handleGoodSearch = () => {
  goodPagination.current = 1
  loadGoodApps()
}

const handleCreate = async () => {
  const prompt = initPrompt.value.trim()
  if (!prompt) {
    message.warning('请先输入你的应用需求描述')
    return
  }
  if (creating.value) return

  try {
    creating.value = true
    const res = await addApp({
      initPrompt: prompt,
      // appName/cover 由后端根据 initPrompt 自动补全
      appName: undefined,
      cover: undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      const appId = String(res.data.data)
      router.push({
        path: `/app/chat/${appId}`,
        query: {
          autoGenerate: '1',
        },
      })
    } else {
      message.error(res.data.message || '创建应用失败')
    }
  } catch (e) {
    console.error(e)
    message.error('创建应用失败，请稍后重试')
  } finally {
    creating.value = false
  }
}

const onMyPageChange = (page: number, pageSize?: number) => {
  myPagination.current = page
  if (pageSize) myPagination.pageSize = pageSize
  loadMyApps()
}

const onGoodPageChange = (page: number, pageSize?: number) => {
  goodPagination.current = page
  if (pageSize) goodPagination.pageSize = pageSize
  loadGoodApps()
}

const openAppChat = (appId?: number) => {
  if (!appId) return
  router.push(`/app/chat/${appId}`)
}

const openAppEdit = (appId?: number) => {
  if (!appId) return
  router.push(`/app/edit/${appId}`)
}

const handleMyDelete = (app: API.App) => {
  if (!app.id) return
  Modal.confirm({
    title: '确认删除应用',
    content: `确定要删除「${app.appName || app.id}」吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteMyApp({ id: app.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadMyApps()
        } else {
          message.error(res.data.message || '删除失败')
        }
      } catch (e) {
        console.error(e)
        message.error('删除失败，请稍后重试')
      }
    },
  })
}

onMounted(() => {
  // 登录由路由守卫/请求拦截器处理，这里只负责触发加载
  if (isLoggedIn.value) {
    loadMyApps()
    loadGoodApps()
  } else {
    // 未登录时不拉取列表
  }
})
</script>

<template>
  <main class="app-home">
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-title">
          一句话生成
          <span class="cat-icon">🐵</span>
          应用
        </div>
        <div class="hero-subtitle">与 AI 对话，创建并预览你的网站应用</div>

        <div class="prompt-card">
          <a-textarea
            v-model:value="initPrompt"
            :rows="4"
            :disabled="creating"
            placeholder="使用 NoCode 创建一个高效的网站，帮我生成..."
            class="prompt-input"
          />

          <div class="prompt-actions">
            <a-button type="primary" :loading="creating" :disabled="!initPrompt.trim()" @click="handleCreate">
              生成应用
            </a-button>
          </div>
        </div>
      </div>
    </section>

    <section class="list-section">
      <div class="section-header">
        <h2>我的应用</h2>
        <div class="section-search">
          <a-input
            v-model:value="myAppName"
            placeholder="按应用名称搜索"
            allow-clear
            style="width: 240px"
          />
          <a-button type="primary" style="margin-left: 8px" @click="handleMySearch">搜索</a-button>
        </div>
      </div>

      <a-empty v-if="myApps.length === 0" description="暂无应用" />
      <a-row v-else :gutter="[16, 16]" class="cards-grid">
        <a-col v-for="app in myApps" :key="app.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="app-card" hoverable>
            <template #cover>
              <div class="cover-wrapper">
                <img v-if="app.cover" :src="app.cover" class="cover-img" alt="" />
                <div v-else class="cover-placeholder" />
              </div>
            </template>
            <a-card-meta :title="app.appName || '未命名'" :description="app.initPrompt ? app.initPrompt.slice(0, 40) + '...' : ''">
              <template #avatar>
                <div class="app-avatar">{{ (app.appName || 'A').slice(0, 1).toUpperCase() }}</div>
              </template>
            </a-card-meta>
            <div class="card-footer">
              <a-button type="link" @click="openAppEdit(app.id)">编辑</a-button>
              <a-button type="link" danger style="margin-left: 8px" @click="handleMyDelete(app)">
                删除
              </a-button>
              <a-button type="link" style="margin-left: 8px" @click="openAppChat(app.id)">进入对话</a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <div v-if="myTotal > 0" class="pagination">
        <a-pagination
          :current="myPagination.current"
          :page-size="myPagination.pageSize"
          :total="myTotal"
          show-size-changer
          :page-size-options="['10', '12', '20']"
          @change="onMyPageChange"
        />
      </div>
    </section>

    <section class="list-section selected-section">
      <div class="section-header">
        <h2>精选应用</h2>
        <div class="section-search">
          <a-input
            v-model:value="goodAppName"
            placeholder="按应用名称搜索"
            allow-clear
            style="width: 240px"
          />
          <a-button type="primary" style="margin-left: 8px" @click="handleGoodSearch">搜索</a-button>
        </div>
      </div>

      <a-empty v-if="goodApps.length === 0" description="暂无精选应用" />
      <a-row v-else :gutter="[16, 16]" class="cards-grid">
        <a-col v-for="app in goodApps" :key="app.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card class="app-card" hoverable>
            <template #cover>
              <div class="cover-wrapper">
                <img v-if="app.cover" :src="app.cover" class="cover-img" alt="" />
                <div v-else class="cover-placeholder" />
              </div>
            </template>
            <a-card-meta :title="app.appName || '未命名'" :description="app.initPrompt ? app.initPrompt.slice(0, 40) + '...' : ''">
              <template #avatar>
                <div class="app-avatar">{{ (app.appName || 'A').slice(0, 1).toUpperCase() }}</div>
              </template>
            </a-card-meta>
            <div class="card-footer">
              <a-button type="link" @click="openAppChat(app.id)">查看预览</a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <div v-if="goodTotal > 0" class="pagination">
        <a-pagination
          :current="goodPagination.current"
          :page-size="goodPagination.pageSize"
          :total="goodTotal"
          show-size-changer
          :page-size-options="['10', '12', '20']"
          @change="onGoodPageChange"
        />
      </div>
    </section>
  </main>
</template>

<style scoped>
.app-home {
  background: linear-gradient(180deg, #f7fbff 0%, #ffffff 60%);
}

.hero {
  padding: 56px 16px 32px;
}

.hero-inner {
  max-width: 1100px;
  margin: 0 auto;
  text-align: center;
}

.hero-title {
  font-size: 40px;
  font-weight: 900;
  margin-bottom: 10px;
  color: #0f172a;
}

.cat-icon {
  display: inline-block;
  margin: 0 8px;
  transform: translateY(3px);
}

.hero-subtitle {
  color: #64748b;
  font-size: 16px;
  margin-bottom: 24px;
}

.prompt-card {
  max-width: 860px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 16px;
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.08);
  padding: 18px;
}

.prompt-input {
  border-radius: 12px;
  overflow: hidden;
}

.prompt-actions {
  display: flex;
  justify-content: center;
  margin-top: 14px;
}

.list-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 22px 16px 52px;
}

.selected-section {
  padding-top: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0f172a;
}

.section-search {
  display: flex;
  align-items: center;
}

.cards-grid {
  margin-top: 8px;
}

.app-card {
  border-radius: 16px;
  overflow: hidden;
}

.cover-wrapper {
  height: 120px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.cover-placeholder {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.25), rgba(59, 130, 246, 0.25));
}

.app-avatar {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.card-footer {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
}

.pagination {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
}
</style>

