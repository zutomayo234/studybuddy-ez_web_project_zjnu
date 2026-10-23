<template>
  <div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按用户名 / 昵称 / 邮箱搜索" clearable style="width: 260px" @keyup.enter="load" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
      <el-button type="success" @click="openCreate">新增用户</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="phone" label="手机号(脱敏)" width="140" />
      <el-table-column prop="genderText" label="性别" width="70" />
      <el-table-column label="角色" width="180">
        <template #default="{ row }">
          <el-tag v-for="r in row.roleCodes" :key="r" size="small" class="role-tag">{{ r }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="success" @click="openAssign(row)">分配角色</el-button>
          <el-button link type="warning" @click="onResetPwd(row)">重置密码</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
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
      :page-sizes="[5, 10, 20, 50]"
      @current-change="onPageChange"
      @size-change="onSizeChange"
    />

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="3~20 位" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6~32 位，入库自动 BCrypt 加密" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" :title="`分配角色 · ${assignTarget.username || ''}`" width="480px">
      <el-alert
        class="mb-16"
        type="info"
        :closable="false"
        title="角色为覆盖式分配"
        description="提交后该用户的角色集合即等于勾选项。系统内置保护：不允许移除最后一名管理员的管理员角色。"
      />
      <el-checkbox-group v-model="checkedRoleIds" class="role-group">
        <el-checkbox v-for="r in roleOptions" :key="r.id" :value="r.id" class="role-item">
          <span class="role-code">{{ r.roleCode }}</span>
          <span class="role-name">{{ r.roleName }}</span>
          <span class="role-desc">{{ r.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api/user'
import { roleApi } from '../api/role'

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref()

const assignVisible = ref(false)
const assigning = ref(false)
const assignTarget = reactive({ id: null, username: '' })
const roleOptions = ref([])
const checkedRoleIds = ref([])

const query = reactive({ keyword: '', status: null, pageNum: 1, pageSize: 10 })
const form = reactive({
  username: '', password: '', nickname: '', email: '', phone: '',
  gender: 0, status: 1, remark: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const load = async () => {
  loading.value = true
  try {
    const res = await userApi.page({ ...query })
    rows.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  query.keyword = ''
  query.status = null
  query.pageNum = 1
  load()
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

const clearForm = () => {
  Object.assign(form, {
    username: '', password: '', nickname: '', email: '', phone: '',
    gender: 0, status: 1, remark: ''
  })
}

const openCreate = () => {
  isEdit.value = false
  editingId.value = null
  clearForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editingId.value = row.id
  clearForm()
  Object.assign(form, {
    username: row.username, nickname: row.nickname, email: row.email,
    phone: row.phone, gender: row.gender, status: row.status, remark: row.remark
  })
  dialogVisible.value = true
}

const submit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (isEdit.value) {
      const { username, password, ...payload } = form
      await userApi.update(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await userApi.create(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    load()
  } finally {
    submitting.value = false
  }
}

const onDelete = async (row) => {
  await ElMessageBox.confirm(`确认删除用户「${row.username}」吗？`, '提示', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('删除成功')
  load()
}

const onResetPwd = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入新密码（6 位以上）', '重置密码', {
    inputPattern: /^.{6,}$/,
    inputErrorMessage: '密码至少 6 位'
  })
  await userApi.resetPassword(row.id, value)
  ElMessage.success('密码已重置（已 BCrypt 加密入库）')
}

const openAssign = async (row) => {
  assignTarget.id = row.id
  assignTarget.username = row.username

  if (row.roleIds && row.roleIds.length) {
    checkedRoleIds.value = [...row.roleIds]
  } else {
    checkedRoleIds.value = roleOptions.value
      .filter((r) => (row.roleCodes || []).includes(r.roleCode))
      .map((r) => r.id)
  }
  if (!roleOptions.value.length) {
    try {
      const res = await roleApi.list()
      roleOptions.value = res.data
    } catch (e) {
      return
    }
  }
  assignVisible.value = true
}

const submitAssign = async () => {
  if (!checkedRoleIds.value.length) {
    ElMessage.warning('至少选择一个角色')
    return
  }
  assigning.value = true
  try {
    await userApi.assignRoles(assignTarget.id, checkedRoleIds.value)
    ElMessage.success('角色已更新')
    assignVisible.value = false
    load()
  } finally {
    assigning.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.role-tag {
  margin-right: 4px;
}

.role-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-item {
  width: 100%;
  height: 30px;
}

.role-code {
  display: inline-block;
  width: 130px;
  font-family: Consolas, Monaco, monospace;
  color: #409eff;
}

.role-name {
  display: inline-block;
  width: 90px;
  color: #606266;
}

.role-desc {
  color: #909399;
  font-size: 12px;
}
</style>
