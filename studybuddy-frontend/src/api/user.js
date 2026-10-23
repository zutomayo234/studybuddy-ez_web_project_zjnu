import request from './request'

export const userApi = {

  page(params) {
    return request.get('/users', { params })
  },

  detail(id) {
    return request.get(`/users/${id}`)
  },

  create(data) {
    return request.post('/users', data)
  },

  update(id, data) {
    return request.put(`/users/${id}`, data)
  },

  remove(id) {
    return request.delete(`/users/${id}`)
  },

  resetPassword(id, password) {
    return request.put(`/users/${id}/password`, null, { params: { password } })
  },

  assignRoles(id, roleIds) {
    return request.put(`/users/${id}/roles`, { roleIds })
  }
}
