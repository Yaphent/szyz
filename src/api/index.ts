import request from './request';

// 认证相关API
export const authApi = {
  // 获取验证码
  getCaptcha: () => request.get('/auth/captcha'),
  
  // 登录
  login: (data: { username: string; password: string; captchaCode: string; captchaKey: string }) => 
    request.post('/auth/login', data),
  
  // 获取用户信息
  getUserInfo: () => request.get('/auth/info'),
  
  // 登出
  logout: () => request.post('/auth/logout'),
  
  // 刷新缓存
  refreshCache: () => request.post('/auth/refresh-cache')
};

// 单位管理API
export const deptApi = {
  getTree: () => request.get('/dept/tree'),
  getById: (deptId: number) => request.get(`/dept/${deptId}`),
  create: (data: any) => request.post('/dept', data),
  update: (data: any) => request.put('/dept', data),
  delete: (deptId: number) => request.delete(`/dept/${deptId}`)
};

// 用户管理API
export const userApi = {
  getPage: (params: any) => request.get('/user/page', { params }),
  getById: (userId: number) => request.get(`/user/${userId}`),
  create: (data: any) => request.post('/user', data),
  update: (data: any) => request.put('/user', data),
  delete: (userId: number) => request.delete(`/user/${userId}`),
  updateStatus: (userId: number, status: number) => request.put('/user/status', { user_id: userId, status }),
  assignRoles: (userId: number, roleIds: number[]) => request.post('/user/assign-roles', { user_id: userId, role_ids: roleIds }),
  assignDepts: (userId: number, deptIds: number[]) => request.post('/user/assign-depts', { user_id: userId, dept_ids: deptIds }),
  changePassword: (oldPassword: string, newPassword: string) => 
    request.put('/user/change-password', { oldPassword, newPassword }),
  resetPassword: (userId: number, newPassword?: string) => 
    request.put(`/user/reset-password/${userId}`, { newPassword }),
  uploadAvatar: (file: File) => {
    const formData = new FormData();
    formData.append('avatar', file);
    return request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  }
};

// 角色管理API
export const roleApi = {
  getPage: (params: any) => request.get('/role/page', { params }),
  getAll: () => request.get('/role/all'),
  getById: (roleId: number) => request.get(`/role/${roleId}`),
  create: (data: any) => request.post('/role', data),
  update: (data: any) => request.put('/role', data),
  delete: (roleId: number) => request.delete(`/role/${roleId}`),
  assignMenus: (roleId: number, menuIds: number[]) => 
    request.post('/role/assign-menus', { role_id: roleId, menu_ids: menuIds })
};

// 菜单管理API
export const menuApi = {
  getTree: () => request.get('/menu/tree'),
  getRoute: () => request.get('/menu/route'),
  getById: (menuId: number) => request.get(`/menu/${menuId}`),
  create: (data: any) => request.post('/menu', data),
  update: (data: any) => request.put('/menu', data),
  delete: (menuId: number) => request.delete(`/menu/${menuId}`)
};

// 仪表盘API
export const dashboardApi = {
  getStatistics: () => request.get('/dashboard/statistics')
};
