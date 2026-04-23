import request from './request';

/**
 * 文档查询参数
 */
export interface DocumentQuery {
  name?: string;
  policyNo?: string;
  businessType?: string;
  status?: number;
  startDate?: string;
  endDate?: string;
  pageNum?: number;
  pageSize?: number;
}

/**
 * 文档表单对象
 */
export interface DocumentForm {
  id?: number;
  name: string;
  policyPrefix?: string;
  policyYear?: string;
  policyNo?: string;
  businessType: string;
  keywords?: string;
  issuingAuthority: string;
  issuingDate: string;
  effectiveDate: string;
  expiryDate?: string;
  lawLevel?: string;
  timeliness?: string;
  summary?: string;
  controlMode: string;
  mainFileId?: number;

  tags?: string[];
}

/**
 * 法规文档管理 API
 */
export const documentApi = {
  /** 分页查询 */
  getPage: (params: DocumentQuery) =>
    request.get('/document/page', { params }),

  /** 详情 */
  getById: (id: number) =>
    request.get(`/document/${id}`),

  /** 新增 */
  create: (data: DocumentForm) =>
    request.post('/document', data),

  /** 更新 */
  update: (id: number, data: DocumentForm) =>
    request.put(`/document/${id}`, data),

  /** 单条删除 */
  remove: (id: number) =>
    request.delete(`/document/${id}`),

  /** 批量删除 */
  removeBatch: (ids: number[]) =>
    request.delete('/document/batch', { data: ids }),

  /** 批量启用 / 停用 */
  updateStatus: (ids: number[], status: number) =>
    request.put('/document/status', { ids, status }),

  /**
   * 上传文件
   * @param file     文件
   * @param category 1=主文件（当前系统只使用主文件类型）
   */
  uploadFile: (file: File, category: 1 | 2 = 1) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', String(category));
    return request.post('/document/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    });
  },

  /**
   * 上传文件到 Dify 知识流水线
   * @param file     文件
   * @param category 1=主文件（当前系统只使用主文件类型）
   */
  uploadFileToDifyPipeline: (file: File, category: 1 | 2 = 1) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('category', String(category));
    return request.post('/document/upload-pipeline', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    });
  },

  /**
   * 解析文件内容（不入库，仅返回文本）
   */
  parseFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/document/parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    });
  },

  /**
   * 智能解析文件内容（使用 Dify 服务进行高级解析）
   */
  smartParseFile: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/document/smart-parse', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 120000
    });
  },

  /** 下载文件的直链（交由浏览器处理） */
  getDownloadUrl: (fileId: number) => {
    const base = (import.meta.env.VITE_API_URL as string) || 'http://localhost:8080/api';
    return `${base}/document/download/${fileId}`;
  },

  /** 获取标签字典（系统预置 + 自定义） */
  getTags: () => request.get('/document/dict/tags'),

  /** 新增自定义标签 */
  addTag: (tagName: string) =>
    request.post('/document/dict/tags', { tagName }),

  /** 删除文件 */
  removeFile: (fileId: number) =>
    request.delete(`/document/file/${fileId}`),

  /** 获取文档的 Dify 处理状态 */
  getDifyStatus: (id: number) =>
    request.get(`/document/dify-status/${id}`),

  /** 运行文档的 Dify 知识流水线解析 */
  runPipeline: (id: number) =>
    request.post(`/document/run-pipeline/${id}`),


};

export default documentApi;
