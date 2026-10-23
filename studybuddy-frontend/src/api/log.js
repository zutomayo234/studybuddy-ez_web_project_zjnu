import request from './request'

export const logApi = {
  page(params) {
    return request.get('/logs', { params })
  }
}
