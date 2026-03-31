<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, onUpdated, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVOById } from '@/api/appController'
import { API_BASE_URL, getDeployUrl, getStaticPreviewUrl } from '@/config/env'

type ChatMessage = {
  id: string
  role: 'user' | 'ai'
  content: string
}

const route = useRoute()
const router = useRouter()

const appId = computed(() => Number(route.params.appId))
const autoGenerate = computed(() => route.query.autoGenerate === '1')

const appVO = ref<API.AppVO | null>(null)

const messages = ref<ChatMessage[]>([])
const userInput = ref('')

const generating = ref(false)
const previewUrl = ref<string>('')
const deployUrl = ref<string>('')

const endRef = ref<HTMLElement | null>(null)
let abortController: AbortController | null = null

const scrollToBottom = () => {
  endRef.value?.scrollIntoView({ behavior: 'smooth', block: 'end' })
}

onUpdated(() => {
  // 新消息出现时自动滚动
  scrollToBottom()
})

const handleDeploy = async () => {
  // 后端当前未实现“部署”控制器时，直接回退到静态预览 URL 展示
  try {
    if (appVO.value?.deployKey) {
      deployUrl.value = getDeployUrl(appVO.value.deployKey)
      message.success('已生成可访问地址（基于 deployKey）')
      return
    }
    // 尝试调用部署接口（若不存在会进入 catch）
    const resp = await fetch(`${API_BASE_URL}/app/deploy`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ id: appId.value }),
    })
    if (resp.ok) {
      const json = await resp.json().catch(() => ({}))
      // 兼容不同后端返回格式
      deployUrl.value =
        json?.data?.url || json?.data?.deployUrl || json?.data || getStaticPreviewUrl(appVO.value?.codeGenType || 'multi_file', String(appId.value))
      message.success('部署完成')
      return
    }
    throw new Error('deploy endpoint not found')
  } catch (e) {
    console.warn(e)
    // 回退：使用静态预览
    if (previewUrl.value) {
      deployUrl.value = previewUrl.value
      message.warning('部署接口不可用，已回退到静态预览链接')
    } else {
      message.error('部署失败，且静态预览不可用')
    }
  }
}

const parseAndHandleSseEvent = (block: string, onData: (chunk: string) => void) => {
  const lines = block.split(/\r?\n/).filter(Boolean)
  let eventName = ''
  const dataLines: string[] = []
  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice('event:'.length).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice('data:'.length).trim())
    }
  }

  const dataStr = dataLines.join('\n')
  if (!dataStr) return { done: false }

  // 后端错误会走全局异常处理，发 event: business-error
  if (eventName === 'business-error') {
    try {
      const errObj = JSON.parse(dataStr)
      throw new Error(errObj?.message || '生成失败')
    } catch (err) {
      throw err
    }
  }

  // 结束事件
  if (eventName === 'done' || dataStr === '{}' || dataStr === '[DONE]' || dataStr === 'done') {
    return { done: true }
  }

  // 兼容两种格式：纯文本 data / JSON {content|chunk|message|data}
  let appended = dataStr
  try {
    const obj = JSON.parse(dataStr)
    if (typeof obj === 'string') {
      appended = obj
    } else {
      appended = obj.content ?? obj.chunk ?? obj.message ?? obj.data ?? dataStr
    }
  } catch {
    // 非 JSON，直接按纯文本追加
    appended = dataStr
  }

  if (appended) onData(String(appended))
  return { done: false }
}

const streamGenerate = async (userMessage: string, onChunk: (chunk: string) => void) => {
  abortController?.abort()
  abortController = new AbortController()

  const res = await fetch(`${API_BASE_URL}/chat/gen/code`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
    },
    credentials: 'include',
    signal: abortController.signal,
    body: JSON.stringify({
      appId: appId.value,
      userMessage,
    }),
  })

  if (!res.ok) {
    throw new Error(`SSE 请求失败：${res.status}`)
  }

  const reader = res.body?.getReader()
  if (!reader) {
    throw new Error('SSE 响应体为空')
  }

  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  while (true) {
    const { value, done } = await reader.read()
    if (done) break

    buffer += decoder.decode(value, { stream: true })

    // 以空行分割一个 SSE event block
    let sepIndex = buffer.indexOf('\n\n')
    while (sepIndex !== -1) {
      const block = buffer.slice(0, sepIndex)
      buffer = buffer.slice(sepIndex + 2)
      sepIndex = buffer.indexOf('\n\n')

      const { done: isDone } = parseAndHandleSseEvent(block, onChunk)
      if (isDone) return
    }
  }
}

const startAutoGenerate = async () => {
  const initPrompt = appVO.value?.initPrompt?.trim() || ''
  if (!initPrompt) {
    message.warning('该应用缺少 initPrompt，无法自动生成')
    return
  }

  messages.value = [
    {
      id: String(Date.now()) + '_u0',
      role: 'user',
      content: initPrompt,
    },
  ]

  const aiMessage: ChatMessage = {
    id: String(Date.now()) + '_ai0',
    role: 'ai',
    content: '',
  }
  messages.value.push(aiMessage)

  generating.value = true
  try {
    await streamGenerate(initPrompt, (chunk) => {
      aiMessage.content += chunk
    })
    message.success('生成完成，正在展示预览')
    previewUrl.value = getStaticPreviewUrl(appVO.value?.codeGenType || 'multi_file', String(appId.value))
  } catch (e) {
    console.error(e)
    message.error('生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

const sendMessage = async () => {
  const content = userInput.value.trim()
  if (!content || generating.value) return
  userInput.value = ''

  const userMsg: ChatMessage = {
    id: String(Date.now()) + '_u',
    role: 'user',
    content,
  }
  const aiMsg: ChatMessage = {
    id: String(Date.now()) + '_ai',
    role: 'ai',
    content: '',
  }

  messages.value.push(userMsg)
  messages.value.push(aiMsg)

  generating.value = true
  try {
    await streamGenerate(content, (chunk) => {
      aiMsg.content += chunk
    })
    previewUrl.value = getStaticPreviewUrl(appVO.value?.codeGenType || 'multi_file', String(appId.value))
  } catch (e) {
    console.error(e)
    message.error('生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

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
      previewUrl.value = getStaticPreviewUrl(res.data.data.codeGenType || 'multi_file', String(id))
    } else {
      message.error(res.data.message || '加载应用失败')
    }
  } catch (e) {
    console.error(e)
    message.error('加载应用失败，请稍后重试')
  }
}

onMounted(async () => {
  await loadApp()
  if (autoGenerate.value) {
    // 创建流程：严格等 SSE 全部返回后再展示预览
    previewUrl.value = ''
    await startAutoGenerate()
  } else {
    // 非创建流程：直接展示预览区域
    // （若静态文件不存在，iframe 可能会显示 404，这符合“已部署应用才能预览”的预期）
  }
})

onBeforeUnmount(() => {
  abortController?.abort()
})
</script>

<template>
  <main class="chat-page">
    <header class="top-bar">
      <div class="app-name">{{ appVO?.appName || '应用' }}</div>
      <div class="top-actions">
        <a-button type="primary" :loading="generating" @click="handleDeploy">部署</a-button>
      </div>
    </header>

    <section class="content">
      <div class="left">
        <div class="messages">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="msg-row"
            :class="{ 'is-user': msg.role === 'user', 'is-ai': msg.role === 'ai' }"
          >
            <div class="bubble" :class="{ 'bubble-user': msg.role === 'user', 'bubble-ai': msg.role === 'ai' }">
              <div class="bubble-content">{{ msg.content }}</div>
            </div>
          </div>
          <div v-if="generating" class="streaming-hint">生成中...</div>
          <div ref="endRef" />
        </div>

        <div class="composer">
          <a-input
            v-model:value="userInput"
            :disabled="generating"
            placeholder="输入需求，继续让 AI 生成..."
            @pressEnter="sendMessage"
          />
          <a-button type="primary" :disabled="generating || !userInput.trim()" style="margin-left: 8px" @click="sendMessage">
            发送
          </a-button>
        </div>
      </div>

      <div class="right">
        <div class="preview-card">
          <div class="preview-header">
            <div class="preview-title">生成后的网页展示</div>
            <div v-if="deployUrl" class="deploy-link">
              <a :href="deployUrl" target="_blank" rel="noopener noreferrer">已部署：打开</a>
            </div>
          </div>

          <div class="preview-body" v-if="previewUrl">
            <iframe :src="previewUrl" class="preview-iframe" />
          </div>
          <div v-else class="preview-empty">等待生成完成后展示预览...</div>
        </div>
      </div>
    </section>
  </main>
</template>

<style scoped>
.chat-page {
  width: 100%;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.app-name {
  font-size: 20px;
  font-weight: 900;
}

.content {
  display: grid;
  grid-template-columns: 1fr 1.1fr;
  gap: 16px;
  min-height: calc(100vh - 220px);
}

.left {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.messages {
  padding: 16px;
  flex: 1;
  overflow: auto;
}

.msg-row {
  display: flex;
  margin-bottom: 12px;
}

.msg-row.is-user {
  justify-content: flex-end;
}

.msg-row.is-ai {
  justify-content: flex-start;
}

.bubble {
  max-width: 78%;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-user {
  background: #e6f7ff;
  border-color: rgba(24, 144, 255, 0.35);
}

.bubble-ai {
  background: #fafafa;
}

.bubble-content {
  font-size: 14px;
  line-height: 1.6;
}

.streaming-hint {
  color: #94a3b8;
  font-size: 13px;
  text-align: center;
  margin-top: 10px;
}

.composer {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
}

.right {
  background: #fff;
  border-radius: 16px;
  border: 1px solid #f0f0f0;
  overflow: hidden;
}

.preview-card {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.preview-header {
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.preview-title {
  font-weight: 800;
}

.preview-body {
  flex: 1;
  padding: 0;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: 0;
}

.preview-empty {
  padding: 28px;
  color: #94a3b8;
}

@media (max-width: 1100px) {
  .content {
    grid-template-columns: 1fr;
  }
  .right {
    min-height: 420px;
  }
}
</style>

