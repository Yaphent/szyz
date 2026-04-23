<template>
  <div class="file-upload-card">
    <!-- 顶部操作按钮 -->
    <div v-if="!hideActions" class="actions">
      <el-upload
        ref="uploadRef"
        :show-file-list="false"
        :auto-upload="true"
        :accept="accept"
        :multiple="multiple"
        :http-request="customUpload"
        :before-upload="beforeUpload"
      >
        <el-button :disabled="disabled">
          <el-icon><UploadFilled /></el-icon>
          <span>上传文件</span>
        </el-button>
      </el-upload>

      <el-button
        v-if="showParse"
        :disabled="!canParse || parsing"
        :loading="parsing"
        @click="handleParse"
      >
        解析
      </el-button>
      <el-button
        v-if="showSmartParse"
        :disabled="!canParse || smartParsing"
        :loading="smartParsing"
        @click="handleSmartParse"
      >
        智能解析
      </el-button>
    </div>

    <!-- 文件卡片列表 -->
    <div v-if="fileList.length" class="file-list">
      <div v-for="(file, idx) in fileList" :key="file.fileId || idx" class="file-item">
        <!-- 文件图标 -->
        <div class="file-icon" :class="extClass(file.fileName)">
          <span class="ext-text">{{ extLabel(file.fileName) }}</span>
        </div>

        <!-- 文件信息 -->
        <div class="file-info">
          <div class="file-name" :title="file.fileName">
            <span class="idx">{{ idx + 1 }}.</span>
            <span class="name-text">{{ file.fileName }}</span>
          </div>
          <div class="file-meta">
            <span v-if="file.uploadTime">{{ file.uploadTime }}</span>
            <span v-if="file.fileSize">{{ formatFileSize(file.fileSize) }}</span>
          </div>
        </div>

        <!-- 操作图标 -->
        <div class="file-actions">
          <el-tooltip content="下载" placement="top">
            <el-icon class="action-icon" @click="handleDownload(file)">
              <Download />
            </el-icon>
          </el-tooltip>
          <el-tooltip v-if="!disabled" content="删除" placement="top">
            <el-icon class="action-icon danger" @click="handleRemove(idx, file)">
              <Delete />
            </el-icon>
          </el-tooltip>
        </div>
      </div>
    </div>

    <!-- 提示文案 -->
    <div v-if="tip" class="tip-text">{{ tip }}</div>
  </div>
</template>

<script setup lang="ts">
/**
 * 通用文件上传 / 展示卡片
 *
 * - v-model 绑定文件列表（每项结构参考后端 FileUploadVO / DocumentFile）
 * - 支持单文件 / 多文件模式
 * - 支持「解析」按钮（仅在 showParse=true 时显示，通常用于主文件卡片）
 * - 文件卡片样式参考设计图：图标 + 文件名 + 上传时间 + 下载/删除按钮
 */
import { computed, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { UploadFilled, Download, Delete } from '@element-plus/icons-vue';
import { documentApi } from '../../../api/document';
import {
  ALLOWED_FILE_EXT,
  FILE_ACCEPT,
  MAX_FILE_SIZE,
  formatFileSize
} from '../../../constants/document';

interface FileItem {
  fileId?: number;
  fileName: string;
  fileUrl?: string;
  fileSize?: number;
  fileType?: string;
  uploadTime?: string;
}

const props = defineProps<{
  /** 文件列表(v-model) */
  modelValue?: FileItem[];
  /** 文件类别：1主文件（当前系统只使用主文件类型） */
  category?: 1 | 2;
  /** 是否多文件 */
  multiple?: boolean;
  /** 是否只读（隐藏上传/删除按钮） */
  disabled?: boolean;
  /** 是否隐藏整个操作按钮区 */
  hideActions?: boolean;
  /** 是否显示"解析"按钮 */
  showParse?: boolean;
  /** 是否显示"智能解析"按钮 */
  showSmartParse?: boolean;
  /** 底部提示 */
  tip?: string;
  /** 允许的扩展名 accept 字符串 */
  accept?: string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', val: FileItem[]): void;
  (e: 'upload-success', file: FileItem): void;
  (e: 'remove', file: FileItem, index: number): void;
  (e: 'parse', content: string): void;
  (e: 'smart-parse', content: string): void;
}>();

const uploadRef = ref<any>(null);
const parsing = ref(false);
const smartParsing = ref(false);
/** 缓存最近一次上传的原始 File，供解析按钮使用 */
const lastRawFile = ref<File | null>(null);

const fileList = computed<FileItem[]>(() => props.modelValue || []);
const accept = computed(() => props.accept || FILE_ACCEPT);
const canParse = computed(() => fileList.value.length > 0);

// ============= 上传前校验 =============
const beforeUpload = (file: File): boolean => {
  const ext = (file.name.split('.').pop() || '').toLowerCase();
  if (!ALLOWED_FILE_EXT.includes(ext)) {
    ElMessage.error(`不支持的文件格式: .${ext}，仅支持 ${ALLOWED_FILE_EXT.join('/')}`);
    return false;
  }
  if (file.size > MAX_FILE_SIZE) {
    ElMessage.error('文件大小不能超过 50MB');
    return false;
  }
  return true;
};

// ============= 自定义上传逻辑 =============
const customUpload = async (option: any) => {
  const rawFile: File = option.file;
  lastRawFile.value = rawFile;
  try {
    const res: any = await documentApi.uploadFile(rawFile, props.category || 1);
    const data = res.data || {};
    const item: FileItem = {
      fileId: data.fileId,
      fileName: data.fileName || rawFile.name,
      fileUrl: data.fileUrl,
      fileSize: data.fileSize || rawFile.size,
      fileType: data.fileType,
      uploadTime: formatNow()
    };
    // 单文件替换，多文件追加
    const next = props.multiple ? [...fileList.value, item] : [item];
    emit('update:modelValue', next);
    emit('upload-success', item);
    ElMessage.success('上传成功');
  } catch (e: any) {
    ElMessage.error(e?.message || '上传失败');
  }
};

// ============= 删除 =============
const handleRemove = async (index: number, file: FileItem) => {
  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    // 如果有 fileId，调用后端删除接口
    if (file.fileId) {
      await documentApi.removeFile(file.fileId);
    }
    
    const next = [...fileList.value];
    next.splice(index, 1);
    emit('update:modelValue', next);
    emit('remove', file, index);
    // 清理缓存
    if (!next.length) {
      lastRawFile.value = null;
    }
    
    ElMessage.success('删除成功');
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败');
    }
  }
};

// ============= 下载 =============
const handleDownload = (file: FileItem) => {
  if (!file.fileId) {
    // 若无 fileId 直接使用 URL
    if (file.fileUrl) {
      window.open(file.fileUrl, '_blank');
    } else {
      ElMessage.warning('文件信息不完整，无法下载');
    }
    return;
  }
  const url = documentApi.getDownloadUrl(file.fileId);
  // 使用 a 标签触发浏览器原生下载，携带 Authorization 需要额外处理，
  // 此处若后端 /download 路径已在 WebMvcConfig 排除鉴权，可直接下载
  window.open(url, '_blank');
};

// ============= 解析 =============
const handleParse = async () => {
  if (!lastRawFile.value) {
    ElMessage.warning('请先重新上传文件以进行解析');
    return;
  }
  parsing.value = true;
  try {
    const res: any = await documentApi.parseFile(lastRawFile.value);
    const content = res.data?.content || '';
    emit('parse', content);
    ElMessage.success('解析完成');
  } catch (e: any) {
    ElMessage.error(e?.message || '解析失败');
  } finally {
    parsing.value = false;
  }
};

// ============= 智能解析 =============
const handleSmartParse = async () => {
  if (!lastRawFile.value) {
    ElMessage.warning('请先重新上传文件以进行智能解析');
    return;
  }
  smartParsing.value = true;
  try {
    const res: any = await documentApi.smartParseFile(lastRawFile.value);
    const summary = res.data?.summary || '';
    emit('smart-parse', summary);
    ElMessage.success('智能解析完成');
  } catch (e: any) {
    ElMessage.error(e?.message || '智能解析失败');
  } finally {
    smartParsing.value = false;
  }
};

// ============= 工具方法 =============
const extLabel = (fileName: string): string => {
  const ext = (fileName || '').split('.').pop()?.toUpperCase() || '';
  return ext.length > 4 ? ext.slice(0, 4) : ext;
};

const extClass = (fileName: string): string => {
  const ext = (fileName || '').split('.').pop()?.toLowerCase() || '';
  if (['pdf'].includes(ext)) return 'type-pdf';
  if (['doc', 'docx'].includes(ext)) return 'type-word';
  if (['xls', 'xlsx'].includes(ext)) return 'type-excel';
  if (['txt', 'md'].includes(ext)) return 'type-txt';
  if (['html', 'htm'].includes(ext)) return 'type-html';
  return 'type-default';
};

const formatNow = (): string => {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(
    d.getHours()
  )}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
};
</script>

<style scoped>
.file-upload-card {
  width: 100%;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 4px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: #f5f7fa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.file-item:hover {
  background: #ecf5ff;
}

.file-icon {
  width: 40px;
  height: 48px;
  border-radius: 3px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 11px;
  flex-shrink: 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.file-icon.type-pdf    { background: #e74c3c; }
.file-icon.type-word   { background: #2b7cd3; }
.file-icon.type-excel  { background: #1d8348; }
.file-icon.type-txt    { background: #7f8c8d; }
.file-icon.type-html   { background: #e67e22; }
.file-icon.type-default{ background: #909399; }

.ext-text {
  letter-spacing: 1px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.name-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.idx {
  color: #909399;
  flex-shrink: 0;
}

.file-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 12px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.action-icon {
  font-size: 16px;
  color: #606266;
  cursor: pointer;
  transition: color 0.2s;
}

.action-icon:hover {
  color: #409eff;
}

.action-icon.danger:hover {
  color: #f56c6c;
}

.tip-text {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
</style>
