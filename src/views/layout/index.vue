<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="aside">
      <div class="logo">
        <el-icon :size="28" color="#409eff"><ElementPlus /></el-icon>
        <span v-if="!isCollapse">{{ systemName }}</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :router="true"
        background-color="#1d1e23"
        text-color="#a0a5b0"
        active-text-color="#fff"
      >
        <!-- 遍历用户菜单 -->
        <template v-for="menu in userMenus" :key="menu.menuId">
          <!-- 有子菜单的目录 -->
          <el-sub-menu v-if="hasVisibleChildren(menu)" :index="menu.path || '/'+menu.menuId">
            <template #title>
              <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item 
              v-for="child in getVisibleChildren(menu)" 
              :key="child.menuId" 
              :index="child.path"
            >
              {{ child.name }}
            </el-menu-item>
          </el-sub-menu>
          
          <!-- 没有子菜单的菜单项 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
            <template #title>{{ menu.name }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 主内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessageBox } from 'element-plus';
import { useUserStore } from '../../store/user';
import { authApi, menuApi } from '../../api';
import {
  Odometer, Setting, User, Wallet, Menu, OfficeBuilding, Tools,
  Fold, Expand, ArrowDown, ElementPlus
} from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isCollapse = ref(false);
const activeMenu = computed(() => route.path);
const systemName = computed(() => userStore.getConfig('sys.system.name', '安阳智能监督平台'));

// 图标映射
const iconMap: Record<string, any> = {
  'Odometer': Odometer,
  'Setting': Setting,
  'User': User,
  'Wallet': Wallet,
  'Menu': Menu,
  'OfficeBuilding': OfficeBuilding,
  'Tools': Tools
};

const getIcon = (iconName?: string) => {
  return iconMap[iconName || ''] || Menu;
};

// 用户菜单
const userMenus = computed(() => {
  // 过滤出菜单类型的菜单（不包括按钮和外链）
  return userStore.menus.filter(menu => {
    // 排除按钮类型（F）
    if (menu.menuType === 'F') return false;
    // 排除外链（isFrame = 1）
    if (menu.isFrame === 1 || menu.isFrame === '1') return false;
    return true;
  });
});

// 辅助函数：获取可见的子菜单
const getVisibleChildren = (menu: any) => {
  if (!menu.children) return [];
  return menu.children.filter((child: any) => {
    // 排除按钮类型（F）
    if (child.menuType === 'F') return false;
    // 排除外链（isFrame = 1）
    if (child.isFrame === 1 || child.isFrame === '1') return false;
    return true;
  });
};

// 辅助函数：判断菜单是否有可见的子菜单
const hasVisibleChildren = (menu: any) => {
  return getVisibleChildren(menu).length > 0;
};

const handleCommand = async (command: string) => {
  if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      type: 'warning'
    });
    
    try {
      await authApi.logout();
    } catch (e) {
      // 忽略错误
    }
    
    userStore.logout();
    router.push('/login');
  }
};

// 组件挂载时获取菜单
onMounted(async () => {
  // 如果有 token 但没有菜单，则获取菜单
  if (userStore.token && userStore.menus.length === 0) {
    try {
      const menuRes = await menuApi.getRoute();
      userStore.setMenus(menuRes.data || []);
    } catch (e) {
      console.error('获取菜单失败', e);
    }
  }
});
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #1d1e23;
  overflow-x: hidden;
  overflow-y: auto;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #333;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  cursor: pointer;
  font-size: 18px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main {
  background-color: #f5f7fa;
  padding: 16px;
  overflow-y: auto;
}

/* 菜单样式 */
:deep(.el-menu) {
  border-right: none;
}

:deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 48px !important;
}
</style>
