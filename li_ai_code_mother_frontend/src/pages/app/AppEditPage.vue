<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  deleteAppByAdmin,
  deleteMyApp,
  getAppVOById,
  updateAppByAdmin,
  updateAppName,
} from '@/api/appController'
import type { FormInstance } from 'ant-design-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appId = computed(() => Number(route.params.appId))

const appVO = ref<API.AppVO | null>(null)

const formRef = ref<FormInstance>()
const submitting = ref(false)

const formState = reactive<{
  id?: number
  appName: string
  cover: string
  priority?: number
}>({
  id: undefined,
  appName: '',
  cover: '',
  priority: undefined,
})

const isAdmin = computed(() => loginUserStore.loginUser?.userRole === 'admin')
const currentUserId = computed(() => loginUserStore.loginUser?.id)

const canEdit = computed(() => {
  if (isAdmin.value) return true
  if (!appVO.value?.userId) return false
  return appVO.value.userId === currentUserId.value
})

const canDelete = computed(() => canEdit.value || isAdmin.value)

const loadApp = async () => {
  const id = appId.value
  if (!id) {
    message.error('无效的 appId')
    router.back()
    return
  }
  try {
    const res = await getAppVOById({ id })
    if (res.data.code === 0 && res.data.data) {
      appVO.value = res.data.data
      formState.id = res.data.data.id
      formState.appName = res.data.data.appName || ''
      formState.cover = res.data.data.cover || ''
      formState.priority = res.data.data.priority ?? undefined
    } else {
      message.error(res.data.message || '加载应用失败')
    }
  } catch (e) {
    console.error(e)
    message.error('加载应用失败，请稍后重试')
  }
}

const buildAdminUpdateBody = (): API.AppAdminUpdateRequest => {
  const body: API.AppAdminUpdateRequest = {
    id: formState.id,
    appName: formState.appName.trim(),
  }

  // cover 由后端校验：若传入非空但为空字符串会抛错，因此仅在非空时才传
  const coverTrim = formState.cover.trim()
  if (coverTrim) body.cover = coverTrim

  if (formState.priority !== undefined) body.priority = formState.priority

  return body
}

const handleSave = async () => {
  if (!canEdit.value) {
    message.error('你没有权限编辑该应用')
    return
  }
  if (!formState.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }
  if (submitting.value) return

  submitting.value = true
  try {
    let res: any
    if (isAdmin.value) {
      res = await updateAppByAdmin(buildAdminUpdateBody())
    } else {
      res = await updateAppName({
        id: formState.id,
        appName: formState.appName.trim(),
      })
    }

    if (res?.data?.code === 0) {
      message.success('保存成功')
      await loadApp()
    } else {
      message.error(res?.data?.message || '保存失败')
    }
  } catch (e) {
    console.error(e)
    message.error('保存失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async () => {
  if (!formState.id) return
  if (!canDelete.value) {
    message.error('你没有权限删除该应用')
    return
  }

  Modal.confirm({
    title: '确认删除',
    content: '删除后将无法恢复，确认继续吗？',
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    async onOk() {
      try {
        const res = isAdmin.value
          ? await deleteAppByAdmin({ id: formState.id })
          : await deleteMyApp({ id: formState.id })
        if (res?.data?.code === 0) {
          message.success('删除成功')
          router.push('/')
        } else {
          message.error(res?.data?.message || '删除失败')
        }
      } catch (e) {
        console.error(e)
        message.error('删除失败，请稍后重试')
      }
    },
  })
}

onMounted(async () => {
  await loadApp()
})
</script>

<template>
  <main class="app-edit-page">
    <div class="page-head">
      <div class="title">应用信息修改</div>
      <div class="actions">
        <a-button @click="router.back()">返回</a-button>
      </div>
    </div>

    <a-card :bordered="false" class="card">
      <a-form ref="formRef" :model="formState" layout="vertical">
        <a-form-item label="应用名称">
          <a-input v-model:value="formState.appName" :disabled="submitting || !canEdit" placeholder="请输入应用名称" />
        </a-form-item>

        <a-form-item v-if="isAdmin" label="应用封面">
          <a-input v-model:value="formState.cover" :disabled="submitting" placeholder="应用封面 URL（可选）" />
          <div v-if="formState.cover" class="cover-preview">
            <img :src="formState.cover" alt="cover" />
          </div>
        </a-form-item>

        <a-form-item v-if="isAdmin" label="优先级">
          <a-input-number v-model:value="formState.priority" :disabled="submitting" style="width: 100%" />
        </a-form-item>

        <a-form-item label="初始化 Prompt">
          <a-textarea :value="appVO?.initPrompt" :rows="6" disabled />
        </a-form-item>

        <div class="meta-grid">
          <div class="meta-item">
            <div class="meta-label">代码生成类型</div>
            <div class="meta-value">{{ appVO?.codeGenType || '-' }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">部署标识</div>
            <div class="meta-value">{{ appVO?.deployKey || '-' }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">部署时间</div>
            <div class="meta-value">{{ appVO?.deployedTime || '-' }}</div>
          </div>
          <div class="meta-item">
            <div class="meta-label">创建用户</div>
            <div class="meta-value">{{ appVO?.user?.userName || appVO?.userId || '-' }}</div>
          </div>
        </div>

        <div class="form-actions">
          <a-button type="primary" :loading="submitting" :disabled="submitting || !canEdit" @click="handleSave">
            保存
          </a-button>
          <a-button v-if="canDelete" danger :disabled="submitting" style="margin-left: 12px" @click="handleDelete">
            删除
          </a-button>
        </div>
      </a-form>
    </a-card>
  </main>
</template>

<style scoped>
.app-edit-page {
  max-width: 980px;
  margin: 0 auto;
}

.page-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 22px;
  font-weight: 900;
}

.card :deep(.ant-card-body) {
  padding: 20px;
}

.cover-preview {
  margin-top: 10px;
  padding: 10px;
  background: #f8fafc;
  border-radius: 12px;
}

.cover-preview img {
  width: 180px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.meta-item {
  border: 1px solid #f0f0f0;
  padding: 12px;
  border-radius: 12px;
}

.meta-label {
  color: #94a3b8;
  font-size: 13px;
}

.meta-value {
  margin-top: 6px;
  font-weight: 700;
  word-break: break-word;
}

.form-actions {
  margin-top: 16px;
  display: flex;
  align-items: center;
}
</style>

