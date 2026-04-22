<template>
  <div class="menu-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增菜单</el-button>
        </div>
      </template>
      
      <!-- 菜单树 -->
      <el-table :data="tableData" v-loading="loading" border stripe row-key="menuId">
        <el-table-column prop="name" label="序号" width="60" align="center">
          <template #default="{ row }">
            {{ row.menuType === 'M' || row.menuType === 'C' ? row.sort : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜单名称" width="200" />
        <el-table-column prop="icon" label="图标" width="100">
          <template #default="{ row }">
            <el-icon v-if="row.icon"><component :is="row.icon" /></el-icon>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.menuType === 'M'" type="warning">目录</el-tag>
            <el-tag v-else-if="row.menuType === 'C'" type="success">菜单</el-tag>
            <el-tag v-else-if="row.menuType === 'F'" type="info">按钮</el-tag>
            <el-tag v-else type="danger">外链</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="150" />
        <el-table-column prop="component" label="组件路径" min-width="150" />
        <el-table-column prop="perms" label="权限标识" width="150" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'name', value: 'menuId', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
            clearable
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio label="M">目录</el-radio>
            <el-radio label="C">菜单</el-radio>
            <el-radio label="F">按钮</el-radio>
            <el-radio label="L">外链</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.menuType !== 'F'">
          <el-input v-model="form.icon" placeholder="请输入图标名称">
            <template #append>
              <el-icon><component :is="form.icon" v-if="form.icon" /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="路由路径" prop="path" v-if="form.menuType !== 'F'">
          <el-input v-model="form.path" placeholder="请输入路由路径" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 'C'">
          <el-input v-model="form.component" placeholder="请输入组件路径，如: system/user/index" />
        </el-form-item>
        <el-form-item label="打开方式" v-if="form.menuType === 'L'">
          <el-radio-group v-model="form.isFrame">
            <el-radio :label="0">新标签页打开</el-radio>
            <el-radio :label="1">嵌入式打开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 'F'">
          <el-input v-model="form.perms" placeholder="请输入权限标识，如: sys:user:add" />
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
import { menuApi } from '../../../api';

const loading = ref(false);
const tableData = ref<any[]>([]);
const dialogVisible = ref(false);
const dialogTitle = ref('新增菜单');
const formRef = ref();

const form = reactive<any>({
  parentId: 0,
  menuType: 'M',
  name: '',
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  status: 1
});

const rules = {
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }]
};

// 树形选择数据
const treeSelectData = computed(() => {
  return [{
    menuId: 0,
    name: '顶级菜单',
    children: tableData.value
  }];
});

const loadData = async () => {
  loading.value = true;
  try {
    const res = await menuApi.getTree();
    tableData.value = res.data;
  } catch (error) {
    console.error('加载数据失败', error);
  } finally {
    loading.value = false;
  }
};

const handleAdd = (parent: any) => {
  dialogTitle.value = '新增菜单';
  Object.assign(form, {
    menuId: undefined,
    parentId: parent?.menuId || 0,
    menuType: 'M',
    name: '',
    path: '',
    component: '',
    perms: '',
    icon: '',
    sort: 0,
    status: 1,
    isFrame: 0  // 默认新标签页打开
  });
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑菜单';
  Object.assign(form, { ...row });
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  
  try {
    if (form.menuId) {
      await menuApi.update(form);
      ElMessage.success('修改成功');
    } else {
      await menuApi.create(form);
      ElMessage.success('创建成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败');
  }
};

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定要删除该菜单吗？', '提示', { type: 'warning' });
  try {
    await menuApi.delete(row.menuId);
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
.menu-manage {
  height: 100%;
}

.menu-manage :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.menu-manage :deep(.el-card__body) {
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

.menu-manage :deep(.el-table) {
  flex: 1;
}
</style>
