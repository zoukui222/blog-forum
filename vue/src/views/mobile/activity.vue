<template>
 <div class="activity-container">
   <van-nav-bar
       title="活动中心"
   />
   <activity-list :span="24" />
 </div>
</template>

<script>
import ActivityList from './mActivityList'
export default {
  name: "activity",
  components: {
    ActivityList
  },
}
</script>

<style scoped>
.activity-container {
  height: calc(100vh - 50px);
  overflow:auto;
}
/deep/.el-row {
  margin: unset !important;
  padding: 10px 0;
}
</style>
