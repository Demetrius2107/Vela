import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useMessageStore = defineStore('message', () => {
  const messages = ref({})

  function setMessages(convId, list) {
    messages.value[convId] = list
  }

  function appendMessage(convId, msg) {
    if (!messages.value[convId]) {
      messages.value[convId] = []
    }
    messages.value[convId].push(msg)
  }

  function getMessages(convId) {
    return messages.value[convId] || []
  }

  return { messages, setMessages, appendMessage, getMessages }
})
