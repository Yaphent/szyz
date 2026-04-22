<template>
  <div class="tag-selector">
    <!-- 已选标签 -->
    <div class="selected-tags">
      <el-tag
        v-for="tag in selected"
        :key="tag"
        :closable="!disabled"
        type="primary"
        effect="light"
        class="tag-item"
        @close="removeTag(tag)"
      >
        {{ tag }}
      </el-tag>

      <!-- "+" 打开选择面板的按钮 -->
      <el-popover
        v-if="!disabled"
        v-model:visible="popoverVisible"
        placement="bottom-start"
        :width="380"
        trigger="click"
        popper-class="tag-selector-popper"
      >
        <template #reference>
          <el-button class="add-btn" size="small" circle>
            <el-icon><Plus /></el-icon>
          </el-button>
        </template>

        <!-- 面板内容：搜索 + 候选标签 + 新建 -->
        <div class="panel">
          <div class="panel-header">
            <el-input
              v-model="keyword"
              size="small"
              placeholder="搜索标签"
              clearable
              :prefix-icon="Search"
            />
          </div>

          <el-scrollbar max-height="260px">
            <div class="panel-body">
              <div v-if="!filteredTags.length" class="empty-tip">
                无匹配标签，可点击下方"新建"创建
              </div>
              <el-check-tag
                v-for="tag in filteredTags"
                :key="tag"
                :checked="selected.includes(tag)"
                class="check-tag"
                @change="toggleTag(tag)"
              >
                {{ tag }}
              </el-check-tag>
            </div>
          </el-scrollbar>

          <div class="panel-footer">
            <el-input
              v-model="newTag"
              size="small"
              placeholder="输入新标签名"
              maxlength="20"
              show-word-limit
              @keyup.enter="handleCreate"
            />
            <el-button
              type="primary"
              size="small"
              :loading="creating"
              :disabled="!newTag.trim()"
              @click="handleCreate"
            >
              新建
            </el-button>
          </div>
        </div>
      </el-popover>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * 标签选择器
 *
 * - v-model 绑定已选标签名数组 string[]
 * - 点击 "+" 打开面板，面板内展示从后端拉取的标签字典
 * - 支持搜索 / 勾选 / 取消勾选
 * - 支持底部输入框新建自定义标签，成功后自动勾选
 */
import { computed, onMounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Search } from '@element-plus/icons-vue';
import { documentApi } from '../../../api/document';
import { PRESET_TAGS } from '../../../constants/document';

const props = defineProps<{
  modelValue?: string[];
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', val: string[]): void;
}>();

const popoverVisible = ref(false);
const keyword = ref('');
const newTag = ref('');
const creating = ref(false);

/** 所有候选标签（来自后端字典 + 预置兜底） */
const allTags = ref<string[]>([]);

const selected = computed<string[]>({
  get: () => props.modelValue || [],
  set: (val) => emit('update:modelValue', val)
});

const filteredTags = computed(() => {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) return allTags.value;
  return allTags.value.filter((t) => t.toLowerCase().includes(kw));
});

/**
 * 拉取标签字典
 */
const loadTags = async () => {
  try {
    const res: any = await documentApi.getTags();
    const list: any[] = res.data || [];
    // 兼容后端返回的两种结构：字符串数组 或 对象数组(tagName)
    const names = list
      .map((item) => (typeof item === 'string' ? item : item.tagName))
      .filter(Boolean);
    allTags.value = Array.from(new Set([...names, ...PRESET_TAGS]));
  } catch (e) {
    // 接口失败时使用预置标签兜底
    allTags.value = [...PRESET_TAGS];
  }
};

const toggleTag = (tag: string) => {
  const arr = [...selected.value];
  const idx = arr.indexOf(tag);
  if (idx >= 0) {
    arr.splice(idx, 1);
  } else {
    arr.push(tag);
  }
  selected.value = arr;
};

const removeTag = (tag: string) => {
  selected.value = selected.value.filter((t) => t !== tag);
};

const handleCreate = async () => {
  const name = newTag.value.trim();
  if (!name) return;
  if (allTags.value.includes(name)) {
    ElMessage.warning('标签已存在');
    if (!selected.value.includes(name)) {
      selected.value = [...selected.value, name];
    }
    newTag.value = '';
    return;
  }
  creating.value = true;
  try {
    await documentApi.addTag(name);
    allTags.value = [name, ...allTags.value];
    selected.value = [...selected.value, name];
    newTag.value = '';
    ElMessage.success('标签已创建');
  } catch (e: any) {
    ElMessage.error(e?.message || '创建失败');
  } finally {
    creating.value = false;
  }
};

// 打开面板时清空搜索
watch(popoverVisible, (v) => {
  if (v) {
    keyword.value = '';
    newTag.value = '';
  }
});

onMounted(() => {
  loadTags();
});
</script>

<style scoped>
.tag-selector {
  width: 100%;
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  min-height: 32px;
  padding: 4px 0;
}

.tag-item {
  margin: 0;
}

.add-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.panel-header {
  padding-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-body {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 4px 2px;
}

.check-tag {
  cursor: pointer;
  user-select: none;
}

.empty-tip {
  width: 100%;
  padding: 20px 0;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.panel-footer {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
