import { defineStore } from 'pinia';
import { ref } from 'vue';

interface MenuItem {
  menuId: number;
  parentId: number;
  name: string;
  path: string;
  component?: string;
  icon?: string;
  menuType: string;
  children?: MenuItem[];
}

interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  email?: string;
  mobile?: string;
  avatar?: string;
  deptId?: number;
  deptName?: string;
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'));
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'));
  const perms = ref<string[]>([]);
  const menus = ref<MenuItem[]>([]);

  function setToken(newToken: string) {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info;
    localStorage.setItem('userInfo', JSON.stringify(info));
  }

  function setPerms(newPerms: string[]) {
    perms.value = newPerms;
  }

  function setMenus(newMenus: MenuItem[]) {
    menus.value = newMenus;
    localStorage.setItem('menus', JSON.stringify(newMenus));
  }

  function hasPerm(perm: string | string[]): boolean {
    if (Array.isArray(perm)) {
      return perm.some(p => perms.value.includes(p));
    }
    return perms.value.includes(perm);
  }

  function logout() {
    token.value = null;
    userInfo.value = null;
    perms.value = [];
    menus.value = [];
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('menus');
  }

  return {
    token,
    userInfo,
    perms,
    menus,
    setToken,
    setUserInfo,
    setPerms,
    setMenus,
    hasPerm,
    logout
  };
});
