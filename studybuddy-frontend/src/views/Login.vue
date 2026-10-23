<template>
  <div class="login-wrap">
    <el-card class="login-card">
      <div class="brand">
        <h1>学习搭子 <span>StudyBuddy</span></h1>
        <p>《团队规范项目实训》00931 · 方野 20243190415</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="onLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="admin / teacher / student" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="admin123 / teacher123 / student123" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" style="width: 100%" @click="onLogin">
          登录
        </el-button>
      </el-form>

      <el-alert
        class="mt-16"
        type="info"
        :closable="false"
        title="演示账号（角色不同，可见菜单也不同）"
        description="admin/admin123（管理员，全部功能）、teacher/teacher123（教师，知识库与日志）、student/student123（学生，仅问答）"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '../api/auth'
import { saveLogin } from '../utils/auth'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const { data } = await authApi.login(form)

    saveLogin(data)
    ElMessage.success('登录成功')
    router.push('/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e8f0ff 0%, #f5f7fa 60%, #eef7f0 100%);
}

.login-card {
  width: 440px;
  padding: 8px;
  box-shadow: 0 8px 32px rgba(64, 128, 255, 0.12);
}

.brand {
  text-align: center;
  margin-bottom: 12px;
}

.brand h1 {
  font-size: 24px;
  margin: 0 0 6px;
}

.brand h1 span {
  color: #409eff;
}

.brand p {
  color: #909399;
  font-size: 12px;
  margin: 0 0 8px;
}
</style>
