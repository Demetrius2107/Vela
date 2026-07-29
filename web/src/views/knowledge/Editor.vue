<template>
  <div style="min-height:100vh;background:#fff">
    <div style="border-bottom:1px solid #e8e8e8;padding:12px 24px;display:flex;align-items:center;gap:12px">
      <n-button quaternary @click="$router.push('/knowledge')">← 返回</n-button>
      <n-input v-model:value="title" placeholder="文档标题" style="flex:1;font-size:18px;font-weight:600;border:none" />
      <n-button type="primary" @click="save" :loading="saving">保存</n-button>
    </div>
    <div style="max-width:860px;margin:0 auto;padding:24px">
      <textarea v-model="content" placeholder="开始写作..." style="width:100%;min-height:60vh;border:none;outline:none;font-size:16px;line-height:1.8;resize:vertical;font-family:inherit" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const route = useRoute()
const router = useRouter()
const msg = useMessage()
const title = ref('')
const content = ref('')
const saving = ref(false)
const docId = route.query.id

onMounted(async () => {
  if (docId) {
    try {
      const { data } = await axios.get(`${VELA.API_URL}/v1/knowledge/get`, { params: { id: docId } })
      if (data.code === 200) {
        title.value = data.data.title || ''
        content.value = data.data.content || ''
      }
    } catch(e) { msg.error('加载失败') }
  }
})

async function save() {
  saving.value = true
  try {
    if (docId) {
      await axios.post(`${VELA.API_URL}/v1/knowledge/update`, { id: parseInt(docId), title: title.value, content: content.value })
      msg.success('已保存')
    } else {
      const { data } = await axios.post(`${VELA.API_URL}/v1/knowledge/create`, { appId:10000, title: title.value, content: content.value, creatorId:'user001' })
      if (data.code === 200) router.replace('/knowledge/edit?id=' + data.data.id)
      msg.success('已创建')
    }
  } catch(e) { msg.error('保存失败') }
  finally { saving.value = false }
}
</script>
