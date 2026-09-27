<template>
  <div class="detail-container">
    <van-nav-bar
        fixed
        title="发布文章"
        left-text="返回"
        @click-left="$router.back()"
    />
    <new-blog class="blog" />
  </div>
</template>

<script>
import NewBlog from "@/views/front/NewBlog";
export default {
  name: "publish",
  components: {
    NewBlog
  },
}
</script>

<style scoped>
.detail-container {
  padding: 50px 10px;
  overflow-y: auto;
  height: calc(100vh - 50px);
}
.blog {
  width: 100% !important;
}
/deep/.el-card__body {
  padding: 0 !important;

}
/deep/.el-form {
  padding: 0 !important;
}
/deep/.el-card {
  box-shadow: none !important;
  border: none !important;
}
/deep/.el-form-item__label {
  /*width: 40px !important;*/
  width: 100% !important;
  text-align: left;
}
/deep/.el-form-item__content {
  margin-left: 0 !important;
}
</style>
