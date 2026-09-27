<template>
  <el-card style="margin: 5px auto; width: 60%">
    <blog-list />
  </el-card>
</template>

<script>
import BlogList from "@/components/BlogList";
export default {
  name: "Search",
  components: {BlogList},
  data() {
    return {
      title: this.$route.query.title
    }
  },
  created() {

  },
  methods: {}
}
</script>

<style scoped>

</style>
