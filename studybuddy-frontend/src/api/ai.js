import request from './request'

export const aiApi = {

  chat(data) {
    return request.post('/ai/chat', data)
  },

  sessions() {
    return request.get('/ai/sessions')
  },

  messages(sessionId) {
    return request.get(`/ai/sessions/${sessionId}/messages`)
  },

  removeSession(sessionId) {
    return request.delete(`/ai/sessions/${sessionId}`)
  }
}
