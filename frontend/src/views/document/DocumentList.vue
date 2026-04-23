<template>
  <div class="document-list">
    <el-card class="doc-card">
      <!-- 顶部：搜索筛选区 -->
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="文档名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入文档名称"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="政策文号">
          <el-input
            v-model="queryForm.policyNo"
            placeholder="请输入政策文号"
            clearable
            style="width: 160px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="业务分类">
          <el-select
            v-model="queryForm.businessType"
            placeholder="请选择"
            clearable
            style="width: 140px"
          >
            <el-option
              v-for="item in BUSINESS_TYPE_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="发文日期">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.status"
            placeholder="全部"
            clearable
            style="width: 100px"
          >
            <el-option
              v-for="item in STATUS_OPTIONS"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="RefreshLeft" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 中部：操作按钮栏 -->
      <div class="toolbar">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
        <el-button
          type="danger"
          :icon="Delete"
          :disabled="!selection.length"
          @click="handleBatchDelete"
        >
          批量删除
        </el-button>
        <el-button
          :icon="CircleClose"
          :disabled="!selection.length"
          @click="handleBatchStatus(0)"
        >
          批量停用
        </el-button>
        <el-button
          type="success"
          :icon="CircleCheck"
          :disabled="!selection.length"
          @click="handleBatchStatus(1)"
        >
          批量启用
        </el-button>
        <el-button :icon="Download" @click="handleExport">导出</el-button>
      </div>

      <!-- 主体：数据表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="onSelectionChange"
      >
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column type="index" label="序号" width="60" align="center">
          <template #default="{ $index }">
            {{ (pagination.pageNum - 1) * pagination.pageSize + $index + 1 }}
          </template>
        </el-table-column>

        <el-table-column prop="name" label="文档名称" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>

        <el-table-column label="政策文号" width="180">
          <template #default="{ row }">
            <span>{{ formatPolicyNo(row) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="businessType" label="业务分类" width="120">
          <template #default="{ row }">
            {{ BUSINESS_TYPE_MAP[row.businessType] || row.businessType || '-' }}
          </template>
        </el-table-column>

        <el-table-column
          prop="issuingAuthority"
          label="发文机关"
          width="160"
          show-overflow-tooltip
        />

        <el-table-column prop="issuingDate" label="发文日期" width="120" sortable />
        <el-table-column prop="effectiveDate" label="实施日期" width="120" sortable />
        <el-table-column prop="expiryDate" label="失效日期" width="120">
          <template #default="{ row }">{{ row.expiryDate || '-' }}</template>
        </el-table-column>

        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              :effect="row.status === 1 ? 'light' : 'plain'"
            >
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="createTime" label="创建时间" width="170" />

        <!-- Dify 处理状态 -->
        <el-table-column label="Dify状态" width="180">
          <template #default="{ row }">
            <div class="dify-status-cell">
              <el-tag 
                :type="getSummaryStatusType(row.summaryStatus)" 
                size="small"
                effect="plain"
                :title="'摘要: ' + getSummaryStatusText(row.summaryStatus)"
              >
                摘要: {{ getSummaryStatusText(row.summaryStatus) }}
              </el-tag>
              <el-tag 
                :type="getPipelineStatusType(row.difyPipelineStatus)" 
                size="small"
                effect="plain"
                :title="getPipelineStatusText(row.difyPipelineStatus)"
              >
                解析: {{ getPipelineStatusText(row.difyPipelineStatus) }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 1 ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button link type="info" @click="handleRunPipeline(row)">解析</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 底部：分页 -->
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        class="pagination"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 法规文档 —— 列表页
 *
 * 功能：
 * 1. 顶部搜索筛选（名称、政策文号、业务分类、发文日期范围、状态）
 * 2. 工具栏操作（新增、批量删除、批量停用、批量启用、导出）
 * 3. 表格：多选、分页、行内查看/编辑/停启用/删除
 */
import { reactive, ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Search,
  RefreshLeft,
  Plus,
  Delete,
  CircleCheck,
  CircleClose,
  Download
} from '@element-plus/icons-vue';
import { documentApi, type DocumentQuery } from '../../api/document';
import {
  BUSINESS_TYPE_OPTIONS,
  BUSINESS_TYPE_MAP,
  STATUS_OPTIONS
} from '../../constants/document';

const router = useRouter();

// -------- 查询表单 --------
const queryForm = reactive<DocumentQuery>({
  name: '',
  policyNo: '',
  businessType: '',
  status: undefined,
  startDate: '',
  endDate: ''
});
const dateRange = ref<[string, string] | null>(null);

// 日期联动
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryForm.startDate = val[0];
    queryForm.endDate = val[1];
  } else {
    queryForm.startDate = '';
    queryForm.endDate = '';
  }
});

// -------- 表格 --------
const loading = ref(false);
const tableData = ref<any[]>([]);
const selection = ref<any[]>([]);

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
});

// 选中的 ID 列表
const selectedIds = computed(() => selection.value.map((r: any) => r.id));

const onSelectionChange = (rows: any[]) => {
  selection.value = rows;
};

/**
 * 政策文号渲染，兼容不同字段组合
 */
const formatPolicyNo = (row: any): string => {
  const prefix = row.policyPrefix || '';
  const year = row.policyYear || '';
  const no = row.policyNo || '';
  if (!prefix && !year && !no) return '-';
  return `${prefix}${year}${no}`;
};

// ============ Dify 状态显示方法 ============
const getSummaryStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待处理',
    1: '处理中',
    2: '已完成',
    3: '失败'
  };
  return statusMap[status] || '未知';
};

const getSummaryStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return typeMap[status] || 'info';
};

const getPipelineStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待解析',
    1: '解析中',
    2: '解析成功',
    3: '解析失败'
  };
  return statusMap[status] || '未知';
};

const getPipelineStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return typeMap[status] || 'info';
};



// -------- 数据加载 --------
const loadData = async () => {
  loading.value = true;
  try {
    const params: DocumentQuery = {
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    };
    // 清理空字段
    Object.keys(params).forEach((k) => {
      const v = (params as any)[k];
      if (v === '' || v === null || v === undefined) {
        delete (params as any)[k];
      }
    });
    const res: any = await documentApi.getPage(params);
    tableData.value = res.data?.list || [];
    pagination.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载文档列表失败', e);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.pageNum = 1;
  loadData();
};

const handleReset = () => {
  Object.assign(queryForm, {
    name: '',
    policyNo: '',
    businessType: '',
    status: undefined,
    startDate: '',
    endDate: ''
  });
  dateRange.value = null;
  handleSearch();
};

// -------- 新增 / 查看 / 编辑 --------
const handleAdd = () => {
  router.push({ name: 'DocumentDetail', params: { id: 'new' } });
};

const handleView = (row: any) => {
  router.push({
    name: 'DocumentDetail',
    params: { id: String(row.id) },
    query: { mode: 'view' }
  });
};

const handleEdit = (row: any) => {
  router.push({
    name: 'DocumentDetail',
    params: { id: String(row.id) },
    query: { mode: 'edit' }
  });
};

// -------- 删除 --------
const handleDelete = async (row: any) => {
  await ElMessageBox.confirm(
    `确定删除文档"${row.name}"吗？此操作不可恢复。`,
    '删除确认',
    { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
  );
  try {
    await documentApi.remove(row.id);
    ElMessage.success('删除成功');
    // 当前页无数据时回退一页
    if (tableData.value.length === 1 && pagination.pageNum > 1) {
      pagination.pageNum -= 1;
    }
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.message || '删除失败');
  }
};

const handleBatchDelete = async () => {
  if (!selectedIds.value.length) return;
  await ElMessageBox.confirm(
    `确定删除选中的 ${selectedIds.value.length} 条文档吗？`,
    '批量删除确认',
    { type: 'warning' }
  );
  try {
    await documentApi.removeBatch(selectedIds.value);
    ElMessage.success('批量删除成功');
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.message || '批量删除失败');
  }
};

// -------- 状态切换 --------
const handleToggleStatus = async (row: any) => {
  const nextStatus = row.status === 1 ? 0 : 1;
  const actionText = nextStatus === 1 ? '启用' : '停用';
  await ElMessageBox.confirm(`确定${actionText}文档"${row.name}"吗？`, `${actionText}确认`, {
    type: 'warning'
  });
  try {
    await documentApi.updateStatus([row.id], nextStatus);
    ElMessage.success(`${actionText}成功`);
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.message || `${actionText}失败`);
  }
};

const handleBatchStatus = async (status: 0 | 1) => {
  if (!selectedIds.value.length) return;
  const actionText = status === 1 ? '启用' : '停用';
  await ElMessageBox.confirm(
    `确定${actionText}选中的 ${selectedIds.value.length} 条文档吗？`,
    `批量${actionText}`,
    { type: 'warning' }
  );
  try {
    await documentApi.updateStatus(selectedIds.value, status);
    ElMessage.success(`批量${actionText}成功`);
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.message || '操作失败');
  }
};

// -------- 导出（前端简单导出当前页 CSV，后端无对应接口时作为兜底） --------
const handleExport = () => {
  if (!tableData.value.length) {
    ElMessage.warning('暂无可导出的数据');
    return;
  }
  const headers = [
    '文档名称',
    '政策文号',
    '业务分类',
    '发文机关',
    '发文日期',
    '实施日期',
    '失效日期',
    '状态',
    '创建时间'
  ];
  const rows = tableData.value.map((r: any) => [
    r.name,
    formatPolicyNo(r),
    BUSINESS_TYPE_MAP[r.businessType] || r.businessType || '',
    r.issuingAuthority || '',
    r.issuingDate || '',
    r.effectiveDate || '',
    r.expiryDate || '',
    r.status === 1 ? '启用' : '停用',
    r.createTime || ''
  ]);
  const csv =
    '\uFEFF' +
    [headers, ...rows]
      .map((line) =>
        line
          .map((cell) => {
            const s = String(cell ?? '');
            return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s;
          })
          .join(',')
      )
      .join('\n');
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `文档列表_${Date.now()}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  ElMessage.success('已导出当前页数据');
};



// ============ 运行Dify流水线解析 ============
const handleRunPipeline = async (row: any) => {
  await ElMessageBox.confirm(
    `确定对文档"${row.name}"运行Dify知识流水线解析吗？此操作将使用Dify服务处理文档内容。`,
    '运行解析确认',
    { type: 'info', confirmButtonText: '确认处理', cancelButtonText: '取消' }
  );
  try {
    await documentApi.runPipeline(row.id);
    ElMessage.success('Dify知识流水线解析已启动');
    // 刷新数据以更新状态
    loadData();
  } catch (e: any) {
    ElMessage.error(e?.message || '运行解析失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.document-list {
  height: 100%;
  min-width: 1200px;
  padding: 16px;
  box-sizing: border-box;
}

.doc-card {
  height: 100%;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  row-gap: 8px;
  margin-bottom: 8px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 8px;
}

.toolbar {
  display: flex;
  gap: 8px;
  margin: 8px 0 16px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
