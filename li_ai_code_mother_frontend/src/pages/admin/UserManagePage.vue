<script setup lang="ts">
import { ref, reactive, onMounted, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { ColumnsType } from 'ant-design-vue/es/table'
import { listUserVoByPage, remove, updateUser } from '@/api/userController'

// 表格数据
const dataSource = ref<API.UserVO[]>([])
const loading = ref(false)
const total = ref(0)

// 搜索表单
const searchForm = reactive({
  userName: '',
  userAccount: '',
})

// 分页参数
const pagination = reactive({
  current: 1,
  pageSize: 10,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100'],
})

// 编辑弹窗控制
const editModalVisible = ref(false)
const editingUser = ref<API.UserVO | null>(null)
const editForm = reactive<API.UserUpdateRequest>({
  id: undefined,
  userName: '',
  userAvatar: '',
  userProfile: '',
  userRole: '',
})

/**
 * 加载用户列表
 */
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await listUserVoByPage({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      userName: searchForm.userName || undefined,
      userAccount: searchForm.userAccount || undefined,
    })
    if (res.data.code === 0 && res.data.data) {
      dataSource.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    } else {
      message.error('获取用户列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理分页变化
 */
const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadUserList()
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadUserList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.userName = ''
  searchForm.userAccount = ''
  pagination.current = 1
  loadUserList()
}

/**
 * 删除用户
 */
const handleDelete = (record: API.UserVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除用户 "${record.userName || record.userAccount}" 吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await remove({ id: record.id! })
        if (res) {
          message.success('删除成功')
          loadUserList()
        } else {
          message.error('删除失败')
        }
      } catch (error) {
        console.error('删除用户失败:', error)
        message.error('删除失败，请稍后重试')
      }
    },
  })
}

/**
 * 打开编辑弹窗
 */
const handleEdit = (record: API.UserVO) => {
  editingUser.value = record
  editForm.id = record.id
  editForm.userName = record.userName || ''
  editForm.userAvatar = record.userAvatar || ''
  editForm.userProfile = record.userProfile || ''
  editForm.userRole = record.userRole || ''
  editModalVisible.value = true
}

/**
 * 打开创建弹窗
 */
const handleCreate = () => {
  editingUser.value = null
  editForm.id = undefined
  editForm.userName = ''
  editForm.userAvatar = ''
  editForm.userProfile = ''
  editForm.userRole = ''
  editModalVisible.value = true
}

/**
 * 提交编辑表单
 */
const handleEditSubmit = async () => {
  try {
    const res = await updateUser(editForm)
    if (res.data.code === 0) {
      message.success(editingUser.value ? '更新成功' : '创建成功')
      editModalVisible.value = false
      loadUserList()
    } else {
      message.error('操作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('提交表单失败:', error)
    message.error('操作失败，请稍后重试')
  }
}

/**
 * 表格列定义
 */
const columns: ColumnsType<API.UserVO> = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    key: 'userName',
    width: 120,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    width: 150,
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    key: 'userAvatar',
    width: 100,
    customRender: ({ record }) => {
      if (record.userAvatar) {
        return h('img', {
          src: record.userAvatar,
          style: { width: '50px', height: '50px', objectFit: 'cover' },
        })
      }
      return h('span', { style: { color: '#999' } }, '无头像')
    },
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    key: 'userProfile',
    ellipsis: true,
  },
  {
    title: '角色',
    dataIndex: 'userRole',
    key: 'userRole',
    width: 100,
    customRender: ({ record }) => {
      const roleMap: Record<string, string> = {
        admin: '管理员',
        user: '普通用户',
      }
      const color = record.userRole === 'admin' ? 'red' : 'blue'
      const roleName = roleMap[record.userRole || ''] || record.userRole
      return h('span', { class: 'ant-tag', style: { backgroundColor: color, color: '#fff', padding: '2px 8px', borderRadius: '2px', fontSize: '12px' } }, roleName)
    },
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
    customRender: ({ record }) => {
      return h('div', {}, [
        h('a', {
          onClick: () => handleEdit(record),
          style: { marginRight: '8px' },
        }, '编辑'),
        h('a', {
          onClick: () => handleDelete(record),
          style: { color: '#ff4d4f' },
        }, '删除'),
      ])
    },
  },
]

// 初始化加载
onMounted(() => {
  loadUserList()
})
</script>

<template>
  <div class="user-manage-page">
    <!-- 搜索区域 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="用户名">
          <a-input
            v-model:value="searchForm.userName"
            placeholder="请输入用户名"
            style="width: 180px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="账号">
          <a-input
            v-model:value="searchForm.userAccount"
            placeholder="请输入账号"
            style="width: 180px"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
          <a-button type="primary" style="margin-left: auto" @click="handleCreate">
            + 新建用户
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="{
          ...pagination,
          total,
        }"
        @change="handleTableChange"
        row-key="id"
      />
    </a-card>

    <!-- 编辑/创建弹窗 -->
    <a-modal
      v-model:open="editModalVisible"
      :title="editingUser ? '编辑用户' : '新建用户'"
      @ok="handleEditSubmit"
      @cancel="editModalVisible = false"
      destroy-on-close
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="用户名" name="userName">
          <a-input v-model:value="editForm.userName" placeholder="请输入用户名" />
        </a-form-item>
        <a-form-item label="头像 URL" name="userAvatar">
          <a-input v-model:value="editForm.userAvatar" placeholder="请输入头像 URL" />
        </a-form-item>
        <a-form-item label="简介" name="userProfile">
          <a-textarea
            v-model:value="editForm.userProfile"
            placeholder="请输入用户简介"
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="角色" name="userRole">
          <a-select v-model:value="editForm.userRole" placeholder="请选择用户角色">
            <a-select-option value="user">普通用户</a-select-option>
            <a-select-option value="admin">管理员</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
.user-manage-page {
  padding: 24px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  min-height: 400px;
}

:deep(.ant-card-body) {
  padding: 16px;
}
</style>
