<template>
  <div style="min-height:100vh;background:#f5f5f5">
    <div style="background:#fff;padding:0 24px;height:56px;display:flex;align-items:center;gap:16px;box-shadow:0 1px 4px rgba(0,0,0,0.06)">
      <div style="font-weight:700;font-size:18px;color:#1a1a2e">📚 知识库</div>
      <div style="flex:1" />
      <n-button type="primary" size="small" @click="$router.push('/knowledge/edit')">新建文档</n-button>
      <n-button quaternary size="tiny" @click="$router.push('/chat')">返回IM</n-button>
    </div>
    <div style="max-width:960px;margin:24px auto;padding:0 16px">
      <n-input v-model:value="keyword" placeholder="搜索文档..." clearable style="margin-bottom:16px" @keydown.enter="load" />
      <div v-for="doc in list" :key="doc.id" style="background:#fff;border-radius:8px;padding:20px;margin-bottom:12px;box-shadow:0 2px 8px rgba(0,0,0,0.04);cursor:pointer" @click="$router.push('/knowledge/edit?id='+doc.id)">
        <div style="font-weight:600;font-size:16px;color:#1a1a2e;margin-bottom:4px">{{ doc.title }}</div>
        <div style="font-size:13px;color:#888;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ doc.summary || doc.content?.substring(0,100) || '暂无内容' }}</div>
        <div style="margin-top:8px;display:flex;gap:8px;font-size:12px;color:#aaa">
          <span>{{ doc.tags }}</span>
          <span style="flex:1" />
          <span>{{ doc.updateTime ? new Date(doc.updateTime).toLocaleDateString() : '' }}</span>
        </div>
      </div>
      <div v-if="!list.length && loaded" style="text-align:center;padding:60px;color:#888">暂无文档</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const list = ref([])
const keyword = ref('')
const loaded = ref(false)

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/knowledge/list`, { params: { appId:10000, keyword: keyword.value || undefined } })
    if (data.code === 200) { list.value = data.data.list || []; loaded.value = true }
  } catch(e) {}
}
onMounted(load)
</script>
