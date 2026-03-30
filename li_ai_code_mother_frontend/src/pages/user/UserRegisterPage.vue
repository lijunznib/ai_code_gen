<template>
  <main id="userRegisterPage" role="main">
    <h1 class="title">用户注册</h1>
    <div class="desc">Lij AI 应用生成 - 不写一行代码，生成完整应用</div>
    <a-form
      ref="formRef"
      :model="formState"
      name="basic"
      autocomplete="off"
      @finish="handleSubmit"
      @finishFailed="handleFinishFailed"
      :label-col="{ span: 24 }"
      :wrapper-col="{ span: 24 }"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[
          { required: true, message: '请输入账号' },
          { min: 4, message: '账号长度不能小于 4 位' },
          { max: 20, message: '账号长度不能超过 20 位' },
        ]"
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
          { max: 20, message: '密码长度不能超过 20 位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="请输入密码"
          aria-label="密码"
          :disabled="submitting"
          autocomplete="new-password"
        />
      </a-form-item>
      <a-form-item
        label="确认密码"
        name="checkPassword"
        :rules="[
          { required: true, message: '请确认密码' },
          { min: 8, message: '密码长度不能小于 8 位' },
          { validator: validateCheckPassword, trigger: 'change' },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          placeholder="请再次输入密码"
          aria-label="确认密码"
          :disabled="submitting"
          autocomplete="new-password"
        />
      </a-form-item>
      <div class="tips">
        已有账号
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
      <a-form-item>
        <a-button type="primary" :loading="submitting" style="width: 100%" @click="handleSubmit">
          {{ submitting ? '注册中...' : '注册' }}
        </a-button>
      </a-form-item>
    </a-form>
  </main>
</template>
<script lang="ts" setup>
import { reactive, ref } from 'vue'
import { userRegister } from '@/api/userController'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'

const formRef = ref<FormInstance>()
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const router = useRouter()
const submitting = ref(false)

/**
 * 校验确认密码
 */
const validateCheckPassword = async () => {
  const value = formState.checkPassword
  if (!value) {
    return Promise.reject(new Error('请确认密码'))
  }
  if (value === formState.userPassword) {
    return Promise.resolve()
  }
  return Promise.reject(new Error('两次输入的密码不一致'))
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  console.log('提交表单')
  if (!formRef.value) return

  try {
    await formRef.value?.validate()
    console.log('表单验证通过', formState)

    if (submitting.value) return
    submitting.value = true

    const res = await userRegister(formState)
    console.log('注册响应:', res)

    // 注册成功
    if (res.data.code === 0 && res.data.data) {
      message.success('注册成功，请登录')
      router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('注册失败，' + res.data.message)
    }
  } catch (error) {
    console.error('注册错误:', error)
    message.error('网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

/**
 * 表单验证失败处理
 */
const handleFinishFailed = (errorInfo: any) => {
  console.log('表单验证失败:', errorInfo)
  message.error('请检查表单填写是否正确')
}
</script>

<style scoped>
#userRegisterPage {
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
