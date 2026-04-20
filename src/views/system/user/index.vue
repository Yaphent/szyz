<template>
  <div class="user-manage">
    <el-card class="user-card">
      <el-container>
        <!-- 左侧单位树 -->
        <el-aside width="280px" class="dept-aside">
          <div class="dept-header">
            <span>所属单位</span>
            <el-button link type="primary" @click="handleRefreshDept" :icon="Refresh" />
          </div>
          <el-tree
            ref="deptTreeRef"
            :data="deptTree"
            :props="{ label: 'deptName', children: 'children' }"
            node-key="deptId"
            default-expand-all
            highlight-current
            :expand-on-click-node="false"
            @node-click="handleDeptSelect"
          >
            <template #default="{ node, data }">
              <span class="dept-node">
                <span>[{{ data.deptCode }}] {{ data.deptName }}</span>
                <span class="dept-count">({{ data.userCount || 0 }})</span>
              </span>
            </template>
          </el-tree>
        </el-aside>
        
        <!-- 右侧用户列表 -->
        <el-main class="user-main">
          <div class="card-header">
            <span>用户列表</span>
            <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          </div>
          
          <!-- 搜索表单 -->
          <el-form :inline="true" :model="queryForm" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="queryForm.username" placeholder="请输入用户名" clearable style="width: 150px" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="queryForm.mobile" placeholder="请输入手机号" clearable style="width: 150px" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px">
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
            <el-table-column prop="deptName" label="所属单位" width="180">
              <template #default="{ row }">
                {{ row.deptCode ? '[' + row.deptCode + '] ' : '' }}{{ row.deptName || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="mobile" label="手机号" width="130" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="170" />
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
                <el-button link type="primary" @click="handleAssign(row)">分配</el-button>
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
        </el-main>
      </el-container>
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
        <el-form-item label="所属单位" prop="deptId">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            placeholder="请选择所属单位"
            check-strictly
            :render-after-expand="false"
          />
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
    
    <!-- 分配角色和管辖单位弹窗 -->
    <el-dialog v-model="assignDialogVisible" title="分配角色 & 授权管辖单位" width="600px" destroy-on-close>
      <el-form label-width="100px">
        <el-form-item label="用户">
          <span>{{ currentUser?.nickname }} ({{ currentUser?.username }})</span>
        </el-form-item>
        <el-form-item label="分配角色">
          <el-checkbox-group v-model="selectedRoleIds">
            <el-checkbox v-for="role in allRoles" :key="role.roleId" :label="role.roleId">
              {{ role.roleName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="授权管辖单位">
          <el-tree
            ref="assignDeptTreeRef"
            :data="deptTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="deptId"
            show-checkbox
            :default-expand-all="true"
            empty-text="加载中..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh } from '@element-plus/icons-vue';
import { userApi, roleApi, deptApi } from '../../../api';
import type { ElTree } from 'element-plus';

const loading = ref(false);
const tableData = ref<any[]>([]);
const dialogVisible = ref(false);
const assignDialogVisible = ref(false);
const dialogTitle = ref('新增用户');
const formRef = ref<any>();
const deptTreeRef = ref<any>();
const assignDeptTreeRef = ref<any>();
const currentUser = ref<any>(null);
const allRoles = ref<any[]>([]);
const selectedRoleIds = ref<number[]>([]);
const selectedDeptId = ref<number | null>(null);

const deptTree = ref<any[]>([]);

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
  userId: undefined,
  username: '',
  nickname: '',
  deptId: null,
  mobile: '',
  email: '',
  status: 1
});

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属单位', trigger: 'change' }]
};

// 加载单位树
const loadDeptTree = async () => {
  try {
    const res = await deptApi.getTree();
    deptTree.value = res.data || [];
  } catch (error) {
    console.error('加载单位树失败', error);
  }
};

// 刷新单位树
const handleRefreshDept = () => {
  loadDeptTree();
};

// 选择单位
const handleDeptSelect = (data: any) => {
  selectedDeptId.value = data.deptId;
  pagination.page = 1;
  loadData();
};

// 加载用户列表
const loadData = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      ...queryForm
    };
    if (selectedDeptId.value) {
      params.deptId = selectedDeptId.value;
    }
    const res = await userApi.getPage(params);
    tableData.value = res.data.list || [];
    pagination.total = res.data.total || 0;
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
  selectedDeptId.value = null;
  deptTreeRef.value?.setCurrentKey(null);
  handleSearch();
};

const handleAdd = () => {
  dialogTitle.value = '新增用户';
  Object.assign(form, {
    userId: undefined,
    username: '',
    nickname: '',
    deptId: selectedDeptId.value || null,
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
    loadDeptTree(); // 刷新单位树（更新用户数）
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
    loadDeptTree();
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败');
  }
};

// 分配角色和管辖单位
const handleAssign = async (row: any) => {
  currentUser.value = row;
  
  // 加载所有角色
  if (allRoles.value.length === 0) {
    const res = await roleApi.getAll();
    allRoles.value = res.data || [];
  }
  
  // 加载用户详情（包含角色和管辖单位）
  try {
    const detailRes = await userApi.getById(row.userId);
    selectedRoleIds.value = detailRes.data.roleIds || [];
    
    // 设置管辖单位复选框
    await nextTick();
    const deptIds = detailRes.data.deptIds || [];
    assignDeptTreeRef.value?.setCheckedKeys(deptIds);
  } catch (error) {
    selectedRoleIds.value = [];
  }
  
  assignDialogVisible.value = true;
};

const handleSubmitAssign = async () => {
  try {
    const roleRes = userApi.assignRoles(currentUser.value.userId, selectedRoleIds.value);
    
    // 获取选中的管辖单位
    const checkedDepts = assignDeptTreeRef.value?.getCheckedKeys(true) || [];
    const deptRes = userApi.assignDepts(currentUser.value.userId, checkedDepts);
    
    await Promise.all([roleRes, deptRes]);
    
    ElMessage.success('分配成功');
    assignDialogVisible.value = false;
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败');
  }
};

onMounted(() => {
  loadDeptTree();
  loadData();
});
</script>

<style scoped>
.user-manage {
  height: 100%;
}

.user-card {
  height: 100%;
}

.user-card :deep(.el-card__body) {
  height: 100%;
  padding: 0;
}

.user-card :deep(.el-container) {
  height: 100%;
}

.dept-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  padding: 16px;
  overflow-y: auto;
}

.dept-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 500;
}

.dept-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.dept-count {
  color: #999;
  font-size: 12px;
}

.user-main {
  padding: 16px;
  background: #f5f7fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.search-form {
  background: #fff;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 12px;
}
</style>
