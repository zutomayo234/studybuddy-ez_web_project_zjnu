import request from './request'

export const statsApi = {
  overview() {
    return request.get('/stats/overview')
  }
}
