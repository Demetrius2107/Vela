<template>
  <n-layout style="height: 100vh">
    <NavHeader />
    <n-layout position="absolute" style="top: 48px; bottom: 0; padding: 24px; display: flex; justify-content: center">
      <n-card title="个人资料" style="width: 560px">
        <n-space vertical size="large">
          <!-- 头像 -->
          <n-space align="center">
            <n-avatar round :size="72" color="#2080f0">{{ form.nickname?.[0] || 'U' }}</n-avatar>
            <n-button size="small" quaternary @click="handleUploadAvatar">更换头像</n-button>
          </n-space>

          <n-form :model="form" label-placement="left" label-width="100">
            <n-form-item label="用户 ID">
              <n-input :value="form.userId" disabled />
            </n-form-item>
            <n-form-item label="昵称">
              <n-input v-model:value="form.nickname" placeholder="你的显示名称" :maxlength="20" />
            </n-form-item>
            <n-form-item label="个性签名">
              <n-input v-model:value="form.signature" type="textarea" :rows="3" placeholder="介绍一下自己..." :maxlength="100" show-count />
            </n-form-item>
            <n-form-item label="性别">
              <n-radio-group v-model:value="form.gender">
                <n-radio value="male">男</n-radio>
                <n-radio value="female">女</n-radio>
                <n-radio value="secret">保密</n-radio>
              </n-radio-group>
            </n-form-item>
          </n-form>

          <n-space justify="end">
            <n-button @click="$router.back()">取消</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave">保存</n-button>
          </n-space>
        </n-space>
      </n-card>
    </n-layout>
  </n-layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import NavHeader from '../../components/layout/NavHeader.vue'

const saving = ref(false)

const form = reactive({
  userId: '',
  nickname: '',
  signature: '',
  gender: 'secret'
})

onMounted(() => {
  form.userId = localStorage.getItem('vela_user_id') || ''
  form.nickname = form.userId
})

function handleUploadAvatar() {
  // TODO: 调用上传接口
}

async function handleSave() {
  saving.value = true
  try {
    // TODO: 调用更新接口
    // await updateUserInfo(form)
    setTimeout(() => {
      saving.value = false
    }, 500)
  } catch (e) {
    saving.value = false
  }
}
</script>
