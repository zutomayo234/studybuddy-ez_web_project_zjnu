<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">学习搭子</div>
      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item v-for="m in visibleMenus" :key="m.path" :index="m.path">
          <span>{{ m.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="title">{{ route.meta.title || '学习搭子' }}</div>
        <div class="right">
          <el-tag type="success" effect="plain">{{ user.nickname || user.username }}</el-tag>
          <el-tag v-for="r in roleLabels" :key="r" class="role-tag" type="warning" effect="plain">
            {{ r }}
          </el-tag>
          <el-button link type="primary" @click="router.push('/profile')">个人中心</el-button>
          <el-button link type="primary" @click="openApiDoc">接口文档</el-button>
          <el-button link type="danger" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { clearLogin, currentPermissions, currentRoles } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const user = ref(readUser())
const roles = ref(currentRoles())
const permissions = ref(currentPermissions())

function readUser() {
  try {
    return JSON.parse(localStorage.getItem('sb_user') || '{}')
  } catch (e) {
    return {}
  }
}

const roleLabels = computed(() =>
  roles.value.map((r) => (r || '').replace('ROLE_', ''))
)

const menus = [
  { path: '/home', label: '首页概览' },
  { path: '/users', label: '用户管理', perm: 'user:list' },
  { path: '/roles', label: '角色权限', perm: 'role:list' },
  { path: '/chat', label: '学习搭子 · 智能问答', perm: 'ai:chat' },
  { path: '/kb', label: '知识库管理', perm: 'kb:manage' },
  { path: '/logs', label: '操作日志', perm: 'log:list' }
]

const visibleMenus = computed(() => {
  const isAdmin = roles.value.includes('ROLE_ADMIN')
  return menus.filter((m) => !m.perm || isAdmin || permissions.value.includes(m.perm))
})

const openApiDoc = () => {
  window.open('http://localhost:8080/swagger-ui/index.html', '_blank')
}

const logout = () => {
  clearLogin()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.aside {
  background: #ffffff;
  border-right: 1px solid #e4e7ed;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
  color: #409eff;
  border-bottom: 1px solid #e4e7ed;
}

.menu {
  border-right: none;
}

.header {
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 16px;
  font-weight: 600;
}

.right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-tag {
  margin-right: 4px;
}

.main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
