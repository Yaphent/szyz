<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="queryForm.roleName" placeholder="请输入角色名称" clearable />
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
      
      <!-- 角色列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="roleId" label="角色ID" width="100" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" @click="handleAssignMenus(row)">分配菜单</el-button>
            <el-button link type="danger" @click="handleDelete(row)" :disabled="row.roleId === 1">删除</el-button>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="!!form.roleId" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 分配菜单弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="600px">
      <el-form label-width="80px">
        <el-form-item label="角色">
          <span>{{ currentRole?.roleName }}</span>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
            ref="menuTreeRef"
            :data="menuTree"
            :props="{ label: 'name', children: 'children' }"
            node-key="menuId"
            show-checkbox
            default-expand-all
            check-strictly
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMenus">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import type { ElTree } from 'element-plus';
import { roleApi, menuApi } from '../../../api';

const loading = ref(false);
const tableData = ref([]);
const dialogVisible = ref(false);
const menuDialogVisible = ref(false);
const dialogTitle = ref('新增角色');
const formRef = ref();
const menuTreeRef = ref<InstanceType<typeof ElTree>>();
const currentRole = ref<any>(null);
const menuTree = ref<any[]>([]);

const queryForm = reactive({
  roleName: '',
  status: undefined
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

const form = reactive<any>({
  roleName: '',
  roleCode: '',
  status: 1,
  remark: ''
});

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await roleApi.getPage({
      page: pagination.page,
      pageSize: pagination.pageSize,
      role_name: queryForm.roleName,
      status: queryForm.status
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
  Object.assign(queryForm, { roleName: '', status: undefined });
  handleSearch();
};

const handleAdd = () => {
  dialogTitle.value = '新增角色';
  Object.assign(form, { roleId: undefined, roleName: '', roleCode: '', status: 1, remark: '' });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色';
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  try {
    if (form.roleId) {
      await roleApi.update(form);
      ElMessage.success('修改成功');
    } else {
      await roleApi.create(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' });
  try {
    await roleApi.delete(row.roleId);
    ElMessage.success('删除成功');
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败');
  }
};

const handleAssignMenus = async (row: any) => {
  currentRole.value = row;
  
  // 加载菜单树
  if (menuTree.value.length === 0) {
    const menuRes = await menuApi.getTree();
    menuTree.value = menuRes.data;
  }
  
  // 加载角色已有菜单
  const detailRes = await roleApi.getById(row.roleId);
  const checkedKeys = detailRes.data.menuIds || [];
  
  menuDialogVisible.value = true;
  
  // 设置选中状态
  setTimeout(() => {
    menuTreeRef.value?.setCheckedKeys(checkedKeys);
  }, 100);
};

const handleSubmitMenus = async () => {
  const checkedKeys = menuTreeRef.value?.getCheckedKeys() || [];
  try {
    await roleApi.assignMenus(currentRole.value.roleId, checkedKeys);
    ElMessage.success('分配成功');
    menuDialogVisible.value = false;
  } catch (error: any) {
    ElMessage.error(error.message || '分配失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.role-manage {
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
