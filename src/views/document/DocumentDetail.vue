<template>
  <div class="document-detail">
    <el-card class="detail-card">
      <!-- 顶部标题栏 -->
      <template #header>
        <div class="card-header">
          <div class="title-bar">
            <el-button :icon="ArrowLeft" link @click="handleBack">返回</el-button>
            <span class="title">{{ pageTitle }}</span>
          </div>
          <div class="header-actions">
            <el-button v-if="mode === 'view' && !isNew" type="primary" @click="switchToEdit">
              编辑
            </el-button>
            <template v-else>
              <el-button @click="handleBack">取消</el-button>
              <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
            </template>
          </div>
        </div>
      </template>

      <!-- 表单主体：4 列栅格 -->
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        :disabled="readOnly"
        label-width="110px"
        label-position="right"
        class="detail-form"
      >
        <el-row :gutter="20">
          <!-- 文档名称 占 2 列 -->
          <el-col :span="12">
            <el-form-item label="文档名称" prop="name" required>
              <el-input v-model="form.name" placeholder="请输入文档名称" maxlength="200" />
            </el-form-item>
          </el-col>

          <!-- 业务分类 -->
          <el-col :span="6">
            <el-form-item label="业务分类" prop="businessType" required>
              <el-select v-model="form.businessType" placeholder="请选择" clearable style="width: 100%">
                <el-option
                  v-for="item in BUSINESS_TYPE_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 时效性 -->
          <el-col :span="6">
            <el-form-item label="时效性" prop="timeliness">
              <el-select v-model="form.timeliness" placeholder="请选择" clearable style="width: 100%">
                <el-option
                  v-for="item in TIMELINESS_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 政策文号（三联输入） -->
          <el-col :span="12">
            <el-form-item label="政策文号">
              <div class="policy-no-group">
                <el-select
                  v-model="form.policyPrefix"
                  placeholder="前缀"
                  filterable
                  allow-create
                  default-first-option
                  style="width: 30%"
                >
                  <el-option
                    v-for="p in POLICY_PREFIX_OPTIONS"
                    :key="p"
                    :label="p"
                    :value="p"
                  />
                </el-select>
                <span class="bracket">〔</span>
                <el-input
                  v-model="form.policyYear"
                  placeholder="年份"
                  maxlength="4"
                  style="width: 20%"
                />
                <span class="bracket">〕</span>
                <el-input
                  v-model="form.policyNo"
                  placeholder="编号"
                  maxlength="20"
                  style="flex: 1"
                />
              </div>
            </el-form-item>
          </el-col>

          <!-- 发文机关 -->
          <el-col :span="12">
            <el-form-item label="发文机关" prop="issuingAuthority" required>
              <el-input v-model="form.issuingAuthority" placeholder="请输入发文机关" maxlength="100" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 发文日期 -->
          <el-col :span="8">
            <el-form-item label="发文日期" prop="issuingDate" required>
              <el-date-picker
                v-model="form.issuingDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <!-- 实施日期 -->
          <el-col :span="8">
            <el-form-item label="实施日期" prop="effectiveDate" required>
              <el-date-picker
                v-model="form.effectiveDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>

          <!-- 失效日期 -->
          <el-col :span="8">
            <el-form-item label="失效日期" prop="expiryDate">
              <el-date-picker
                v-model="form.expiryDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 法规层次 -->
          <el-col :span="8">
            <el-form-item label="法规层次" prop="lawLevel">
              <el-select v-model="form.lawLevel" placeholder="请选择" clearable style="width: 100%">
                <el-option
                  v-for="item in LAW_LEVEL_OPTIONS"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- 管控模式 -->
          <el-col :span="8">
            <el-form-item label="管控模式" prop="controlMode" required>
              <el-radio-group v-model="form.controlMode">
                <el-radio
                  v-for="item in CONTROL_MODE_OPTIONS"
                  :key="item.value"
                  :value="item.value"
                >
                  {{ item.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>

          <!-- 关键词 -->
          <el-col :span="8">
            <el-form-item label="关键词" prop="keywords">
              <el-input
                v-model="form.keywords"
                placeholder="多关键词用逗号分隔"
                maxlength="200"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 标签 -->
          <el-col :span="24">
            <el-form-item label="标签">
              <TagSelector v-model="form.tags" :disabled="readOnly" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 主文件 -->
          <el-col :span="24">
            <el-form-item label="主文件">
              <FileUploadCard
                v-model="mainFileList"
                :category="1"
                :multiple="false"
                :disabled="readOnly"
                :show-parse="!readOnly"
                :show-smart-parse="!readOnly"
                @parse="handleParseContent"
                @smart-parse="handleSmartParseContent"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 附件 -->
          <el-col :span="24">
            <el-form-item label="附件">
              <FileUploadCard
                v-model="attachmentList"
                :category="2"
                :multiple="true"
                :disabled="readOnly"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <!-- 摘要 占满整行 -->
          <el-col :span="24">
            <el-form-item label="摘要" prop="summary">
              <div class="dify-summary-section">
                <el-input
                  v-model="form.summary"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入摘要，或点击主文件旁的'解析'按钮自动提取"
                  maxlength="2000"
                  show-word-limit
                />
                <!-- Dify 处理状态 -->
                <div v-if="difyStatus" class="dify-status-info">
                  <el-tag 
                    :type="getSummaryStatusType(difyStatus.summaryStatus)" 
                    size="small"
                    effect="plain"
                  >
                    摘要: {{ getSummaryStatusText(difyStatus.summaryStatus) }}
                  </el-tag>
                  <el-tag 
                    :type="getVectorizationStatusType(difyStatus.vectorizationStatus)" 
                    size="small"
                    effect="plain"
                  >
                    向量化: {{ getVectorizationStatusText(difyStatus.vectorizationStatus) }}
                  </el-tag>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 法规文档 —— 详情 / 新增 / 编辑页
 *
 * 路由约定：
 * - /document/detail/new          新增
 * - /document/detail/:id          默认查看（view）；可通过 query.mode=edit 切换到编辑
 */
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';
import FileUploadCard from './components/FileUploadCard.vue';
import TagSelector from './components/TagSelector.vue';
import { documentApi, type DocumentForm } from '../../api/document';
import {
  BUSINESS_TYPE_OPTIONS,
  TIMELINESS_OPTIONS,
  CONTROL_MODE_OPTIONS,
  LAW_LEVEL_OPTIONS,
  POLICY_PREFIX_OPTIONS
} from '../../constants/document';

const route = useRoute();
const router = useRouter();

// ============ 模式判定 ============
const rawId = String(route.params.id || '');
const isNew = computed(() => rawId === 'new' || rawId === '');
const currentId = computed(() => (isNew.value ? null : Number(rawId)));

/** view / edit / create */
const mode = ref<'view' | 'edit' | 'create'>(
  isNew.value ? 'create' : (route.query.mode as any) === 'edit' ? 'edit' : 'view'
);
const readOnly = computed(() => mode.value === 'view');

const pageTitle = computed(() => {
  if (mode.value === 'create') return '新增文档';
  if (mode.value === 'edit') return '编辑文档';
  return '文档详情';
});

// ============ 表单数据 ============
interface FileItem {
  fileId?: number;
  fileName: string;
  fileUrl?: string;
  fileSize?: number;
  fileType?: string;
  uploadTime?: string;
}

const formRef = ref<FormInstance | null>(null);
const saving = ref(false);

const defaultForm = (): DocumentForm => ({
  name: '',
  policyPrefix: '',
  policyYear: new Date().getFullYear().toString(),
  policyNo: '',
  businessType: '',
  keywords: '',
  issuingAuthority: '',
  issuingDate: '',
  effectiveDate: '',
  expiryDate: '',
  lawLevel: '',
  timeliness: 'valid',
  summary: '',
  controlMode: 'UNIFIED',
  mainFileId: undefined,
  attachmentIds: [],
  tags: []
});

const form = reactive<DocumentForm>(defaultForm());
const mainFileList = ref<FileItem[]>([]);
const attachmentList = ref<FileItem[]>([]);

// Dify 状态相关
const difyStatus = ref<any>(null);

// ============ 校验规则 ============
const rules: FormRules = {
  name: [
    { required: true, message: '请输入文档名称', trigger: 'blur' },
    { max: 200, message: '文档名称不能超过 200 个字符', trigger: 'blur' }
  ],
  businessType: [{ required: true, message: '请选择业务分类', trigger: 'change' }],
  issuingAuthority: [{ required: true, message: '请输入发文机关', trigger: 'blur' }],
  issuingDate: [{ required: true, message: '请选择发文日期', trigger: 'change' }],
  effectiveDate: [{ required: true, message: '请选择实施日期', trigger: 'change' }],
  controlMode: [{ required: true, message: '请选择管控模式', trigger: 'change' }]
};

// ============ 数据加载 ============
const loadDetail = async () => {
  if (isNew.value) return;
  try {
    const res: any = await documentApi.getById(currentId.value as number);
    const d = res.data || {};
    Object.assign(form, {
      id: d.id,
      name: d.name || '',
      policyPrefix: d.policyPrefix || '',
      policyYear: d.policyYear || '',
      policyNo: d.policyNo || '',
      businessType: d.businessType || '',
      keywords: d.keywords || '',
      issuingAuthority: d.issuingAuthority || '',
      issuingDate: d.issuingDate || '',
      effectiveDate: d.effectiveDate || '',
      expiryDate: d.expiryDate || '',
      lawLevel: d.lawLevel || '',
      timeliness: d.timeliness || 'valid',
      summary: d.summary || '',
      controlMode: d.controlMode || 'UNIFIED',
      tags: Array.isArray(d.tags) ? d.tags.map((t: any) => (typeof t === 'string' ? t : t.tagName)) : []
    });
    // 主文件
    if (d.mainFile) {
      mainFileList.value = [
        {
          fileId: d.mainFile.id || d.mainFile.fileId,
          fileName: d.mainFile.fileName,
          fileUrl: d.mainFile.fileUrl,
          fileSize: d.mainFile.fileSize,
          fileType: d.mainFile.fileType,
          uploadTime: d.mainFile.createTime || d.mainFile.uploadTime
        }
      ];
      form.mainFileId = d.mainFile.id || d.mainFile.fileId;
    }
    // 附件
    if (Array.isArray(d.attachments)) {
      attachmentList.value = d.attachments.map((f: any) => ({
        fileId: f.id || f.fileId,
        fileName: f.fileName,
        fileUrl: f.fileUrl,
        fileSize: f.fileSize,
        fileType: f.fileType,
        uploadTime: f.createTime || f.uploadTime
      }));
      form.attachmentIds = attachmentList.value.map((f) => f.fileId).filter(Boolean) as number[];
    }
    
    // 加载 Dify 状态
    await loadDifyStatus();
  } catch (e: any) {
    ElMessage.error(e?.message || '加载详情失败');
  }
};

// ============ Dify 状态加载 ============
const loadDifyStatus = async () => {
  if (isNew.value) return;
  try {
    const res: any = await documentApi.getDifyStatus(currentId.value as number);
    difyStatus.value = res.data || null;
  } catch (e: any) {
    console.error('加载 Dify 状态失败:', e);
    // 不显示错误消息，因为这不影响主要功能
  }
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

const getVectorizationStatusText = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待处理',
    1: '处理中',
    2: '已完成',
    3: '失败'
  };
  return statusMap[status] || '未知';
};

const getVectorizationStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  };
  return typeMap[status] || 'info';
};

// ============ 解析回填 ============
const handleParseContent = async (content: string) => {
  if (!content) return;
  // 若摘要已有内容，询问是否覆盖
  if (form.summary && form.summary.trim()) {
    try {
      await ElMessageBox.confirm('已有摘要内容，是否覆盖？', '提示', {
        type: 'warning',
        confirmButtonText: '覆盖',
        cancelButtonText: '取消'
      });
    } catch {
      return;
    }
  }
  form.summary = content.length > 2000 ? content.slice(0, 2000) : content;
  ElMessage.success('已回填摘要');
};

// ============ 智能解析回填 ============
const handleSmartParseContent = async (summary: string) => {
  if (!summary) return;
  // 若摘要已有内容，询问是否覆盖
  if (form.summary && form.summary.trim()) {
    try {
      await ElMessageBox.confirm('已有摘要内容，是否覆盖？', '提示', {
        type: 'warning',
        confirmButtonText: '覆盖',
        cancelButtonText: '取消'
      });
    } catch {
      return;
    }
  }
  form.summary = summary.length > 2000 ? summary.slice(0, 2000) : summary;
  ElMessage.success('已回填智能解析摘要');
};

// ============ 动作 ============
const switchToEdit = () => {
  mode.value = 'edit';
  router.replace({ query: { ...route.query, mode: 'edit' } });
};

const handleBack = () => {
  router.push({ name: 'DocumentList' });
};

const buildSubmitPayload = (): DocumentForm => {
  return {
    ...form,
    mainFileId: mainFileList.value[0]?.fileId,
    attachmentIds: attachmentList.value.map((f) => f.fileId).filter(Boolean) as number[],
    tags: form.tags || []
  };
};

const handleSave = async () => {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    ElMessage.warning('请检查表单必填项');
    return;
  }
  // 主文件必传
  if (!mainFileList.value.length || !mainFileList.value[0].fileId) {
    ElMessage.warning('请上传主文件');
    return;
  }
  // 失效日期需晚于实施日期
  if (form.expiryDate && form.effectiveDate && form.expiryDate < form.effectiveDate) {
    ElMessage.warning('失效日期不能早于实施日期');
    return;
  }

  saving.value = true;
  const payload = buildSubmitPayload();
  try {
    if (isNew.value) {
      await documentApi.create(payload);
      ElMessage.success('新增成功');
    } else {
      await documentApi.update(currentId.value as number, payload);
      ElMessage.success('修改成功');
    }
    router.push({ name: 'DocumentList' });
  } catch (e: any) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};

onMounted(() => {
  loadDetail();
});
</script>

<style scoped>
.document-detail {
  height: 100%;
  min-width: 1100px;
  padding: 16px;
  box-sizing: border-box;
}

.detail-card {
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.detail-form {
  padding: 8px 4px;
}

/* 政策文号三联输入 */
.policy-no-group {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
}

.policy-no-group .bracket {
  color: #606266;
  font-size: 14px;
  user-select: none;
  flex-shrink: 0;
}

/* Dify 摘要区域 */
.dify-summary-section {
  position: relative;
}

.dify-status-info {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
