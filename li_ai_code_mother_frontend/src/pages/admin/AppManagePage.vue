<script setup lang="ts">
import { onMounted, reactive, ref, h } from 'vue'
import { message, Modal } from 'ant-design-vue'
import type { ColumnsType } from 'ant-design-vue/es/table'
import { useRouter } from 'vue-router'
import { listAppsByPageByAdmin, deleteAppByAdmin, updateAppByAdmin } from '@/api/appController'

const router = useRouter()

// 表格数据
const dataSource = ref<API.App[]>([])
const loading = ref(false)
const total = ref(0)

// 搜索表单（支持除时间外的字段查询）
const searchForm = reactive<API.AppAdminQueryRequest>({
  id: undefined,
  appName: '',
  cover: '',
  initPrompt: '',
  codeGenType: '',
  deployKey: '',
  priority: undefined,
  userId: undefined,
  isDelete: undefined,
  sortField: undefined,
  sortOrder: 'descend',
})

// 分页参数
const pagination = reactive({
  current: 1,
  pageSize: 20,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
  pageSizeOptions: ['10', '20', '50', '100', '200'],
})

const loadApps = async () => {
  loading.value = true
  try {
    const body: API.AppAdminQueryRequest = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      // 字段级查询：由后端 QueryWrapper 决定字符串模糊匹配、数值等值匹配
      id: searchForm.id && searchForm.id > 0 ? searchForm.id : undefined,
      appName: searchForm.appName || undefined,
      cover: searchForm.cover || undefined,
      initPrompt: searchForm.initPrompt || undefined,
      codeGenType: searchForm.codeGenType || undefined,
      deployKey: searchForm.deployKey || undefined,
      priority: searchForm.priority !== undefined ? searchForm.priority : undefined,
      userId: searchForm.userId !== undefined ? searchForm.userId : undefined,
      isDelete: searchForm.isDelete !== undefined ? searchForm.isDelete : undefined,
      sortField: searchForm.sortField || undefined,
      sortOrder: searchForm.sortOrder || undefined,
    }
    const res = await listAppsByPageByAdmin(body)
    if (res.data.code === 0 && res.data.data) {
      dataSource.value = res.data.data.records || []
      total.value = res.data.data.totalRow || 0
    } else {
      message.error(res.data.message || '获取应用列表失败')
      dataSource.value = []
      total.value = 0
    }
  } catch (e) {
    console.error(e)
    message.error('获取应用列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  loadApps()
}

const handleSearch = () => {
  pagination.current = 1
  loadApps()
}

const handleReset = () => {
  searchForm.id = undefined
  searchForm.appName = ''
  searchForm.cover = ''
  searchForm.initPrompt = ''
  searchForm.codeGenType = ''
  searchForm.deployKey = ''
  searchForm.priority = undefined
  searchForm.userId = undefined
  searchForm.isDelete = undefined
  searchForm.sortField = undefined
  searchForm.sortOrder = 'descend'
  pagination.current = 1
  loadApps()
}

const handleEdit = (record: API.App) => {
  if (!record.id) return
  router.push(`/app/edit/${record.id}`)
}

const handleDelete = (record: API.App) => {
  if (!record.id) return
  Modal.confirm({
    title: '确认删除应用',
    content: `确定要删除应用「${record.appName || record.id}」吗？`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await deleteAppByAdmin({ id: record.id })
        if (res.data.code === 0) {
          message.success('删除成功')
          loadApps()
        } else {
          message.error(res.data.message || '删除失败')
        }
      } catch (e) {
        console.error(e)
        message.error('删除失败，请稍后重试')
      }
    },
  })
}

const handleSetGood = (record: API.App) => {
  if (!record.id) return
  const body: API.AppAdminUpdateRequest = {
    id: record.id,
    priority: 99,
  }
  Modal.confirm({
    title: '设置为精选应用',
    content: `将「${record.appName || record.id}」设置为优先级 99（精选）？`,
    okText: '确认',
    onOk: async () => {
      try {
        const res = await updateAppByAdmin(body)
        if (res.data.code === 0) {
          message.success('设置成功')
          loadApps()
        } else {
          message.error(res.data.message || '设置失败')
        }
      } catch (e) {
        console.error(e)
        message.error('设置失败，请稍后重试')
      }
    },
  })
}

const columns: ColumnsType<API.App> = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: '封面',
    dataIndex: 'cover',
    key: 'cover',
    width: 100,
    customRender: ({ record }) => {
      if (record.cover) {
        return h('img', {
          src: record.cover,
          style: { width: '50px', height: '50px', objectFit: 'cover', borderRadius: '6px' },
        })
      }
      return h('span', { style: { color: '#999' } }, '-')
    },
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    key: 'appName',
    ellipsis: true,
    minWidth: 180,
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    key: 'priority',
    width: 90,
    customRender: ({ record }) => {
      const color = record.priority === 99 ? 'red' : '#1890ff'
      return h(
        'span',
        {
          style: {
            backgroundColor: color === 'red' ? 'rgba(255,77,79,0.15)' : 'rgba(24,144,255,0.15)',
            color: color,
            padding: '2px 8px',
            borderRadius: '999px',
            fontSize: '12px',
            fontWeight: 700,
          },
        },
        record.priority ?? '-',
      )
    },
  },
  {
    title: '代码类型',
    dataIndex: 'codeGenType',
    key: 'codeGenType',
    width: 120,
  },
  {
    title: '部署标识',
    dataIndex: 'deployKey',
    key: 'deployKey',
    width: 140,
    ellipsis: true,
  },
  {
    title: '创建用户',
    dataIndex: 'userId',
    key: 'userId',
    width: 120,
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
    width: 240,
    fixed: 'right',
    customRender: ({ record }) => {
      return h('div', {}, [
        h(
          'a',
          {
            onClick: () => handleEdit(record),
            style: { marginRight: '12px' },
          },
          '编辑',
        ),
        h(
          'a',
          {
            onClick: () => handleSetGood(record),
            style: { marginRight: '12px' },
          },
          '精选',
        ),
        h(
          'a',
          {
            onClick: () => handleDelete(record),
            style: { color: '#ff4d4f' },
          },
          '删除',
        ),
      ])
    },
  },
]

onMounted(() => {
  loadApps()
})
</script>

<template>
  <div class="app-manage-page">
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="ID">
          <a-input-number v-model:value="searchForm.id" :min="1" placeholder="可选" />
        </a-form-item>
        <a-form-item label="应用名称">
          <a-input v-model:value="searchForm.appName" placeholder="可模糊匹配" style="width: 160px" allow-clear />
        </a-form-item>
        <a-form-item label="封面 URL">
          <a-input v-model:value="searchForm.cover" placeholder="可模糊匹配" style="width: 200px" allow-clear />
        </a-form-item>
        <a-form-item label="代码类型">
          <a-input v-model:value="searchForm.codeGenType" placeholder="html / multi_file" style="width: 160px" allow-clear />
        </a-form-item>
        <a-form-item label="部署标识">
          <a-input v-model:value="searchForm.deployKey" placeholder="可模糊匹配" style="width: 200px" allow-clear />
        </a-form-item>
        <a-form-item label="优先级">
          <a-input-number v-model:value="searchForm.priority" placeholder="可选" />
        </a-form-item>
        <a-form-item label="用户 ID">
          <a-input-number v-model:value="searchForm.userId" :min="1" placeholder="可选" />
        </a-form-item>
        <a-form-item label="是否删除">
          <a-input-number v-model:value="searchForm.isDelete" :min="0" :max="1" placeholder="可选（0/1）" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleSearch">搜索</a-button>
          <a-button style="margin-left: 8px" @click="handleReset">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

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
  </div>
</template>

<style scoped>
.app-manage-page {
  padding: 24px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  min-height: 400px;
}

::deep(.ant-card-body) {
  padding: 16px;
}
</style>

