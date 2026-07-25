<template>
  <div style="height: 100vh; display: flex; flex-direction: column; background: #fff">
    <NavHeader />
    <div style="flex: 1; display: flex; overflow: hidden">
      <!-- 左侧导航 -->
      <div style="width: 280px; border-right: 1px solid #e8e8e8; background: #fafafa; flex-shrink: 0; display: flex; flex-direction: column">
        <div style="padding: 16px 12px 8px">
          <n-input placeholder="搜索好友或群组..." round clearable size="small">
            <template #prefix><n-icon><search-outline /></n-icon></template>
          </n-input>
        </div>
        <div style="flex: 1; overflow-y: auto; padding: 8px 0">
          <div
            v-for="tab in tabs" :key="tab.key"
            @click="activeTab = tab.key"
            :style="{
              padding: '12px 20px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '10px',
              background: activeTab === tab.key ? '#e8f0fe' : 'transparent',
              fontWeight: activeTab === tab.key ? 600 : 400, color: activeTab === tab.key ? '#2080f0' : '#333',
              borderLeft: activeTab === tab.key ? '3px solid #2080f0' : '3px solid transparent'
            }"
          >
            <n-icon size="18">{{ tab.icon }}</n-icon>
            <span>{{ tab.label }}</span>
            <span v-if="tab.badge" style="marginLeft: 'auto'; background: '#d03050'; color: '#fff'; borderRadius: '10px'; padding: '0 8px'; fontSize: '11px'">{{ tab.badge }}</span>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div style="flex: 1; padding: 24px; overflow-y: auto; background: #f5f7fa">
        <!-- 好友列表 -->
        <template v-if="activeTab === 'friends'">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px">好友 · {{ friends.length }}</div>
          <div v-for="f in friends" :key="f.id" style="background: #fff; borderRadius: 12px; padding: 16px; margin-bottom: 12px; boxShadow: '0 1px 3px rgba(0,0,0,0.04)'">
            <div style="display: flex; align-items: center; gap: 12px">
              <div style="position: relative">
                <n-avatar round :size="44" :color="f.color">{{ f.name[0] }}</n-avatar>
                <div v-if="f.online" style="position: absolute; bottom: 0; right: 0; width: 12px; height: 12px; borderRadius: 50%; background: #31c451; border: 2px solid #fff" />
              </div>
              <div style="flex: 1; min-width: 0">
                <div style="display: flex; align-items: center; gap: 6px">
                  <span style="font-weight: 600; font-size: 15px; color: #333">{{ f.name }}</span>
                  <span v-if="f.remark" style="color: #999; font-size: 12px">({{ f.remark }})</span>
                  <span :style="{ fontSize: '11px', padding: '1px 8px', borderRadius: '8px', background: f.status === 'busy' ? '#fff0f0' : f.status === 'away' ? '#fff8e8' : '#e8f8e8', color: f.status === 'busy' ? '#d03050' : f.status === 'away' ? '#f0a020' : '#18a058' }">
                    {{ f.status === 'busy' ? '忙碌' : f.status === 'away' ? '离开' : '在线' }}
                  </span>
                </div>
                <div style="font-size: 13px; color: #888; margin-top: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap">
                  {{ f.signature || '这个人很懒，什么都没写' }}
                </div>
              </div>
              <n-button size="tiny" quaternary @click="$router.push('/chat')">发消息</n-button>
            </div>
          </div>
        </template>

        <!-- 群组列表 -->
        <template v-if="activeTab === 'groups'">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px">群组 · {{ groups.length }}</div>
          <div v-for="g in groups" :key="g.id" style="background: #fff; borderRadius: 12px; padding: 16px; margin-bottom: 12px; boxShadow: '0 1px 3px rgba(0,0,0,0.04)'">
            <div style="display: flex; align-items: center; gap: 12px">
              <n-avatar round :size="44" :color="g.color" style="background: #18a058">{{ g.name[0] }}</n-avatar>
              <div style="flex: 1">
                <div style="font-weight: 600; font-size: 15px; color: #333">{{ g.name }}</div>
                <div style="font-size: 13px; color: #888; margin-top: 2px">{{ g.memberCount }} 人 · {{ g.description || '暂无介绍' }}</div>
              </div>
              <n-button size="tiny" quaternary @click="$router.push('/chat')">进入</n-button>
            </div>
          </div>
        </template>

        <!-- 好友请求 -->
        <template v-if="activeTab === 'requests'">
          <div style="font-size: 16px; font-weight: 600; margin-bottom: 16px">好友请求</div>
          <div v-if="requests.length === 0" style="text-align: center; padding: 40px; color: #bbb">
            <n-empty description="暂无新的好友请求" />
          </div>
          <div v-for="r in requests" :key="r.id" style="background: #fff; borderRadius: 12px; padding: 16px; margin-bottom: 12px; boxShadow: '0 1px 3px rgba(0,0,0,0.04)'">
            <div style="display: flex; align-items: center; gap: 12px">
              <n-avatar round :size="44">{{ r.from[0] }}</n-avatar>
              <div style="flex: 1">
                <div style="font-weight: 600; font-size: 15px; color: #333">{{ r.from }}</div>
                <div style="font-size: 13px; color: #888; margin-top: 2px">{{ r.message }}</div>
              </div>
              <n-button size="tiny" type="primary" style="marginRight: 4px">同意</n-button>
              <n-button size="tiny" quaternary type="error">拒绝</n-button>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { NIcon } from 'naive-ui'
import { PeopleOutline, ChatbubbleEllipsesOutline, PersonAddOutline, SearchOutline } from '@vicons/ionicons5'
import NavHeader from '../../components/layout/NavHeader.vue'

const activeTab = ref('friends')

const tabs = [
  { key: 'friends', label: '好友', icon: PeopleOutline },
  { key: 'groups', label: '群组', icon: ChatbubbleEllipsesOutline },
  { key: 'requests', label: '好友请求', icon: PersonAddOutline, badge: 1 }
]

const friends = ref([
  { id: 1, name: '张三', remark: '张三丰', color: '#2080f0', online: true, status: 'online', signature: '前端开发中，有事请留言' },
  { id: 2, name: '李四', remark: '', color: '#d03050', online: true, status: 'busy', signature: '需求评审中，勿扰' },
  { id: 3, name: '王五', remark: '老王', color: '#f0a020', online: false, status: 'away', signature: '周末去爬山' }
])

const groups = ref([
  { id: 1, name: '项目团队', color: '#18a058', memberCount: 8, description: '日常项目沟通' },
  { id: 2, name: '技术交流群', color: '#2080f0', memberCount: 128, description: '技术讨论与分享' }
])

const requests = ref([
  { id: 1, from: '赵六', message: '你好，我是赵六，想加个好友' }
])
</script>
