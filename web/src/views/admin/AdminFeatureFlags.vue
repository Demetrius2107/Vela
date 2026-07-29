<template>
  <div>
    <div style="font-size:22px;font-weight:700;color:#1a1a2e;margin-bottom:16px">🔌 功能开关</div>
    <n-card style="border-radius:8px">
      <div style="font-size:13px;color:#888;margin-bottom:16px">集中管理所有功能的灰度发布、紧急下线、白名单。</div>
      <n-data-table :columns="columns" :data="list" :loading="loading" :bordered="false" :single-line="true" size="small" />
    </n-card>

    <!-- 编辑开关抽屉 -->
    <n-drawer v-model:show="showDrawer" :width="420" placement="right">
      <n-drawer-content title="编辑功能开关" closable>
        <div v-if="editItem">
          <div style="margin-bottom:20px">
            <div style="font-weight:600;font-size:16px;color:#1a1a2e">{{ editItem.flagName }}</div>
            <div style="font-size:13px;color:#888;margin-top:4px">{{ editItem.description }}</div>
          </div>
          <n-form label-placement="top" size="small">
            <n-form-item label="状态">
              <n-radio-group v-model:value="editForm.enabled">
                <n-radio :value="1">✅ 开启</n-radio>
                <n-radio :value="0">❌ 关闭</n-radio>
              </n-radio-group>
            </n-form-item>
            <n-form-item label="灰度白名单（JSON 数组，仅对指定用户开启）">
              <n-input v-model:value="editForm.userWhitelist" type="textarea" rows="4" placeholder='["user001","user002"]' />
            </n-form-item>
            <n-button type="primary" block @click="handleSave" :loading="saving" style="background:linear-gradient(135deg,#4F6EF7,#7C3AED);border:none">保存</n-button>
          </n-form>
        </div>
      </n-drawer-content>
    </n-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, h } from 'vue'
import { useMessage } from 'naive-ui'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const list = ref([])
const loading = ref(false)
const showDrawer = ref(false)
const saving = ref(false)
const editItem = ref(null)
const editForm = reactive({ enabled: 1, userWhitelist: '' })

const columns = [
  { title: '标识', key: 'flagKey', width: 140 },
  { title: '名称', key: 'flagName', width: 120 },
  {
    title: '状态', key: 'enabled', width: 80,
    render: (r) => h('n-tag', { size: 'tiny', type: r.enabled === 1 ? 'success' : 'error', round: true },
      { default: () => r.enabled === 1 ? '已开启' : '已关闭' })
  },
  { title: '白名单', key: 'userWhitelist', ellipsis: { tooltip: true }, render: (r) => r.userWhitelist || '-' },
  { title: '描述', key: 'description', ellipsis: { tooltip: true } },
  { title: '更新时间', key: 'updateTime', width: 160, render: (r) => r.updateTime ? new Date(r.updateTime).toLocaleString() : '-' },
  {
    title: '操作', width: 80,
    render: (r) => h('n-button', { size: 'tiny', quaternary: true, onClick: () => openEdit(r) }, { default: () => '编辑' })
  }
]

async function load() {
  loading.value = true
  try {
    const res = await axios.get(`${VELA.API_URL}/v1/admin/feature-flags`, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' }
    })
    if (res.data.code === 200) list.value = res.data.data || []
  } catch (e) { msg.error('加载失败') }
  finally { loading.value = false }
}

function openEdit(item) {
  editItem.value = item
  editForm.enabled = item.enabled
  editForm.userWhitelist = item.userWhitelist || ''
  showDrawer.value = true
}

async function handleSave() {
  if (!editItem.value) return
  saving.value = true
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/admin/feature-flags/update`, null, {
      headers: { 'X-Admin-Role': localStorage.getItem('vela_admin_role') || 'admin' },
      params: { id: editItem.value.id, enabled: editForm.enabled, userWhitelist: editForm.userWhitelist || '' }
    })
    if (res.data.code === 200) { msg.success('已更新'); showDrawer.value = false; load() }
    else { msg.error(res.data.msg || '更新失败') }
  } catch (e) { msg.error('更新失败') }
  finally { saving.value = false }
}

load()
</script>
