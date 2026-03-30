<template>
  <main id="userLoginPage" role="main">
    <h1 class="title">用户登录</h1>
    <div class="desc">Lij AI 应用生成 - 不写一行代码，生成完整应用</div>
    <a-form
      ref="formRef"
      :model="formState"
      name="basic"
      autocomplete="off"
      :label-col="{ span: 24 }"
      :wrapper-col="{ span: 24 }"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[{ required: true, message: '请输入账号' }]"
      >
        <a-input
          v-model:value="formState.userAccount"
          placeholder="请输入账号"
          aria-label="账号"
          :disabled="submitting"
          autocomplete="username"
        />
      </a-form-item>
      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="请输入密码"
          aria-label="密码"
          :disabled="submitting"
          autocomplete="current-password"
        />
      </a-form-item>
      <div class="tips">
        没有账号
        <RouterLink to="/user/register">去注册</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" :loading="submitting" style="width: 100%" @click="handleSubmit">
          {{ submitting ? '登录中...' : '登录' }}
        </a-button>
      </a-form-item>
    </a-form>
  </main>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userLogin } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'

const formRef = ref<FormInstance>()
const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()
const submitting = ref(false)

/**
 * 提交表单
 */
const handleSubmit = async () => {
  console.log('提交登录表单')
  if (!formRef.value) return

  try {
    await formRef.value?.validate()
    console.log('表单验证通过', formState)

    if (submitting.value) return
    submitting.value = true

    const res = await userLogin(formState)
    console.log('登录响应:', res)

    // 登录成功
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('登录成功')
      router.push({
        path: '/',
        replace: true,
      })
    } else {
      message.error('登录失败，' + res.data.message)
    }
  } catch (error) {
    console.error('登录错误:', error)
    // 验证失败不显示网络错误
    if (error !== 'validate') {
      message.error('网络错误，请稍后重试')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userLoginPage {
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
  margin-bottom: 16px;
}

.tips {
  text-align: right;
  color: #666;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
