
/* eslint-disable */
import request from '@/request'

/**
 * 用户 - 应用相关接口
 */

/** POST /app/user/add - 创建应用，返回应用 id */
export async function addApp(body: API.AppAddRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseLong>('/app/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/user/update - 更新自己的应用名称 */
export async function updateAppName(body: API.AppUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/user/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/user/delete - 删除自己的应用 */
export async function deleteMyApp(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/user/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** GET /app/user/get?id=... - 查询自己的应用详情（实体） */
export async function getMyAppById(
  params: API.getAppByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseApp>('/app/user/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** POST /app/user/list/page - 分页查询自己的应用列表（实体） */
export async function listMyAppsByPage(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageApp>('/app/user/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/user/list/page/good - 分页查询精选应用列表（实体） */
export async function listGoodAppsByPage(body: API.AppQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageApp>('/app/user/list/page/good', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/**
 * 通用 - VO 详情（包含用户信息）
 */

/** GET /app/get/vo?id=... - 根据 id 获取应用详情（VO） */
export async function getAppVOById(
  params: API.getAppVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO>('/app/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/**
 * 管理员 - 应用相关接口
 */

/** POST /app/admin/delete - 管理员删除应用 */
export async function deleteAppByAdmin(body: API.DeleteRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/admin/update - 管理员更新应用（支持 appName/cover/priority） */
export async function updateAppByAdmin(body: API.AppAdminUpdateRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponseBoolean>('/app/admin/update', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** POST /app/admin/list/page - 管理员分页查询应用列表（实体，支持 isDelete） */
export async function listAppsByPageByAdmin(body: API.AppAdminQueryRequest, options?: { [key: string]: any }) {
  return request<API.BaseResponsePageApp>('/app/admin/list/page', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  })
}

/** GET /app/admin/get?id=... - 管理员根据 id 查看应用详情（实体） */
export async function getAppByIdByAdmin(
  params: API.getAppByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseApp>('/app/admin/get', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/**
 * 管理员 - VO 详情（包含用户信息）
 */

/** GET /app/admin/get/vo?id=... - 管理员根据 id 获取应用详情（VO） */
export async function getAppVOByIdByAdmin(
  params: API.getAppVOByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAppVO>('/app/admin/get/vo', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

