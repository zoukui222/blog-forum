export default {
  data () {
    return {
      pageSize: 10
    }
  },
  methods: {
    handleSizeChange (val) {
      this.pageSize = val
      this.load(this.pageNum)
    }
  }
}