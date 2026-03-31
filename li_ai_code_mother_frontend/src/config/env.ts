/**
 * 环境变量配置
 *
 * 注：当前项目后端部署在本地 `http://localhost:8123/api`，因此默认值也按该地址配置。
 */

// 应用部署域名（用于部署成功后的“可访问 URL”拼接；若后端未实现部署，可暂时回退静态预览）
export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 静态资源地址（后端生成目录通常会被映射到该路径下）
export const STATIC_BASE_URL = `${API_BASE_URL}/static`

// 获取部署应用的完整URL
export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

// 获取静态资源预览URL
// 约定：{STATIC_BASE_URL}/{codeGenType}_{appId}/ 目录下包含 index.html
export const getStaticPreviewUrl = (codeGenType: string, appId: string) => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
  return baseUrl
}
