<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>企业级权限管理平台</h1>
        <p>Enterprise Permission Management System</p>
      </div>
      
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <el-form-item prop="captchaCode">
          <el-input 
            v-model="loginForm.captchaCode" 
            placeholder="验证码"
            prefix-icon="CircleCheck"
            size="large"
            style="width: 60%"
            @keyup.enter="handleLogin"
          />
          <div class="captcha-image" @click="refreshCaptcha">
            <img v-if="captchaImage" :src="captchaImage" alt="验证码" />
            <span v-else>加载中...</span>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            style="width: 100%"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        
        <div class="login-tips">
          <p>默认账号: admin / admin123</p>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { authApi, menuApi } from '../../api';
import { useUserStore } from '../../store/user';

const router = useRouter();
const userStore = useUserStore();

const loginFormRef = ref();
const loading = ref(false);
const captchaKey = ref('');
const captchaImage = ref('');

const loginForm = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: ''
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
};

// 获取验证码
const getCaptcha = async () => {
  try {
    const res = await authApi.getCaptcha();
    captchaKey.value = res.data.captchaKey;
    loginForm.captchaKey = res.data.captchaKey;
    // 验证码是base64图片
    captchaImage.value = res.data.captchaImage;
  } catch (error) {
    console.error('获取验证码失败', error);
  }
};

// 刷新验证码
const refreshCaptcha = () => {
  getCaptcha();
};

// 登录
const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false);
  if (!valid) return;
  
  loading.value = true;
  
  try {
    const res = await authApi.login(loginForm);
    
    userStore.setToken(res.data.token);
    userStore.setUserInfo(res.data.userInfo);
    
    // 获取用户信息和权限
    const infoRes = await authApi.getUserInfo();
    userStore.setPerms(infoRes.data.perms);
    
    // 获取用户菜单
    const menuRes = await menuApi.getRoute();
    userStore.setMenus(menuRes.data || []);
    
    ElMessage.success('登录成功');
    router.push('/');
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败');
    refreshCaptcha();
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  getCaptcha();
});
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0f3e7a 0%, #1a5fb4 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 24px;
  color: #0f3e7a;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 12px;
  color: #999;
}

.captcha-image {
  width: 38%;
  height: 40px;
  margin-left: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f5f7fa;
}

.captcha-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.login-tips {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 10px;
}
</style>
