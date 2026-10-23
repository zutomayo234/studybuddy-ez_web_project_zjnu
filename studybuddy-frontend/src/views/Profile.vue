<template>
  <div>
    <el-row :gutter="16">

      <el-col :span="10">
        <el-card>
          <h3 class="page-title">个人资料</h3>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
            <el-descriptions-item label="昵称">{{ user.nickname || '—' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ user.email || '—' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ user.phone || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ user.genderText || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="user.status === 1 ? 'success' : 'danger'" size="small">
                {{ user.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ user.createTime }}</el-descriptions-item>
          </el-descriptions>

          <el-divider content-position="left">我的角色与权限</el-divider>
          <div class="tag-block">
            <el-tag v-for="r in me.roles" :key="r" type="warning" effect="plain" class="tag">{{ r }}</el-tag>
          </div>
          <div class="tag-block">
            <el-tag v-for="p in me.permissions" :key="p" type="success" effect="plain" class="tag">
              {{ p }}
            </el-tag>
            <span v-if="!me.permissions || !me.permissions.length" class="empty-text">（无细粒度权限）</span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-card>
          <h3 class="page-title">修改密码</h3>
          <el-alert
            class="mb-16"
            type="warning"
            :closable="false"
            title="修改密码必须提供旧密码"
            description="这是「修改本人密码」与「管理员重置密码」的核心区别：前者校验旧密码，避免令牌一旦被盗，攻击者可直接改密把账号锁死。"
          />
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="110px" style="max-width: 460px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6~32 位，入库自动 BCrypt 加密" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="onChangePassword">提交</el-button>
              <el-button @click="resetPwdForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="mt-16">
          <h3 class="page-title">安全提示</h3>
          <ul class="tips">
            <li>密码在数据库中以 BCrypt 密文存储，同一明文每次加密结果不同（随机盐）。</li>
            <li>本系统令牌存于 localStorage，仅为教学演示；生产环境建议 HttpOnly Cookie。</li>
            <li>修改密码后建议重新登录，以确保各处会话使用的凭据一致。</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { authApi } from '../api/auth'
import { clearLogin, currentUser, updateCachedUser } from '../utils/auth'

const router = useRouter()
const user = ref(currentUser())
const me = ref({ roles: [], permissions: [] })
const submitting = ref(false)
const pwdFormRef = ref()

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '新密码长度需在 6~32 之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const load = async () => {
  try {
    const res = await authApi.me()
    me.value = res.data
    if (res.data.user) {
      user.value = res.data.user
      updateCachedUser(res.data.user)
    }
  } catch (e) {

  }
}

const resetPwdForm = () => {
  pwdFormRef.value.resetFields()
}

const onChangePassword = async () => {
  await pwdFormRef.value.validate()
  submitting.value = true
  try {
    await authApi.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success('密码修改成功，请使用新密码重新登录')

    await ElMessageBox.alert('密码已更新，需要重新登录以继续使用。', '提示', { type: 'success' })
    clearLogin()
    router.push('/login')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.tag-block {
  margin-bottom: 10px;
  line-height: 26px;
}

.tag {
  margin-right: 6px;
  margin-bottom: 4px;
}

.empty-text {
  color: #c0c4cc;
  font-size: 12px;
}

.tips {
  margin: 0;
  padding-left: 20px;
  color: #606266;
  font-size: 13px;
  line-height: 2;
}
</style>
