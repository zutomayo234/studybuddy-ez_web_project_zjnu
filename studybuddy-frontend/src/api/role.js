import request from './request'

export const roleApi = {

  list() {
    return request.get('/roles')
  },

  permissions() {
    return request.get('/roles/permissions')
  },

  updatePermissions(id, permIds) {
    return request.put(`/roles/${id}/permissions`, { permIds })
  }
}
