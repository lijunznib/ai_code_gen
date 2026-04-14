<template>
  <div id="userRegisterPage" class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="badge">Create Account</div>
        <h2 class="title">LI NoCode - 用户注册</h2>
        <div class="desc">不写一行代码，生成完整应用</div>
      </div>

      <a-form :model="formState" name="basic" autocomplete="off" layout="vertical" @finish="handleSubmit">
        <a-form-item label="账号" name="userAccount" :rules="[{ required: true, message: '请输入账号' }]">
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" size="large" />
        </a-form-item>
        <a-form-item
          label="密码"
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>
        <a-form-item
          label="确认密码"
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { min: 8, message: '密码不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password
            v-model:value="formState.checkPassword"
            placeholder="请确认密码"
            size="large"
          />
        </a-form-item>
        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn" size="large">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.userPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister(values)
  // 注册成功，跳转到登录页面
  if (res.data.code === 0) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + res.data.message)
  }
}
</script>

<style scoped>
.auth-page {
  width: min(760px, 100%);
  margin: 0 auto;
  padding: var(--space-10) var(--space-4);
}

.auth-card {
  border-radius: var(--radius-lg);
  background: color-mix(in srgb, var(--color-bg-surface) 94%, transparent);
  border: 1px solid var(--color-border-default);
  box-shadow: var(--shadow-md);
  padding: var(--space-8);
}

.auth-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.badge {
  display: inline-flex;
  align-items: center;
  padding: var(--space-1) var(--space-3);
  border-radius: 999px;
  font-size: var(--font-size-caption);
  color: var(--color-primary);
  background: color-mix(in srgb, var(--color-primary) 12%, transparent);
  border: 1px solid color-mix(in srgb, var(--color-primary) 35%, transparent);
}

.title {
  margin: var(--space-3) 0 var(--space-2);
  color: var(--color-text-primary);
}

.desc {
  color: var(--color-text-tertiary);
  margin-bottom: 0;
}

.tips {
  margin-bottom: var(--space-4);
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
  text-align: right;
}

.tips a {
  margin-left: var(--space-1);
  color: var(--color-primary);
}

.submit-btn {
  width: 100%;
  border-radius: var(--radius-sm);
}

:deep(.ant-form-item-label > label) {
  color: var(--color-text-secondary);
  font-weight: 500;
}

:deep(.ant-input-affix-wrapper),
:deep(.ant-input) {
  border-radius: var(--radius-sm);
  border-color: var(--color-border-default);
  background: color-mix(in srgb, var(--color-bg-surface) 90%, transparent);
  color: var(--color-text-primary);
}

:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-input:hover) {
  border-color: var(--color-primary-hover);
}

:deep(.ant-input-affix-wrapper-focused),
:deep(.ant-input:focus) {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 2px color-mix(in srgb, var(--color-primary) 20%, transparent);
}
</style>
