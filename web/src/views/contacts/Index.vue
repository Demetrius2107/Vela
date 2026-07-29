<template>
  <div style="height: 100vh; display: flex; flex-direction: column; background: #fff">
    <NavHeader />
    <div style="flex: 1; display: flex; overflow: hidden">
      <!-- 左侧导航 -->
      <div style="width: 260px; border-right: 1px solid rgba(0,0,0,0.04); background: #fff; flex-shrink: 0; display: flex; flex-direction: column">
        <div style="padding: 16px 14px 8px">
          <n-input placeholder="搜索好友或群组..." round clearable size="small">
            <template #prefix><span style="color: #bbb">🔍</span></template>
          </n-input>
        </div>
        <div style="flex: 1; overflow-y: auto; padding: 8px">
          <div
            v-for="tab in tabs" :key="tab.key"
            @click="activeTab = tab.key"
            :style="{
              padding: '12px 16px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '10px',
              borderRadius: '10px', marginBottom: '2px',
              background: activeTab === tab.key ? 'linear-gradient(135deg, #EEF1FF, #F5F3FF)' : 'transparent',
              fontWeight: activeTab === tab.key ? 600 : 400, color: activeTab === tab.key ? '#4F6EF7' : '#333',
              transition: 'all 0.15s'
            }"
            @mouseenter="$event.currentTarget.style.background = activeTab === tab.key ? 'linear-gradient(135deg, #EEF1FF, #F5F3FF)' : '#f8f9ff'"
            @mouseleave="$event.currentTarget.style.background = activeTab === tab.key ? 'linear-gradient(135deg, #EEF1FF, #F5F3FF)' : 'transparent'"
          >
            <span style="font-size: 18px">{{ tab.icon }}</span>
            <span>{{ tab.label }}</span>
            <span v-if="tab.badge" style="marginLeft: 'auto'; background: 'linear-gradient(135deg, #EF4444, #DC2626)'; color: '#fff'; borderRadius: '10px'; padding: '0 8px'; fontSize: '11px'">{{ tab.badge }}</span>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div style="flex: 1; padding: 24px; overflow-y: auto; background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f2 100%)">
        <!-- 好友列表 -->
        <template v-if="activeTab === 'friends'">
          <div style="font-size: 18px; font-weight: 700; color: #1a1a2e; margin-bottom: 20px">👥 好友 · {{ friends.length }}</div>
          <div style="display: grid; gap: 12px">
            <div v-for="f in friends" :key="f.id" style="background: #fff; border-radius: 14px; padding: 16px 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px; transition: box-shadow 0.2s, transform 0.2s" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 20px rgba(0,0,0,0.08)';$event.currentTarget.style.transform='translateY(-1px)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 12px rgba(0,0,0,0.04)';$event.currentTarget.style.transform='none'">
              <div style="position: relative">
                <n-avatar round :size="46" :color="f.color" style="boxShadow: f.online ? '0 0 0 2px #22C55E' : 'none'">{{ f.name[0] }}</n-avatar>
                <div v-if="f.online" style="position: absolute; bottom: 0; right: 0; width: 12px; height: 12px; border-radius: 50%; background: #22C55E; border: 2px solid #fff" />
              </div>
              <div style="flex: 1; min-width: 0">
                <div style="display: flex; align-items: center; gap: 8px">
                  <span style="font-weight: 600; font-size: 15px; color: #1a1a2e">{{ f.name }}</span>
                  <span v-if="f.remark" style="color: #999; font-size: 12px">({{ f.remark }})</span>
                  <span :style="{ fontSize: '11px', padding: '1px 10px', borderRadius: '8px', background: f.status === 'busy' ? '#fef2f2' : f.status === 'away' ? '#fffbeb' : '#f0fdf4', color: f.status === 'busy' ? '#EF4444' : f.status === 'away' ? '#F59E0B' : '#22C55E', fontWeight: 500 }">
                    {{ f.status === 'busy' ? '忙碌' : f.status === 'away' ? '离开' : '在线' }}
                  </span>
                </div>
                <div style="font-size: 13px; color: #999; margin-top: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
                  {{ f.signature || '这个人很懒，什么都没写' }}
                </div>
              </div>
              <n-button size="small" round type="primary" ghost @click="$router.push('/chat')" style="borderColor: #4F6EF7; color: #4F6EF7">💬 发消息</n-button>
            </div>
          </div>
        </template>

        <!-- 群组列表 -->
        <template v-if="activeTab === 'groups'">
          <div style="font-size: 18px; font-weight: 700; color: #1a1a2e; margin-bottom: 20px">👥 群组 · {{ groups.length }}</div>
          <div style="display: grid; gap: 12px">
            <div v-for="g in groups" :key="g.id" style="background: #fff; border-radius: 14px; padding: 16px 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px; transition: box-shadow 0.2s, transform 0.2s" @mouseenter="$event.currentTarget.style.boxShadow='0 4px 20px rgba(0,0,0,0.08)';$event.currentTarget.style.transform='translateY(-1px)'" @mouseleave="$event.currentTarget.style.boxShadow='0 2px 12px rgba(0,0,0,0.04)';$event.currentTarget.style.transform='none'">
              <n-avatar round :size="46" :style="{ background: 'linear-gradient(135deg, #22C55E, #18A058)' }">{{ g.name[0] }}</n-avatar>
              <div style="flex: 1">
                <div style="font-weight: 600; font-size: 15px; color: #1a1a2e">{{ g.name }}</div>
                <div style="font-size: 13px; color: #999; margin-top: 3px">{{ g.memberCount }} 人 · {{ g.description || '暂无介绍' }}</div>
              </div>
              <n-button size="small" round type="primary" ghost @click="$router.push('/chat')" style="borderColor: #22C55E; color: #22C55E">进入</n-button>
            </div>
          </div>
        </template>

        <!-- 好友请求 -->
        <template v-if="activeTab === 'requests'">
          <div style="font-size: 18px; font-weight: 700; color: #1a1a2e; margin-bottom: 20px">🔔 好友请求</div>
          <div v-if="requests.length === 0" style="text-align: center; padding: 60px; background: #fff; border-radius: 14px">
            <n-empty description="暂无新的好友请求" />
          </div>
          <div v-for="r in requests" :key="r.id" style="background: #fff; border-radius: 14px; padding: 16px 20px; box-shadow: 0 2px 12px rgba(0,0,0,0.04); display: flex; align-items: center; gap: 14px; margin-bottom: 12px">
            <n-avatar round :size="44" :style="{ background: 'linear-gradient(135deg, #4F6EF7, #7C3AED)' }">{{ r.from[0] }}</n-avatar>
            <div style="flex: 1">
              <div style="font-weight: 600; font-size: 15px; color: #1a1a2e">{{ r.from }}</div>
              <div style="font-size: 13px; color: #999; margin-top: 2px">{{ r.message }}</div>
            </div>
            <n-button size="small" round type="primary" style="marginRight: 4px; background: linear-gradient(135deg, #4F6EF7, #7C3AED); border: none">同意</n-button>
            <n-button size="small" round quaternary type="error" style="color: #EF4444">拒绝</n-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import NavHeader from '../../components/layout/NavHeader.vue'

const activeTab = ref('friends')

const tabs = [
  { key: 'friends', label: '好友', icon: '👥' },
  { key: 'groups', label: '群组', icon: '💬' },
  { key: 'requests', label: '好友请求', icon: '🔔', badge: 1 }
]

const friends = ref([
  { id: 1, name: '张三', remark: '张三丰', color: '#4F6EF7', online: true, status: 'online', signature: '前端开发中，有事请留言' },
  { id: 2, name: '李四', remark: '', color: '#EF4444', online: true, status: 'busy', signature: '需求评审中，勿扰' },
  { id: 3, name: '王五', remark: '老王', color: '#F59E0B', online: false, status: 'away', signature: '周末去爬山' }
])

const groups = ref([
  { id: 1, name: '项目团队', memberCount: 8, description: '日常项目沟通' },
  { id: 2, name: '技术交流群', memberCount: 128, description: '技术讨论与分享' }
])

const requests = ref([
  { id: 1, from: '赵六', message: '你好，我是赵六，想加个好友' }
])
</script>
