<template>
  <div id="userLoginPage" class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <div class="badge">Welcome Back</div>
        <h2 class="title">LI NoCode - 用户登录</h2>
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
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password
            v-model:value="formState.userPassword"
            placeholder="请输入密码"
            size="large"
          />
        </a-form-item>
        <div class="tips">
          没有账号
          <RouterLink to="/user/register">去注册</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn" size="large">登录</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'

const formState = reactive<API.UserLoginRequest>({
  userAccount: '',
  userPassword: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const res = await userLogin(values)
  // 登录成功，把登录态保存到全局状态中
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
  text-align: right;
  color: var(--color-text-tertiary);
  font-size: var(--font-size-caption);
  margin-bottom: var(--space-4);
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
