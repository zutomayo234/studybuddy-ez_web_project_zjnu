const TOKEN_KEY = 'sb_token'
const USER_KEY = 'sb_user'
const ROLES_KEY = 'sb_roles'
const PERMS_KEY = 'sb_permissions'

export function saveLogin(data) {
  localStorage.setItem(TOKEN_KEY, data.token)
  localStorage.setItem(USER_KEY, JSON.stringify(data.user || {}))
  localStorage.setItem(ROLES_KEY, JSON.stringify(data.roles || []))
  localStorage.setItem(PERMS_KEY, JSON.stringify(data.permissions || []))
}

export function currentUser() {
  return readJson(USER_KEY, {})
}

export function currentRoles() {
  return readJson(ROLES_KEY, [])
}

export function currentPermissions() {
  return readJson(PERMS_KEY, [])
}

export function updateCachedUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user || {}))
}

export function hasPermission(perm) {
  return currentPermissions().includes(perm) || currentRoles().includes('ROLE_ADMIN')
}

export function hasAnyPermission(perms) {
  if (currentRoles().includes('ROLE_ADMIN')) {
    return true
  }
  const owned = currentPermissions()
  return perms.some((p) => owned.includes(p))
}

export function clearLogin() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  localStorage.removeItem(ROLES_KEY)
  localStorage.removeItem(PERMS_KEY)
}

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch (e) {
    return fallback
  }
}
