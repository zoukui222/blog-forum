<template>
  <div class="myActivity-container">
    <van-nav-bar
        fixed
        left-arrow
        title="我报名的活动"
        @click-left="$router.back()"
    />
    <m-activity-list type="user" :span="24"/>
  </div>
</template>

<script>
import MActivityList from "@/views/mobile/mActivityList";
export default {
  name: "myActivity",
  components: {
    MActivityList
  },
}
</script>

<style scoped>
.myActivity-container {
  padding-top: 50px;
  height: calc(100vh - 50px);
  overflow-y: auto;
}
</style>
