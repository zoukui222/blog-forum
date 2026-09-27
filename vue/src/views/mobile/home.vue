<template>
 <div class="home-container">
   <van-nav-bar
       title="首页"
   />
   <van-tabs title-active-color="#0066bc" v-model="active" sticky @change="handleChange">
     <van-tab v-for="item in categoryList" :title="item.name" :key="item.id" >
       <div>
         <MBlogList :categoryName="current" />
       </div>
     </van-tab>
   </van-tabs>

   <van-button round class="publish-btn" icon="plus" type="info" @click="$router.push('/mobile/publish')">发布博客</van-button>

 </div>
</template>

<script>
import MBlogList from "./mBlogList";

export default {
  name: "home",
  components: {
    MBlogList
  },
  data() {
    return {
      current: '全部博客',
      active: 0,
      categoryList: []
    }
  },
  mounted() {
    this.load()
  },
  methods: {
    handleChange(id,name) {
      this.current = name
    },
    load() {
      // 请求分类的数据
      this.$request.get('/category/selectAll').then(res => {
        this.categoryList = res.data || []
        this.categoryList.unshift({name: '全部博客',id: 0})
      })
    },
  }
}
</script>

<style scoped>
.home-container {
  height: calc(100vh - 50px);
  overflow:auto;
}
/deep/.is-always-shadow {
  height: 100% !important;
  overflow: auto;
}
.publish-btn {
  position: fixed;
  /*border-radius: 50%;*/
  bottom: 15%;
  right: 8%;
}

</style>
