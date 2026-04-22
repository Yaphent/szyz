/**
 * 法规文档管理 相关字典常量
 */

/** 业务分类 */
export const BUSINESS_TYPE_OPTIONS = [
  { label: '国家发文', value: 'NATIONAL' },
  { label: '部委发文', value: 'MINISTRY' },
  { label: '地方发文', value: 'LOCAL' },
  { label: '行业规范', value: 'INDUSTRY' },
  { label: '内部制度', value: 'INTERNAL' }
];

/** 业务分类 code => label 映射 */
export const BUSINESS_TYPE_MAP: Record<string, string> = BUSINESS_TYPE_OPTIONS
  .reduce((acc, cur) => ({ ...acc, [cur.value]: cur.label }), {} as Record<string, string>);

/** 时效性 */
export const TIMELINESS_OPTIONS = [
  { label: '有效', value: 'valid' },
  { label: '失效', value: 'invalid' },
  { label: '待生效', value: 'pending' }
];

export const TIMELINESS_MAP: Record<string, string> = TIMELINESS_OPTIONS
  .reduce((acc, cur) => ({ ...acc, [cur.value]: cur.label }), {} as Record<string, string>);

/** 管控模式 */
export const CONTROL_MODE_OPTIONS = [
  { label: '统建',           value: 'UNIFIED' },
  { label: '自建',           value: 'SELF' },
  { label: '自定义范围统建', value: 'CUSTOM' }
];

/** 状态 */
export const STATUS_OPTIONS = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 }
];

/** 支持的上传文件扩展名 */
export const ALLOWED_FILE_EXT = ['html', 'htm', 'doc', 'docx', 'pdf', 'txt', 'xls', 'xlsx', 'md'];

/** 文件类型接受 accept（用于 <input type=file> 或 el-upload） */
export const FILE_ACCEPT = ALLOWED_FILE_EXT.map((e) => '.' + e).join(',');

/** 单文件最大 50MB */
export const MAX_FILE_SIZE = 50 * 1024 * 1024;

/** 法规层次可选项 */
export const LAW_LEVEL_OPTIONS = [
  { label: '宪法',       value: '宪法' },
  { label: '法律',       value: '法律' },
  { label: '行政法规',   value: '行政法规' },
  { label: '部门规章',   value: '部门规章' },
  { label: '地方性法规', value: '地方性法规' },
  { label: '地方政府规章', value: '地方政府规章' },
  { label: '规范性文件', value: '规范性文件' }
];

/** 政策文号前缀下拉预置 */
export const POLICY_PREFIX_OPTIONS = [
  '中发', '国发', '国办发', '财发', '财政', '发改', '国税', '工信', '环发', '银发'
];

/**
 * 预置标签（与后端 t_dict_tag 初始化保持一致）
 */
export const PRESET_TAGS = [
  '重点支出',
  '脱贫攻坚',
  '生态环保',
  '基础建设',
  '重大项目投资',
  '国有土地使用权出让',
  '一带一路',
  '民生工程',
  '三公经费',
  '政府采购'
];

/**
 * 根据文件名/扩展名获取文件图标
 */
export function getFileIcon(fileName: string): string {
  const ext = (fileName || '').split('.').pop()?.toLowerCase() || '';
  const iconMap: Record<string, string> = {
    pdf:   'Document',
    doc:   'Document',
    docx:  'Document',
    xls:   'Tickets',
    xlsx:  'Tickets',
    txt:   'Memo',
    md:    'Memo',
    html:  'Link',
    htm:   'Link'
  };
  return iconMap[ext] || 'Document';
}

/**
 * 格式化文件大小
 */
export function formatFileSize(size: number): string {
  if (!size) return '0 B';
  if (size < 1024) return size + ' B';
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB';
  if (size < 1024 * 1024 * 1024) return (size / 1024 / 1024).toFixed(1) + ' MB';
  return (size / 1024 / 1024 / 1024).toFixed(2) + ' GB';
}
