<template>
 <div class="layout">
   <router-view />
   <van-tabbar route>
     <van-tabbar-item replace to="/mobile/home" icon="home-o">首页</van-tabbar-item>
     <van-tabbar-item replace to="/mobile/activity" icon="coupon-o">活动中心</van-tabbar-item>
     <van-tabbar-item replace to="/mobile/person" icon="manager-o">个人中心</van-tabbar-item>
   </van-tabbar>
 </div>
</template>

<script>
export default {
  name: "layout"
}
</script>

<style scoped>
.layout {
  background-color: #ffffff !important;
}
</style>
