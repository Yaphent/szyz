import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router';
import { useUserStore } from '../store/user';

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../views/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'system/user',
        name: 'UserManage',
        component: () => import('../views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/role',
        name: 'RoleManage',
        component: () => import('../views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'Wallet' }
      },
      {
        path: 'system/menu',
        name: 'MenuManage',
        component: () => import('../views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/dept',
        name: 'DeptManage',
        component: () => import('../views/system/dept/index.vue'),
        meta: { title: '单位管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'system/config',
        name: 'ConfigManage',
        component: () => import('../views/system/config/index.vue'),
        meta: { title: '参数管理', icon: 'Tools' }
      },
      // ================ 法规文档管理 ================
      {
        path: 'document',
        name: 'DocumentList',
        component: () => import('../views/document/DocumentList.vue'),
        meta: { title: '法规文档', icon: 'Document' }
      },
      {
        path: 'document/detail/:id',
        name: 'DocumentDetail',
        component: () => import('../views/document/DocumentDetail.vue'),
        props: true,
        meta: { title: '文档详情', icon: 'Document', hidden: true, activeMenu: '/document' }
      }
    ]
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore();
  const token = localStorage.getItem('token');
  
  if (to.path === '/login') {
    next();
  } else if (!token) {
    next('/login');
  } else {
    next();
  }
});

export default router;
