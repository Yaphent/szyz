<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryForm.mobile" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 用户列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="mobile" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAssignRoles(row)">分配角色</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.userId === 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.userId" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="mobile">
          <el-input v-model="form.mobile" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.nickname }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox v-for="role in allRoles" :key="role.roleId" :label="role.roleId">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitRoles">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { userApi, roleApi } from '../../../api';

const loading = ref(false);
const tableData = ref([]);
const dialogVisible = ref(false);
const roleDialogVisible = ref(false);
const dialogTitle = ref('新增用户');
const formRef = ref();
const currentUser = ref<any>(null);
const allRoles = ref<any[]>([]);
const selectedRoleIds = ref<number[]>([]);

const queryForm = reactive({
  username: '',
  mobile: '',
  status: undefined
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

const form = reactive<any>({
  username: '',
  nickname: '',
  mobile: '',
  email: '',
  status: 1
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await userApi.getPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...queryForm
    });
    tableData.value = res.data.list;
    pagination.total = res.data.total;
  } catch (error) {
    console.error('加载数据失败', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.page = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(queryForm, { username: '', mobile: '', status: undefined });
  handleSearch();
};

const handleAdd = () => {
  dialogTitle.value = '新增用户';
  Object.assign(form, {
    userId: undefined,
    username: '',
    nickname: '',
    mobile: '',
    email: '',
    status: 1
  });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑用户';
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  try {
    if (form.userId) {
      await userApi.update(form);
      ElMessage.success('修改成功');
    } else {
      await userApi.create(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' });
  try {
    await userApi.delete(row.userId);
    ElMessage.success('删除成功');
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败');
  }
};

const handleAssignRoles = async (row: any) => {
  currentUser.value = row;
  selectedRoleIds.value = row.roleIds || [];
  
  // 加载所有角色
  if (allRoles.value.length === 0) {
    const res = await roleApi.getAll();
    allRoles.value = res.data;
  }
  
  // 加载用户详情
  const detailRes = await userApi.getById(row.userId);
  selectedRoleIds.value = detailRes.data.roleIds || [];
  
  roleDialogVisible.value = true;
};

const handleSubmitRoles = async () => {
  try {
    await userApi.assignRoles(currentUser.value.userId, selectedRoleIds.value);
    ElMessage.success('分配成功');
    roleDialogVisible.value = false;
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.user-manage {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
