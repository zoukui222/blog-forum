<template>
 <div class="person-detain">
   <van-nav-bar
       title="个人中心"
       fixed
   />
   <person-page />
   <van-cell title="我发表的博客" @click="$router.push('/mobile/myBlog')" is-link />
   <van-cell title="我报名的活动" @click="$router.push('/mobile/myActivity')" is-link />
   <van-cell title="我的点赞" is-link @click="$router.push('/mobile/myLike')"  />
   <van-cell title="我的收藏" is-link @click="$router.push('/mobile/myCollect')"  />
   <van-cell title="我的评论" is-link @click="$router.push('/mobile/myComment')"  />

   <div style="width: 100%;display: flex;justify-content: center;margin: 10px 0">
     <van-button round style="width: 100%; margin: 10px" type="info" @click="logout">退出登录</van-button>
   </div>
 </div>
</template>

<script>
import personPage from "@/components/PersonPage";
export default {
  name: "person",
  components: {
    personPage
  },
  methods: {
    // 退出登录
    logout() {
      localStorage.removeItem("xm-user");
      this.$router.push("/login");
    },
  }
}
</script>

<style scoped>
.person-detain {
  height: calc(100vh - 50px);
  padding: 50px 10px;
  overflow-y: auto;
}
/deep/.el-card {
  box-shadow: none !important;
  border: none !important;
}

/deep/.el-dialog {
  width: 80% !important;
}

</style>
