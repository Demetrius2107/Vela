<template>
  <div style="min-height: 100vh; background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%)">
    <NavHeader />
    <div style="max-width: 720px; margin: 0 auto; padding: 32px 20px">
      <!-- 头像卡片 -->
      <n-card :bordered="false" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <div style="display: flex; align-items: center; gap: 24px">
          <div style="position: relative">
            <n-avatar round :size="80" color="#2080f0" style="font-size: 32px; box-shadow: 0 4px 12px rgba(32,128,240,0.3)">
              {{ form.nickname?.[0] || form.userId?.[0] || 'U' }}
            </n-avatar>
            <div style="position: absolute; bottom: 0; right: -4px; width: 24px; height: 24px; border-radius: 50%; background: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; box-shadow: 0 2px 6px rgba(0,0,0,0.15)" @click="handleUploadAvatar">📷</div>
          </div>
          <div style="flex: 1">
            <div style="font-size: 22px; font-weight: 700; color: #1a1a2e">{{ form.nickname || form.userId || '用户' }}</div>
            <div style="font-size: 13px; color: #888; margin-top: 4px; display: flex; align-items: center; gap: 8px">
              <span>ID: {{ form.userId }}</span>
              <span style="width: 4px; height: 4px; border-radius: 50%; background: #ddd" />
              <span>{{ stats.friendCount || 0 }} 好友</span>
              <span style="width: 4px; height: 4px; border-radius: 50%; background: #ddd" />
              <span>{{ stats.groupCount || 0 }} 群组</span>
            </div>
          </div>
          <n-button :loading="saving" type="primary" round @click="handleSave">保存</n-button>
        </div>
      </n-card>

      <!-- 个人信息 -->
      <n-card :bordered="false" title="个人信息" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <n-form :model="form" label-placement="left" label-width="100" size="medium">
          <n-form-item label="用户 ID">
            <n-input :value="form.userId" disabled style="color: #999" />
          </n-form-item>
          <n-form-item label="昵称">
            <n-input v-model:value="form.nickname" placeholder="设置你的显示名称" :maxlength="20" clearable>
              <template #suffix>{{ (form.nickname || '').length }}/20</template>
            </n-input>
          </n-form-item>
          <n-form-item label="个性签名">
            <n-input v-model:value="form.signature" type="textarea" :rows="3" placeholder="介绍一下自己..." :maxlength="100" show-count />
          </n-form-item>
          <n-form-item label="性别">
            <n-radio-group v-model:value="form.gender">
              <n-radio-button value="male">👨 男</n-radio-button>
              <n-radio-button value="female">👩 女</n-radio-button>
              <n-radio-button value="secret">🔒 保密</n-radio-button>
            </n-radio-group>
          </n-form-item>
          <n-form-item label="地区">
            <n-input v-model:value="form.location" placeholder="如: 中国 · 北京" clearable />
          </n-form-item>
        </n-form>
      </n-card>

      <!-- 账号统计 -->
      <n-card :bordered="false" title="账号统计" style="border-radius: 16px; margin-bottom: 20px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 16px; text-align: center">
          <div style="padding: 16px; background: #f0f5ff; border-radius: 12px">
            <div style="font-size: 24px; font-weight: 700; color: #2080f0">{{ stats.friendCount || '-' }}</div>
            <div style="font-size: 12px; color: #888; margin-top: 4px">好友</div>
          </div>
          <div style="padding: 16px; background: #f0fff0; border-radius: 12px">
            <div style="font-size: 24px; font-weight: 700; color: #18a058">{{ stats.groupCount || '-' }}</div>
            <div style="font-size: 12px; color: #888; margin-top: 4px">群组</div>
          </div>
          <div style="padding: 16px; background: #fff7e6; border-radius: 12px">
            <div style="font-size: 24px; font-weight: 700; color: #faad14">{{ stats.msgCount || '-' }}</div>
            <div style="font-size: 12px; color: #888; margin-top: 4px">消息</div>
          </div>
        </div>
      </n-card>

      <!-- 账号安全 -->
      <n-card :bordered="false" title="账号安全" style="border-radius: 16px; box-shadow: 0 4px 16px rgba(0,0,0,0.06)">
        <n-space vertical>
          <div v-for="item in securityItems" :key="item.key" style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0">
            <div>
              <div style="font-weight: 500; font-size: 14px; color: #333">{{ item.label }}</div>
              <div style="font-size: 12px; color: #999; margin-top: 2px">{{ item.desc }}</div>
            </div>
            <n-button size="tiny" quaternary @click="item.action">{{ item.btnText }}</n-button>
          </div>
        </n-space>
      </n-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import { useRouter } from 'vue-router'
import NavHeader from '../../components/layout/NavHeader.vue'
import { useUserStore } from '../../stores/user'
import axios from 'axios'
import { VELA } from '../../utils/constants'

const msg = useMessage()
const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)

const form = reactive({
  userId: '',
  nickname: '',
  signature: '',
  gender: 'secret',
  location: ''
})

const stats = reactive({
  friendCount: 0,
  groupCount: 0,
  msgCount: 0
})

const securityItems = [
  { key: 'password', label: '修改密码', desc: '定期更换密码保障账号安全', btnText: '修改', action: () => msg.info('密码修改功能待开发') },
  { key: 'account', label: '当前账号', desc: localStorage.getItem('vela_user_id') || '未登录', btnText: '', action: () => {} }
]

async function loadProfile() {
  form.userId = localStorage.getItem('vela_user_id') || ''
  form.nickname = form.userId
  
  // 加载用户资料
  if (form.userId) {
    try {
      const res = await axios.get(`${VELA.API_URL}/v1/admin/users/detail`, { params: { userId: form.userId } })
      if (res.data.code === 200 && res.data.data) {
        const data = res.data.data
        form.nickname = data.nickName || form.userId
        form.signature = data.selfSignature || ''
        form.location = data.location || ''
        // 性别映射
        if (data.userSex === 1) form.gender = 'male'
        else if (data.userSex === 2) form.gender = 'female'
        else form.gender = 'secret'
      }
    } catch (e) { /* 使用默认值 */ }
  }
}

async function handleSave() {
  saving.value = true
  try {
    const res = await axios.post(`${VELA.API_URL}/v1/admin/users/update`, null, {
      headers: { 'X-Admin-Role': 'admin' },
      params: {
        userId: form.userId,
        nickName: form.nickname || form.userId,
        selfSignature: form.signature || '',
        userSex: form.gender === 'male' ? 1 : form.gender === 'female' ? 2 : 0,
        location: form.location || ''
      }
    })
    if (res.data.code === 200) msg.success('保存成功')
    else msg.error(res.data.msg || '保存失败')
  } catch (e) { msg.error('保存失败') }
  finally { saving.value = false }
}

function handleUploadAvatar() {
  msg.info('头像上传功能待对接')
}

onMounted(loadProfile)
</script>
