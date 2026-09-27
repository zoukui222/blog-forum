<template>
  <div class="main-content">
    <div style="display: flex; align-items: flex-start; grid-gap: 10px">

      <el-card style="width: 150px" class="card">
        <div class="category-item" :class="{ 'category-item-active': item.name === current }"
             v-for="item in categoryList" :key="item.id" @click="selectCategory(item.name)">{{ item.name }}
        </div>
      </el-card>

      <el-card style="flex: 1;">
        <BlogList :categoryName="current"></BlogList>
      </el-card>

      <div style="width: 260px">
        <el-card class="card" style="margin-bottom: 10px">
          <div style="font-size: 20px; font-weight: bold; margin-bottom: 10px">欢迎您, {{ user.name }}！😊</div>
          <a @click.stop="$router.push({ path: '/front/person' })">
            <div style="color: #666">写下博客记录美好的一天</div>
          </a>
        </el-card>

        <el-card class="card" style="margin-bottom: 10px">
          <div style="display: flex; align-items: center; padding-bottom: 10px; border-bottom: 1px solid #ddd">
            <div style="font-size: 20px; flex: 1">消息通知</div>
            <div style="font-size: 12px; color: #666; cursor: pointer;" @click="loadNotice"><i
                class="el-icon-refresh"></i> 刷新
            </div>
          </div>
          <div>
            <div v-for="item in noticeList" :key="item.id" style="margin: 15px 0;cursor: pointer" class="line1">
              <a @click.stop="handleShowNotice(item)">
                <span style="color: #666;">{{ item.title }}</span>
              </a>
            </div>
          </div>
        </el-card>

        <el-card class="card" style="margin-bottom: 10px">
          <div style="display: flex; align-items: center; padding-bottom: 10px; border-bottom: 1px solid #ddd">
            <div style="font-size: 20px; flex: 1">文章榜单</div>
            <div style="font-size: 12px; color: #666; cursor: pointer;" @click="refreshTop"><i
                class="el-icon-refresh"></i> 换一换
            </div>
          </div>
          <div>
            <div v-for="item in showList" :key="item.id" style="margin: 15px 0;cursor: pointer" class="line1">
              <a @click.stop="$router.push({path: '/front/blogDetail', query: {blogId: item.id}})">
                 <span style="width: 18px; display: inline-block; text-align: right; margin-right: 10px">
                   <span style="color: orangered" v-if="item.index === 1">{{ item.index }}</span>
                   <span style="color: goldenrod" v-else-if="item.index === 2">{{ item.index }}</span>
                   <span style="color: dodgerblue" v-else-if="item.index === 3">{{ item.index }}</span>
                   <span style="color: #666" v-else>{{ item.index }}</span>
                 </span>
                <span style="color: #666;">{{ item.title }}</span>
              </a>
            </div>
          </div>
        </el-card>

        <div style="margin-bottom: 10px">
          <div v-for="item in topActivityList" :key="item.id" style="margin-bottom: 10px">
            <a @click.stop="$router.push({ path: '/front/activityDetail', query: {activityId: item.id} })">
              <img :src="item.cover" alt=""
               style="width: 100%; border-radius: 5px"></a>
          </div>
        </div>

        <el-dialog
            v-if="currentNotice"
            :title="currentNotice.title"
            :visible.sync="showNotice"
            width="30%"
            center>
          <span>{{ currentNotice.content }}</span>
          <span slot="footer" class="dialog-footer">
    <el-button type="primary" @click="showNotice = false">确 定</el-button>
  </span>
        </el-dialog>

      </div>
    </div>
  </div>
</template>

<script>
import Footer from "@/components/Footer.vue";
import BlogList from "@/components/BlogList.vue";

export default {
  components: {
    Footer,
    BlogList
  },
  data() {
    return {
      current: '全部博客',  //当前选中的分类名称
      categoryList: [],
      tableData: [],  // 所有的数据
      pageNum: 1,   // 当前的页码
      pageSize: 10,  // 每页显示的个数
      total: 0,
      showNotice:false,
      topList: [],
      showList: [],
      lastIndex: 0,
      noticeList: [],
      currentNotice: null,
      topActivityList: [],
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
    }
  },
  mounted() {
    this.load()

    this.loadBlogs(1)

    this.refreshTop()

    this.loadTopActivity()

    this.loadNotice()

  },
  // methods：本页面所有的点击事件或者其他函数定义区
  methods: {
    handleShowNotice(item) {
      this.currentNotice = item
      this.showNotice = true
    },
    loadTopActivity() {
      this.$request.get('/activity/selectTop').then(res => {
        this.topActivityList = res.data || []
      })
    },
    loadNotice() {
      this.$request.get('/notice/selectAll').then(res => {
        this.noticeList = res.data || []
      })
    },
    refreshTop() {
      this.$request.get('/blog/selectTop').then(res => {
        this.topList = res.data || []
        let i = 1
        this.topList.forEach(item => item.index = i++)
        // 0  5  0
        if (this.lastIndex === 20) {
          this.lastIndex = 0
        }
        this.showList = this.topList.slice(this.lastIndex, this.lastIndex + 5)  // 0-5   5- 10  // 0-5
        this.lastIndex += 5  // 5  10  5
      })
    },
    selectCategory(categoryName) {
      this.current = categoryName
      this.loadBlogs(1)
    },
    load() {
      // 请求分类的数据
      this.$request.get('/category/selectAll').then(res => {
        this.categoryList = res.data || []
        this.categoryList.unshift({name: '全部博客'})
      })
    },
    loadBlogs(pageNum) {
      if (pageNum) this.pageNum = pageNum
      this.$request.get('/blog/selectPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          categoryName: this.current === '全部博客' ? null : this.current,
        }
      }).then(res => {
        this.tableData = res.data?.list
        this.total = res.data?.total
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    },
  }
}
</script>

<style>
.category-item {
  text-align: center;
  padding: 10px 0;
  font-size: 16px;
  cursor: pointer;
}

.category-item-active {
  background-color: #1890ff;
  color: #fff;
  border-radius: 5px;
}

.line1 {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.blog-box {
  display: flex;
  grid-gap: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #ddd;
}

.blog-box:first-child {
  padding-top: 0;
}

.blog-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
  cursor: pointer;
}

.blog-title:hover {
  color: #2a60c9;
}

a {
  color: #333333;
}
</style>
