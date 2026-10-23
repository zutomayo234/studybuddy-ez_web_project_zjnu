<template>
  <div>
    <el-row :gutter="16" class="mb-16">
      <el-col :span="6">
        <el-card class="mini"><div class="num">{{ stats.chunkCount }}</div><div class="label">片段总数</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini"><div class="num">{{ stats.runtimeChunkCount }}</div><div class="label">运行时追加</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini"><div class="num">{{ stats.topK }}</div><div class="label">检索 Top-K</div></el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="mini">
          <div class="num small">classpath</div>
          <div class="label">知识库来源</div>
          <div class="file">{{ shortFile }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mb-16">
      <div class="toolbar">
        <el-button @click="load" :loading="loading">刷新</el-button>
        <el-button type="warning" :loading="reloading" @click="onReload">重新加载知识库文件</el-button>
        <el-button type="primary" @click="dialogVisible = true">追加片段</el-button>
        <span class="tip">重新加载会保留运行时追加的片段；追加的片段在进程重启后失效</span>
      </div>

      <el-input
        v-model="keyword"
        placeholder="按内容过滤片段"
        clearable
        style="width: 320px; margin-bottom: 12px"
      />

      <el-table v-loading="loading" :data="filteredChunks" border stripe max-height="520">
        <el-table-column prop="index" label="序号" width="80" />
        <el-table-column prop="text" label="片段内容" min-width="420" show-overflow-tooltip />
        <el-table-column prop="tokenCount" label="词元数" width="90" />
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.runtimeAdded" size="small" type="warning">运行时追加</el-tag>
            <el-tag v-else size="small" type="info">文件</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="追加知识库片段" width="620px">
      <el-alert
        class="mb-16"
        type="info"
        :closable="false"
        title="片段会被独立切块并立即参与检索"
        description="建议一个片段只讲清一个知识点，便于检索时精确命中；与原文件保持「空行分隔」的段落风格一致。"
      />
      <el-input
        v-model="newContent"
        type="textarea"
        :rows="7"
        maxlength="1000"
        show-word-limit
        placeholder="例如：&#10;接口幂等性：同一请求重复执行多次，对系统状态的影响与执行一次相同。常见实现有唯一业务号去重、数据库唯一索引、乐观锁版本号。"
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="onAdd">追加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { kbApi } from '../api/kb'

const loading = ref(false)
const reloading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const chunks = ref([])
const keyword = ref('')
const newContent = ref('')
const stats = ref({ chunkCount: 0, runtimeChunkCount: 0, topK: 3, sourceFile: '' })

const shortFile = computed(() => {
  const f = stats.value.sourceFile || ''
  const i = f.lastIndexOf('/')
  return i >= 0 ? f.substring(i + 1) : f
})

const filteredChunks = computed(() => {
  const k = keyword.value.trim()
  return k ? chunks.value.filter((c) => (c.text || '').includes(k)) : chunks.value
})

const load = async () => {
  loading.value = true
  try {
    const [chunkRes, statsRes] = await Promise.all([kbApi.chunks(), kbApi.stats()])
    chunks.value = chunkRes.data
    stats.value = statsRes.data
  } finally {
    loading.value = false
  }
}

const onReload = async () => {
  reloading.value = true
  try {
    const res = await kbApi.reload()
    ElMessage.success(`重新加载完成，当前共 ${res.data.chunkCount} 个片段`)
    load()
  } finally {
    reloading.value = false
  }
}

const onAdd = async () => {
  if (!newContent.value.trim()) {
    ElMessage.warning('请输入片段内容')
    return
  }
  submitting.value = true
  try {
    const res = await kbApi.addChunk(newContent.value)
    ElMessage.success(`已追加，当前共 ${res.data.chunkCount} 个片段`)
    dialogVisible.value = false
    newContent.value = ''
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.mini {
  text-align: center;
}

.num {
  font-size: 26px;
  font-weight: 700;
  color: #409eff;
}

.num.small {
  font-size: 18px;
  padding: 6px 0;
}

.label {
  color: #909399;
  font-size: 13px;
  margin-top: 4px;
}

.file {
  color: #c0c4cc;
  font-size: 11px;
  margin-top: 2px;
}

.tip {
  color: #909399;
  font-size: 13px;
}
</style>
