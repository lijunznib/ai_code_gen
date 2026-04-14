<template>
  <a-layout-header class="header">
    <a-row :wrap="false" align="middle" class="header-row">
      <a-col flex="240px">
        <RouterLink to="/" class="brand-link">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <div>
              <h1 class="site-title">LI NoCode</h1>
              <p class="site-subtitle">AI App Workspace</p>
            </div>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <div class="menu-shell">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="horizontal"
            :items="menuItems"
            @click="handleMenuClick"
          />
        </div>
      </a-col>
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://github.com/lijunznib', target: '_blank' }, '代码仓库'),
    title: '代码仓库',
  },
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 退出登录
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
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: var(--z-header);
  height: 72px;
  line-height: 72px;
  background: color-mix(in srgb, var(--color-bg-surface) 92%, transparent);
  border-bottom: 1px solid var(--color-border-default);
  backdrop-filter: blur(12px);
  padding: 0 var(--space-6);
  box-shadow: var(--shadow-sm);
}

.header-row {
  height: 100%;
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.brand-link {
  text-decoration: none;
}

.logo {
  height: 42px;
  width: 42px;
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.site-title {
  margin: 0;
  line-height: 1.2;
  font-size: var(--font-size-title);
  font-weight: 700;
  color: var(--color-text-primary);
}

.site-subtitle {
  margin: 0;
  line-height: 1.1;
  font-size: var(--font-size-caption);
  color: var(--color-text-tertiary);
}

.menu-shell {
  padding: 0 var(--space-6);
}

.ant-menu-horizontal {
  border-bottom: none !important;
  background: transparent;
}

:deep(.ant-menu-horizontal > .ant-menu-item-selected) {
  color: var(--color-primary);
  font-weight: 600;
}

:deep(.ant-menu-horizontal > .ant-menu-item::after) {
  border-bottom-color: var(--color-primary) !important;
}

.user-login-status {
  display: flex;
  align-items: center;
  color: var(--color-text-secondary);
}

:deep(.ant-btn-primary) {
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-sm);
}

:deep(.ant-btn-primary:hover) {
  background: var(--color-primary-hover);
  border-color: var(--color-primary-hover);
}
</style>
