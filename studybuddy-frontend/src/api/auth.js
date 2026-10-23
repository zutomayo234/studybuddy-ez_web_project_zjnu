import request from './request'

export const authApi = {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },

  me() {
    return request.get('/auth/me')
  },

  changePassword(data) {
    return request.put('/auth/password', data)
  }
}
