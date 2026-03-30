<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { Menu, Button, Space, Dropdown, Avatar } from 'ant-design-vue'
import { LogoutOutlined } from '@ant-design/icons-vue'
import { userLogout } from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])

// 菜单配置
const menuItems = computed<MenuProps['items']>(() => [
  {
    key: '/',
    label: '首页',
  },
  {
    key: '/features',
    label: '功能特性',
  },
  {
    key: '/docs',
    label: '文档',
  },
  {
    key: '/about',
    label: '关于我们',
  },
  ...(loginUserStore.loginUser.userRole === 'admin'
    ? [
        {
          key: '/admin/userManage',
          label: '用户管理',
        },
      ]
    : []),
])

// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  router.push(key)
}

// 处理登录按钮点击
const handleLogin = () => {
  router.push('/user/login')
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

// 处理用户菜单点击
const handleUserMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  if (key === 'profile') {
    router.push('/user/profile')
  } else if (key === 'settings') {
    router.push('/user/settings')
  } else if (key === 'logout') {
    doLogout()
  }
}

// 获取用户头像首字母
const getAvatarText = (name?: string) => {
  if (!name || name === '未登录') {
    return 'U'
  }
  return name.charAt(0).toUpperCase()
}

// 组件挂载时获取登录用户信息
onMounted(async () => {
  await loginUserStore.fetchLoginUser()
})
</script>

<template>
  <div class="global-header">
    <div class="header-left">
      <div class="logo-wrapper">
        <img src="../../assets/aiAvatar.png" alt="LI AI Code Mother 首页" class="logo" />
        <span class="site-title">LI AI Code Mother</span>
      </div>
      <nav role="navigation" aria-label="主导航">
        <Menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
          class="nav-menu"
        />
      </nav>
    </div>
    <div class="header-right">
      <Space>
        <template v-if="loginUserStore.loginUser.id">
          <a-dropdown>
            <a-space class="user-info" style="cursor: pointer">
              <a-avatar :size="32" :src="loginUserStore.loginUser.userAvatar">
                {{ getAvatarText(loginUserStore.loginUser.userName) }}
              </a-avatar>
              <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
            </a-space>
            <template #overlay>
              <a-menu>
                <a-menu-item key="profile">个人中心</a-menu-item>
                <a-menu-item key="settings">个人设置</a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="doLogout">
                  <LogoutOutlined />
                  退出登录
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <Button type="primary" @click="handleLogin">登录</Button>
        </template>
      </Space>
    </div>
  </div>
</template>

<style scoped>
.global-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 24px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  height: 32px;
  width: auto;
}

.site-title {
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
  white-space: nowrap;
}

.nav-menu {
  border-bottom: none;
  line-height: 64px;
}

.nav-menu :deep(.ant-menu-item) {
  height: auto;
  margin: 0;
  padding: 0 16px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .global-header {
    padding: 0 16px;
    flex-direction: column;
    height: auto;
    gap: 16px;
  }

  .header-left {
    flex-direction: column;
    gap: 16px;
    width: 100%;
  }

  .logo-wrapper {
    justify-content: center;
  }

  .nav-menu {
    width: 100%;
    justify-content: center;
  }

  .nav-menu :deep(.ant-menu-item) {
    padding: 0 8px;
    font-size: 14px;
  }
}
</style>
