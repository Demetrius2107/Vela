<template>
  <div>
    <div style="font-size:22px;font-weight:700;color:#1a1a2e;margin-bottom:16px">⚙️ 系统配置</div>

    <n-table :single-line="true" size="small" style="background:#fff;border-radius:8px">
      <thead><tr style="background:#fafafa">
        <th>配置项</th><th>值</th><th>说明</th><th style="width:100px">操作</th>
      </tr></thead>
      <tbody>
        <tr v-for="c in configs" :key="c.id">
          <td style="font-family:monospace;font-size:13px">{{ c.configKey }}</td>
          <td><n-input v-model:value="editValues[c.id]" size="small" style="width:200px" /></td>
          <td style="color:#888;font-size:13px">{{ c.description }}</td>
          <td><n-button size="tiny" type="primary" @click="save(c.id)">保存</n-button></td>
        </tr>
        <tr v-if="!configs.length"><td colspan="4" style="text-align:center;padding:40px;color:#888">暂无配置</td></tr>
      </tbody>
    </n-table>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const configs = ref([])
const editValues = reactive({})

async function load() {
  try {
    const { data } = await axios.get(`${VELA.API_URL}/v1/admin/configs`)
    if (data.code === 200) {
      configs.value = data.data || []
      configs.value.forEach(c => { editValues[c.id] = c.configValue })
    }
  } catch (e) { msg.error('加载配置失败') }
}

async function save(id) {
  try {
    const { data } = await axios.post(`${VELA.API_URL}/v1/admin/configs/update`, null, { params: { id, value: editValues[id] } })
    if (data.code === 200) { msg.success('已保存') }
    else msg.error(data.msg)
  } catch (e) { msg.error('保存失败') }
}

onMounted(load)
</script>
