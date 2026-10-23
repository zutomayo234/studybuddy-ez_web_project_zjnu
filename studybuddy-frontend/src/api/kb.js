import request from './request'

export const kbApi = {

  chunks() {
    return request.get('/kb/chunks')
  },

  stats() {
    return request.get('/kb/stats')
  },

  reload() {
    return request.post('/kb/reload')
  },

  addChunk(content) {
    return request.post('/kb/chunks', { content })
  }
}
