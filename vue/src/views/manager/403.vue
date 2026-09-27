<template>
  <div>
    <div style="height: calc(100vh - 80px); overflow: hidden; display: flex; align-items: center; justify-content: center">
      <div style="font-size: 40px">403 无访问权限
        <a v-if="user.role==='USER'"  @click.stop="navTo('/front/home')">返回首页</a>
        <a v-else @click.stop="navTo('/home')">返回首页</a>
      </div>
    </div>

  </div>
</template>

<script>
export default {
  name: "403",
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    }
  },
  created() {

  },
  methods: {
    navTo(url) {
      this.$router.push(url)
    },
  }
}
</script>

<style scoped>
a {
  color: blue;
}

a:hover {
  color: red;
}
</style>
