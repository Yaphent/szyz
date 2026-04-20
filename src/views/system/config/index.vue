<template>
  <div class="config-manage">
    <el-card>
      <!-- 查询条件 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryForm">
          <el-form-item label="参数名称">
            <el-input v-model="queryForm.configName" placeholder="请输入参数名称" clearable style="width: 150px" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="参数键名">
            <el-input v-model="queryForm.configKey" placeholder="请输入参数键名" clearable style="width: 150px" @keyup.enter="loadData" />
          </el-form-item>
          <el-form-item label="参数类型">
            <el-select v-model="queryForm.configType" placeholder="请选择" clearable style="width: 120px">
              <el-option label="字符串" value="S" />
              <el-option label="数值" value="N" />
              <el-option label="布尔" value="B" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadData">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      
      <!-- 操作按钮 -->
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增参数</el-button>
        <el-button :icon="Refresh" @click="handleRefreshCache">刷新缓存</el-button>
      </div>
      
      <!-- 参数列表 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="configId" label="参数ID" width="100" />
        <el-table-column prop="configName" label="参数名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="configKey" label="参数键名" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configValue" label="参数值" min-width="150" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.configType)" size="small">
              {{ getTypeLabel(row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入参数键名，如 sys.xxx" maxlength="100" :disabled="!!form.configId">
            <template #append>
              <el-tooltip content="建议使用前缀，如 sys、app 等">
                <el-icon><QuestionFilled /></el-icon>
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="参数值" prop="configValue">
          <el-input
            v-model="form.configValue"
            :placeholder="getValuePlaceholder()"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="参数类型" prop="configType">
          <el-radio-group v-model="form.configType">
            <el-radio label="S">字符串</el-radio>
            <el-radio label="N">数值</el-radio>
            <el-radio label="B">布尔</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" maxlength="200" show-word-limit />
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
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Refresh, QuestionFilled } from '@element-plus/icons-vue';
import { configApi } from '@/api';

const loading = ref(false);
const submitLoading = ref(false);
const dialogVisible = ref(false);
const dialogTitle = ref('');
const formRef = ref<any>();
const tableData = ref<any[]>([]);

const queryForm = reactive({
  configName: '',
  configKey: '',
  configType: ''
});

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
});

const form = reactive({
  configId: undefined as number | undefined,
  configName: '',
  configKey: '',
  configValue: '',
  configType: 'S',
  remark: '',
  status: 1
});

const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [
    { required: true, message: '请输入参数键名', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_.]*$/, message: '键名必须小写字母开头，支持字母、数字、下划线、点', trigger: 'blur' }
  ],
  configType: [{ required: true, message: '请选择参数类型', trigger: 'change' }]
};

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = { S: '字符串', N: '数值', B: '布尔' };
  return map[type] || type;
};

const getTypeTag = (type: string) => {
  const map: Record<string, string> = { S: '', N: 'success', B: 'warning' };
  return map[type] || 'info';
};

const getValuePlaceholder = () => {
  const map: Record<string, string> = {
    S: '请输入字符串类型的参数值',
    N: '请输入数值类型的参数值，如 100',
    B: '请输入布尔类型的参数值，如 true/false 或 0/1'
  };
  return map[form.configType] || '请输入参数值';
};

const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.page,
      pageSize: pagination.pageSize,
      config_name: queryForm.configName || undefined,
      config_key: queryForm.configKey || undefined,
      config_type: queryForm.configType || undefined
    };
    const res = await configApi.getPage(params);
    if (res.code === 200) {
      tableData.value = res.data.records || [];
      pagination.total = res.data.total || 0;
    } else {
      ElMessage.error(res.msg || '加载数据失败');
    }
  } catch {
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

const handleReset = () => {
  queryForm.configName = '';
  queryForm.configKey = '';
  queryForm.configType = '';
  pagination.page = 1;
  loadData();
};

const handleAdd = () => {
  dialogTitle.value = '新增参数';
  form.configId = undefined;
  form.configName = '';
  form.configKey = '';
  form.configValue = '';
  form.configType = 'S';
  form.remark = '';
  form.status = 1;
  dialogVisible.value = true;
};

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑参数';
  form.configId = row.configId;
  form.configName = row.configName;
  form.configKey = row.configKey;
  form.configValue = row.configValue;
  form.configType = row.configType;
  form.remark = row.remark || '';
  form.status = row.status;
  dialogVisible.value = true;
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      submitLoading.value = true;
      try {
        const res = form.configId
          ? await configApi.update(form)
          : await configApi.create(form);
        if (res.code === 200) {
          ElMessage.success(form.configId ? '修改成功' : '新增成功');
          dialogVisible.value = false;
          loadData();
        } else {
          ElMessage.error(res.msg || '操作失败');
        }
      } catch {
        ElMessage.error('操作失败');
      } finally {
        submitLoading.value = false;
      }
    }
  });
};

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除参数"${row.configName}"吗？`, '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await configApi.delete(row.configId);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        loadData();
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch {
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

const handleRefreshCache = async () => {
  try {
    const res = await configApi.refreshCache();
    if (res.code === 200) {
      ElMessage.success('缓存刷新成功');
    } else {
      ElMessage.error(res.msg || '刷新失败');
    }
  } catch {
    ElMessage.error('刷新失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.config-manage {
  height: 100%;
}

.config-manage :deep(.el-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.config-manage :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.search-bar {
  padding: 16px 16px 0;
}

.toolbar {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.config-manage :deep(.el-table) {
  flex: 1;
}

.config-manage :deep(.el-pagination) {
  flex-shrink: 0;
  padding: 16px;
}
</style>
