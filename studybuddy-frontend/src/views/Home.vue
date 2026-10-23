<template>
  <div>

    <el-row :gutter="16">
      <el-col v-for="c in cards" :key="c.label" :span="6">
        <el-card class="stat-card">
          <div class="num">{{ c.value }}</div>
          <div class="label">{{ c.label }}</div>
          <div class="hint">{{ c.hint }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">

      <el-col :span="12">
        <el-card>
          <h3 class="page-title">用户角色分布</h3>
          <div v-if="stats.roleDistribution && stats.roleDistribution.length" class="dist">
            <div v-for="d in stats.roleDistribution" :key="d.name" class="dist-row">
              <div class="dist-name">{{ d.name }}</div>
              <el-progress
                :percentage="percent(d.value)"
                :stroke-width="16"
                :show-text="false"
                class="dist-bar"
              />
              <div class="dist-val">{{ d.value }} 人</div>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <h3 class="page-title">系统能力状态</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="角色 / 权限点">
              {{ stats.roleCount }} 个角色 · {{ stats.permCount }} 个权限点
            </el-descriptions-item>
            <el-descriptions-item label="智能问答">
              {{ stats.sessionTotal }} 个会话 · {{ stats.messageTotal }} 条消息
            </el-descriptions-item>
            <el-descriptions-item label="RAG 知识库">
              {{ stats.kbChunkCount }} 个片段（支持运行时热更新）
            </el-descriptions-item>
            <el-descriptions-item label="操作审计">
              {{ stats.logTotal }} 条记录 · 其中失败 {{ stats.logFailed }} 条
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt-16">
      <h3 class="page-title">实验一到五 · 技术栈对照</h3>
      <el-table :data="expList" border stripe>
        <el-table-column prop="exp" label="实验" width="90" />
        <el-table-column prop="topic" label="主题" width="220" />
        <el-table-column prop="stack" label="关键技术" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { statsApi } from '../api/stats'

const stats = ref({
  userTotal: 0,
  userActive: 0,
  roleCount: 0,
  permCount: 0,
  sessionTotal: 0,
  messageTotal: 0,
  kbChunkCount: 0,
  logTotal: 0,
  logFailed: 0,
  roleDistribution: []
})

const cards = computed(() => [
  { label: '系统用户总数', value: stats.value.userTotal, hint: `其中正常状态 ${stats.value.userActive}` },
  { label: '智能问答会话', value: stats.value.sessionTotal, hint: `累计 ${stats.value.messageTotal} 条消息` },
  { label: '知识库片段', value: stats.value.kbChunkCount, hint: 'RAG 检索语料' },
  { label: '审计日志', value: stats.value.logTotal, hint: `失败操作 ${stats.value.logFailed} 条` }
])

const percent = (v) => {
  const total = stats.value.userTotal || 0
  if (!total) {
    return 0
  }
  return Math.round((v / total) * 100)
}

const expList = [
  { exp: '实验一', topic: '搭建 Web 基础项目', stack: 'Maven · Spring Boot 3 · MyBatis · MySQL · 内嵌 Tomcat' },
  { exp: '实验二', topic: '前后端分离与 RESTful API', stack: 'RESTful · JSON · Vue3 · Axios · CORS · OpenAPI 文档' },
  { exp: '实验三', topic: 'Git 协作与持续集成', stack: 'Git 分支模型 · Pull Request · GitHub Actions CI' },
  { exp: '实验四', topic: '登录、权限与 AI 安全', stack: 'Spring Security · JWT · BCrypt · RBAC 管理端 · 操作审计' },
  { exp: '实验五', topic: 'LLM 应用与智能原型', stack: '大模型 API · 提示工程 · RAG · 多轮对话 · 降级兜底' }
]

onMounted(async () => {
  try {
    const res = await statsApi.overview()
    stats.value = res.data
  } catch (e) {

  }
})
</script>

<style scoped>
.stat-card {
  text-align: center;
}

.num {
  font-size: 30px;
  font-weight: 700;
  color: #409eff;
}

.label {
  color: #303133;
  font-size: 14px;
  margin-top: 6px;
}

.hint {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.dist-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.dist-name {
  width: 90px;
  font-size: 13px;
  color: #606266;
  flex-shrink: 0;
}

.dist-bar {
  flex: 1;
}

.dist-val {
  width: 70px;
  text-align: right;
  font-size: 13px;
  color: #909399;
  flex-shrink: 0;
}
</style>
