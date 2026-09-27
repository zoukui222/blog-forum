<template>
  <div class="myBlog-container">
    <van-nav-bar
        fixed
        left-arrow
        title="我发表的博客"
        @click-left="$router.back()"
    />
    <m-blog-list type="user" :show-opt="true"/>
  </div>
</template>

<script>
import mBlogList from './mBlogList'
export default {
  name: "myBlog",
  components: {
    mBlogList
  },
}
</script>

<style scoped>
.myBlog-container {
  height: calc(100vh - 50px);
  padding-top: 50px !important;
  overflow:auto;
}
/deep/.el-card {
  box-shadow: none !important;
  border: none !important;
  border-radius: 0 !important;
  height: unset !important;
}
</style>
