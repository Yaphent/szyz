<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: #409eff">
            <el-icon :size="32"><User /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">用户总数</p>
            <p class="stat-value">{{ statistics.totalUser }}</p>
          </div>
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: #67c23a">
            <el-icon :size="32"><OfficeBuilding /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">单位总数</p>
            <p class="stat-value">{{ statistics.totalDept }}</p>
          </div>
        </div>
      </el-col>
      
      <el-col :span="8">
        <div class="stat-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon :size="32"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <p class="stat-label">角色总数</p>
            <p class="stat-value">{{ statistics.totalRole }}</p>
          </div>
        </div>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="welcome-card">
          <template #header>
            <span>欢迎使用企业级权限管理平台</span>
          </template>
          <div class="welcome-content">
            <p>您好，<strong>{{ userStore.userInfo?.nickname }}</strong>！</p>
            <p>当前角色：<el-tag type="success">{{ userStore.userInfo?.username }}</el-tag></p>
            <p>欢迎回来，上次登录时间：{{ lastLoginTime }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { dashboardApi } from '../../api';
import { useUserStore } from '../../store/user';

const userStore = useUserStore();

const statistics = reactive({
  totalUser: 0,
  totalDept: 0,
  totalRole: 0
});

const lastLoginTime = ref('');

const getStatistics = async () => {
  try {
    const res = await dashboardApi.getStatistics();
    Object.assign(statistics, res.data);
  } catch (error) {
    console.error('获取统计数据失败', error);
  }
};

onMounted(() => {
  getStatistics();
  lastLoginTime.value = new Date().toLocaleString('zh-CN');
});
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.welcome-card {
  border-radius: 8px;
}

.welcome-content {
  line-height: 2;
  font-size: 14px;
  color: #606266;
}

.welcome-content strong {
  color: #0f3e7a;
  font-size: 16px;
}
</style>
