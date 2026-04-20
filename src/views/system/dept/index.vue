<template>
  <div class="dept-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增单位</el-button>
        </div>
      </template>
      
      <!-- 单位树 -->
      <el-table :data="tableData" v-loading="loading" border stripe row-key="deptId" default-expand-all>
        <el-table-column prop="deptName" label="单位名称" min-width="200" />
        <el-table-column prop="deptCode" label="单位编码" width="150" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="联系电话" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">新增</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级单位">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            placeholder="请选择上级单位"
            clearable
          />
        </el-form-item>
        <el-form-item label="单位名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入单位名称" />
        </el-form-item>
        <el-form-item label="单位编码">
          <el-input v-model="form.deptCode" placeholder="请输入单位编码" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import { deptApi } from '../../../api';

const loading = ref(false);
const tableData = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref('新增单位');
const formRef = ref();

const form = reactive<any>({
  parentId: 0,
  deptName: '',
  deptCode: '',
  leader: '',
  phone: '',
  sort: 0,
  status: 1
});

const rules = {
  deptName: [{ required: true, message: '请输入单位名称', trigger: 'blur' }]
};

// 树形选择数据
const treeSelectData = computed(() => {
  return [{
    deptId: 0,
    deptName: '顶级单位',
    children: tableData.value
  }];
});

const loadData = async () => {
  loading.value = true;
  try {
    const res = await deptApi.getTree();
    tableData.value = res.data;
  } catch (error) {
    console.error('加载数据失败', error);
  } finally {
    loading.value = false;
  }
};

const handleAdd = (parent: any) => {
  dialogTitle.value = '新增单位';
  Object.assign(form, {
    deptId: undefined,
    parentId: parent?.deptId || 0,
    deptName: '',
    deptCode: '',
    leader: '',
    phone: '',
    sort: 0,
    status: 1
  });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑单位';
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  try {
    if (form.deptId) {
      await deptApi.update(form);
      ElMessage.success('修改成功');
    } else {
      await deptApi.create(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该单位吗？', '提示', { type: 'warning' });
  try {
    await deptApi.delete(row.deptId);
    ElMessage.success('删除成功');
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '删除失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.dept-manage {
  height: 100%;
}

.dept-manage :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.dept-manage :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.dept-manage :deep(.el-table) {
  flex: 1;
}
</style>
