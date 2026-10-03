<template>
  <div id="app">
    <router-view/>
    <ai-assistant v-if="showAssistant"/>
  </div>
</template>
<script>
import AiAssistant from '@/components/AiAssistant.vue'

export default {
  name: 'App',
  components: { AiAssistant },
  computed: {
    // 登录/注册/移动端页面不挂载助手
    showAssistant () {
      const path = this.$route.path || ''
      if (path === '/login' || path === '/register') return false
      if (path.indexOf('/mobile') === 0) return false
      return true
    }
  }
}
</script>
