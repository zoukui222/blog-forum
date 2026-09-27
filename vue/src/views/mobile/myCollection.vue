<template>
  <div class="myLike-container">
    <van-nav-bar
        title="我的收藏"
        fixed
        left-arrow
        @click-left="$router.back()"
    />
    <van-tabs v-model="active">
      <van-tab title="博客">
        <m-blog-list type="collect" span="24" />
      </van-tab>
      <van-tab title="活动">
        <m-activity-list type="collect" span="24" />
      </van-tab>
    </van-tabs>
  </div>
</template>

<script>
import MBlogList from "@/views/mobile/mBlogList";
import MActivityList from "@/views/mobile/mActivityList";
export default {
  name: "myLike",
  components: {MActivityList, MBlogList},
  data() {
    return {
      active: 0
    }
  }
}
</script>

<style scoped>
.myLike-container {
  padding-top: 50px !important;
  height: calc(100vh - 50px);
}
</style>
