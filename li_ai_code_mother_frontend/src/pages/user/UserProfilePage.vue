<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { updateUser, getUserVoById } from '@/api/userController'
import type { UserUpdateRequest } from '@/api/typings'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const formRef = ref<FormInstance>()
const submitting = ref(false)

// 表单数据
const formState = reactive<UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
})

// 当前登录用户
const currentUser = computed(() => loginUserStore.loginUser)

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  if (!currentUser.value.id) {
    message.warning('请先登录')
    router.push('/user/login')
    return
  }

  try {
    const res = await getUserVoById({ id: currentUser.value.id })
    if (res.data.code === 0 && res.data.data) {
      const user = res.data.data
      formState.id = user.id
      formState.userName = user.userName || ''
      formState.userAvatar = user.userAvatar || ''
      formState.userProfile = user.userProfile || ''
      formState.userRole = user.userRole || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    message.error('加载用户信息失败，请稍后重试')
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    if (submitting.value) return
    submitting.value = true

    const res = await updateUser(formState)
    if (res.data.code === 0) {
      message.success('保存成功')
      // 更新本地用户信息
      await loginUserStore.fetchLoginUser()
    } else {
      message.error('保存失败，' + res.data.message)
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<template>
  <main id="userProfilePage" role="main">
    <h1 class="title">个人中心</h1>
    <div class="desc">编辑您的个人信息</div>

    <a-form
      ref="formRef"
      :model="formState"
      name="profile"
      layout="vertical"
      :label-col="{ span: 24 }"
      :wrapper-col="{ span: 24 }"
    >
      <a-form-item
        label="用户名"
        name="userName"
        :rules="[
          { required: true, message: '请输入用户名' },
          { min: 2, max: 20, message: '用户名长度在 2-20 个字符之间' },
        ]"
      >
        <a-input
          v-model:value="formState.userName"
          placeholder="请输入用户名"
          :disabled="submitting"
        />
      </a-form-item>

      <a-form-item
        label="头像 URL"
        name="userAvatar"
        :rules="[{ type: 'url', message: '请输入有效的 URL' }]"
      >
        <a-input
          v-model:value="formState.userAvatar"
          placeholder="请输入头像图片链接"
          :disabled="submitting"
        />
        <div v-if="formState.userAvatar" class="avatar-preview">
          <p class="preview-label">头像预览：</p>
          <img :src="formState.userAvatar" alt="头像预览" class="avatar-image" />
        </div>
      </a-form-item>

      <a-form-item
        label="个人简介"
        name="userProfile"
        :rules="[{ max: 200, message: '简介不能超过 200 字' }]"
      >
        <a-textarea
          v-model:value="formState.userProfile"
          placeholder="介绍一下自己吧..."
          :rows="4"
          :disabled="submitting"
          show-count
          :maxlength="200"
        />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ submitting ? '保存中...' : '保存' }}
        </a-button>
        <a-button style="margin-left: 16px" @click="router.back()">
          返回
        </a-button>
      </a-form-item>
    </a-form>
  </main>
</template>

<style scoped>
#userProfilePage {
  background: white;
  max-width: 720px;
  padding: 24px;
  margin: 24px auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #666;
  margin-bottom: 32px;
}

.avatar-preview {
  margin-top: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.preview-label {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

.avatar-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #1890ff;
}
</style>
