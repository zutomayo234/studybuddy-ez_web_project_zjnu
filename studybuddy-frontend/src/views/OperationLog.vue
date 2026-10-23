<template>
  <div>
    <el-alert
      class="mb-16"
      type="info"
      :closable="false"
      title="操作审计日志（功能增强 #5）"
      description="由 AOP 切面在标注 @OpLog 的方法执行后自动写入。切面优先于事务执行，因此业务回滚时失败记录同样会被保留——这正是审计最需要的部分。"
    />

    <el-card>
      <div class="toolbar">
        <el-input v-model="query.username" placeholder="操作人" clearable style="width: 160px" @keyup.enter="search" />
        <el-input v-model="query.module" placeholder="模块（如 用户管理）" clearable style="width: 180px" @keyup.enter="search" />
        <el-input v-model="query.action" placeholder="操作描述关键字" clearable style="width: 180px" @keyup.enter="search" />
        <el-select v-model="query.success" placeholder="结果" clearable style="width: 120px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <el-button type="primary" @click="search" :loading="loading">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>

      <el-table v-loading="loading" :data="rows" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="操作人" width="120">
          <template #default="{ row }">
            {{ row.username || '（匿名）' }}
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="110" />
        <el-table-column prop="action" label="操作" width="140" />
        <el-table-column label="请求" min-width="220">
          <template #default="{ row }">
            <el-tag size="small" effect="plain" class="method-tag">{{ row.httpMethod }}</el-tag>
            <span class="uri">{{ row.uri }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="来源 IP" width="130" />
        <el-table-column label="结果" width="90">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">{{ row.costMs }} ms</template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="详情" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'err-text': row.success !== 1 }">{{ row.detail || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="mt-16"
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { logApi } from '../api/log'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({ username: '', module: '', action: '', success: null, pageNum: 1, pageSize: 10 })

const load = async () => {
  loading.value = true
  try {
    const res = await logApi.page({ ...query })
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const search = () => {
  query.pageNum = 1
  load()
}

const reset = () => {
  query.username = ''
  query.module = ''
  query.action = ''
  query.success = null
  search()
}

const onPageChange = (p) => {
  query.pageNum = p
  load()
}

const onSizeChange = (s) => {
  query.pageSize = s
  query.pageNum = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.method-tag {
  margin-right: 6px;
}

.uri {
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
  color: #606266;
}

.err-text {
  color: #f56c6c;
}
</style>
