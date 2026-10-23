<template>
  <div>
    <el-alert
      class="mb-16"
      type="info"
      :closable="false"
      title="RBAC 权限模型（实验四 + 功能增强）"
      description="用户 → 角色 → 权限 三级关联。角色权限为覆盖式调整：提交后该角色的权限集合即等于勾选项。调整会立即影响已登录用户的鉴权结果，请谨慎操作。"
    />

    <el-card>
      <div class="toolbar">
        <el-button @click="load" :loading="loading">刷新</el-button>
        <span class="tip">共 {{ roles.length }} 个角色 · {{ permissions.length }} 个权限点</span>
      </div>

      <el-table v-loading="loading" :data="roles" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="130" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="挂载用户" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.userCount }} 人</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限数" width="90">
          <template #default="{ row }">
            <el-tag size="small" type="success">{{ (row.permissions || []).length }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限明细" min-width="300">
          <template #default="{ row }">
            <el-tag
              v-for="p in row.permissions"
              :key="p.id"
              size="small"
              class="perm-tag"
              effect="plain"
            >
              {{ p.permCode }}
            </el-tag>
            <span v-if="!(row.permissions || []).length" class="empty-text">（无权限）</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">调整权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="`调整权限 · ${editing.roleName || ''}`" width="640px">
      <el-alert
        class="mb-16"
        type="warning"
        :closable="false"
        :title="`当前该角色下已挂载 ${editing.userCount || 0} 个用户，权限调整会立即对其生效`"
      />
      <el-checkbox-group v-model="checkedPermIds" class="perm-group">
        <el-checkbox v-for="p in permissions" :key="p.id" :value="p.id" class="perm-item">
          <span class="perm-code">{{ p.permCode }}</span>
          <span class="perm-name">{{ p.permName }}</span>
          <el-tag size="small" type="info" effect="plain">{{ p.resourceType }}</el-tag>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { roleApi } from '../api/role'

const loading = ref(false)
const submitting = ref(false)
const roles = ref([])
const permissions = ref([])
const dialogVisible = ref(false)
const checkedPermIds = ref([])
const editing = reactive({ id: null, roleName: '', userCount: 0 })

const load = async () => {
  loading.value = true
  try {
    const [roleRes, permRes] = await Promise.all([roleApi.list(), roleApi.permissions()])
    roles.value = roleRes.data
    permissions.value = permRes.data
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  editing.id = row.id
  editing.roleName = row.roleName
  editing.userCount = row.userCount

  checkedPermIds.value = (row.permissions || []).map((p) => p.id)
  dialogVisible.value = true
}

const submit = async () => {
  if (!checkedPermIds.value.length) {
    ElMessage.warning('至少保留一个权限')
    return
  }
  submitting.value = true
  try {
    await roleApi.updatePermissions(editing.id, checkedPermIds.value)
    ElMessage.success('角色权限已更新（已登录用户需重新登录后生效）')
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.tip {
  color: #909399;
  font-size: 13px;
}

.perm-tag {
  margin-right: 4px;
  margin-bottom: 4px;
}

.empty-text {
  color: #c0c4cc;
  font-size: 12px;
}

.perm-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.perm-item {
  width: 100%;
  height: 30px;
}

.perm-code {
  display: inline-block;
  width: 110px;
  font-family: Consolas, Monaco, monospace;
  color: #409eff;
}

.perm-name {
  display: inline-block;
  width: 100px;
  color: #606266;
}
</style>
