<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <n-card title="创建群聊" style="width: 520px">
        <n-form :model="form" label-placement="top">
          <n-form-item label="群名称" path="name">
            <n-input v-model:value="form.name" placeholder="给群聊起个名字" :maxlength="30" show-count />
          </n-form-item>
          <n-form-item label="群介绍">
            <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="介绍一下这个群..." :maxlength="200" show-count />
          </n-form-item>
          <n-form-item label="选择成员">
            <n-space vertical style="width: 100%">
              <n-input v-model:value="searchText" placeholder="搜索好友添加..." clearable @input="handleSearch">
                <template #prefix><n-icon><search-outline /></n-icon></template>
              </n-input>
              <n-checkbox-group v-model:value="selectedIds">
                <n-space>
                  <n-checkbox v-for="f in filteredFriends" :key="f.userId" :value="f.userId" :label="f.nickname" />
                </n-space>
              </n-checkbox-group>
            </n-space>
          </n-form-item>
          <n-form-item label="已选成员">
            <n-space>
              <n-tag v-for="id in selectedIds" :key="id" closable @close="removeMember(id)" type="primary">
                {{ getFriendName(id) }}
              </n-tag>
            </n-space>
            <n-text v-if="selectedIds.length === 0" depth="3">尚未选择成员</n-text>
          </n-form-item>
        </n-form>
        <n-button type="primary" block :loading="creating" @click="handleCreate">创建群聊</n-button>
      </n-card>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { NIcon } from 'naive-ui'
import { SearchOutline } from '@vicons/ionicons5'
import NavHeader from '../../components/layout/NavHeader.vue'

const router = useRouter()
const msg = useMessage()
const creating = ref(false)
const searchText = ref('')
const selectedIds = ref([])

const form = reactive({
  name: '',
  description: ''
})

const friends = ref([
  { userId: 'user002', nickname: '张三' },
  { userId: 'user003', nickname: '李四' },
  { userId: 'user004', nickname: '王五' },
  { userId: 'user005', nickname: '赵六' }
])

const filteredFriends = computed(() => {
  if (!searchText.value) return friends.value
  return friends.value.filter(f =>
    f.nickname.includes(searchText.value) || f.userId.includes(searchText.value)
  )
})

function getFriendName(userId) {
  return friends.value.find(f => f.userId === userId)?.nickname || userId
}

function removeMember(userId) {
  selectedIds.value = selectedIds.value.filter(id => id !== userId)
}

async function handleCreate() {
  if (!form.name.trim()) {
    msg.warning('请输入群名称')
    return
  }
  creating.value = true
  try {
    // TODO: 调用创建群组接口
    setTimeout(() => {
      msg.success('群聊创建成功')
      router.push('/chat')
    }, 500)
  } finally {
    creating.value = false
  }
}
</script>
