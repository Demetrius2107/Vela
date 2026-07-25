import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useConversationStore = defineStore('conversation', () => {
  const list = ref([])
  const currentId = ref(null)

  function setList(conversations) {
    list.value = conversations
  }

  function select(id) {
    currentId.value = id
  }

  function addMessage(convId, msg) {
    const conv = list.value.find(c => c.id === convId)
    if (conv) {
      conv.lastMessage = msg.content
      conv.time = msg.time
      conv.unread = (conv.unread || 0) + 1
    }
  }

  function markRead(convId) {
    const conv = list.value.find(c => c.id === convId)
    if (conv) conv.unread = 0
  }

  return { list, currentId, setList, select, addMessage, markRead }
})
