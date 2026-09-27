<template>
 <div class="detail-container">
   <van-nav-bar
       fixed
       title="文章详情"
       left-text="返回"
       @click-left="$router.back()"
   />
   <blog-detail class="blog-detail" />
 </div>
</template>

<script>
import BlogDetail from "@/views/front/BlogDetail";
export default {
  name: "articleDetail",
  components: {
    BlogDetail
  },
}
</script>

<style scoped>
.detail-container {
  width: 100vw;
  height: 100vh;
  padding-top: 46px !important;
  padding-bottom: 50px;
  overflow: auto !important;
  background-color: #ffffff !important;
}
.blog-detail {
  padding: 10px !important;
}
.blog-detail /deep/.el-card__body {
  padding: 0 !important;
  width: 100%;
}
/deep/.el-card {
  padding: 0 !important;
  margin-bottom:  0 !important;
}
/deep/.main-content div {
  /*display: unset !important;*/
}
/deep/.is-always-shadow {
  box-shadow: unset !important;
  border: none !important;
}
</style>
