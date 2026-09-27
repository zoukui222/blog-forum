<template>
  <div class="blog-stats-dashboard">
    <!-- 页面头部 -->
    <div class="dashboard-header">
      <div class="header-content">
        <h1 class="main-title">
          <i class="title-icon">📊</i>
          博客数据统计中心
        </h1>
        <p class="subtitle">实时数据洞察 · 助力内容优化</p>
      </div>
      <div class="update-info">
        最后更新：{{ currentTime }}
      </div>
    </div>

    <!-- 图表网格 -->
    <div class="charts-container">
      <!-- 1. 博客分类统计 - 饼图 -->
      <div class="chart-wrapper">
        <div class="chart-card category-chart">
          <div class="card-header">
            <div class="card-info">
              <i class="card-icon">🏷️</i>
              <h3>博客分类统计</h3>
            </div>
            <div class="card-meta">分类占比</div>
          </div>
          <div class="chart-content" ref="categoryChart"></div>
        </div>
      </div>

      <!-- 2. 博客信息 - 柱状图 -->
      <div class="chart-wrapper">
        <div class="chart-card blog-chart">
          <div class="card-header">
            <div class="card-info">
              <i class="card-icon">📝</i>
              <h3>博客发布趋势</h3>
            </div>
            <div class="card-meta">月度统计</div>
          </div>
          <div class="chart-content" ref="blogChart"></div>
        </div>
      </div>

      <!-- 3. 活动信息 - 折线图 -->
      <div class="chart-wrapper">
        <div class="chart-card activity-chart">
          <div class="card-header">
            <div class="card-info">
              <i class="card-icon">📈</i>
              <h3>用户活跃度</h3>
            </div>
            <div class="card-meta">每日趋势</div>
          </div>
          <div class="chart-content" ref="activityChart"></div>
        </div>
      </div>

      <!-- 4. 报名信息 - 环形图 -->
      <div class="chart-wrapper">
        <div class="chart-card registration-chart">
          <div class="card-header">
            <div class="card-info">
              <i class="card-icon">👥</i>
              <h3>活动报名情况</h3>
            </div>
            <div class="card-meta">状态分布</div>
          </div>
          <div class="chart-content" ref="registrationChart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'BlogStatsDashboard',
  data() {
    return {
      currentTime: '',
      // 静态数据
      staticData: {
        // 博客分类数据
        categories: [
          {name: '技术分享', value: 42, color: '#5470c6'},
          {name: '生活随笔', value: 28, color: '#91cc75'},
          {name: '项目总结', value: 22, color: '#fac858'},
          {name: '学习笔记', value: 15, color: '#ee6666'},
          {name: '其他', value: 8, color: '#73c0de'}
        ],
        // 博客发布趋势
        blogTrend: {
          months: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
          counts: [15, 22, 18, 25, 32, 28, 35, 38, 42, 39, 45, 52]
        },
        // 活跃度数据
        activity: {
          days: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
          views: [1200, 1350, 1100, 1450, 1680, 2200, 1950],
          comments: [80, 95, 70, 110, 130, 180, 150],
          likes: [200, 240, 180, 280, 320, 420, 360]
        },
        // 报名数据
        registrations: [
          {name: '已确认', value: 65, color: '#52c41a'},
          {name: '待确认', value: 28, color: '#faad14'},
          {name: '已取消', value: 12, color: '#ff4d4f'},
          {name: '已过期', value: 5, color: '#d9d9d9'}
        ]
      }
    }
  },
  mounted() {
    this.updateCurrentTime()
    setInterval(this.updateCurrentTime, 60000) // 每分钟更新时间

    this.$nextTick(() => {
      this.initAllCharts()
    })

    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 清理资源
    Object.values(this.charts).forEach(chart => {
      if (chart) chart.dispose()
    })
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    updateCurrentTime() {
      this.currentTime = new Date().toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    initAllCharts() {
      this.initCategoryChart()
      this.initBlogChart()
      this.initActivityChart()
      this.initRegistrationChart()
    },

    // 1. 博客分类饼图
    initCategoryChart() {
      const chart = echarts.init(this.$refs.categoryChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {color: '#333'},
          extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center',
          textStyle: {color: '#666', fontSize: 13},
          itemWidth: 14,
          itemHeight: 14
        },
        series: [{
          name: '博客分类',
          type: 'pie',
          radius: ['35%', '65%'],
          center: ['65%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
            shadowBlur: 4,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              formatter: '{b}\n{c}篇'
            },
            itemStyle: {
              shadowBlur: 8,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.2)'
            }
          },
          labelLine: {
            show: false
          },
          data: this.staticData.categories.map(item => ({
            ...item,
            itemStyle: {color: item.color}
          }))
        }]
      }
      chart.setOption(option)
      this.charts = {...this.charts, categoryChart: chart}
    },

    // 2. 博客信息柱状图
    initBlogChart() {
      const chart = echarts.init(this.$refs.blogChart)
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {type: 'shadow'},
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {color: '#333'},
          extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15)'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.staticData.blogTrend.months,
          axisLine: {lineStyle: {color: '#e8e8e8'}},
          axisLabel: {
            color: '#666',
            rotate: 0,
            interval: 0
          },
          axisTick: {alignWithLabel: true}
        },
        yAxis: {
          type: 'value',
          axisLine: {lineStyle: {color: '#e8e8e8'}},
          axisLabel: {color: '#666'},
          splitLine: {
            lineStyle: {
              color: '#f5f5f5',
              type: 'dashed'
            }
          }
        },
        series: [{
          name: '博客数量',
          type: 'bar',
          barWidth: '55%',
          data: this.staticData.blogTrend.counts,
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              {offset: 0, color: '#40a9ff'},
              {offset: 1, color: '#1890ff'}
            ]),
            borderRadius: [3, 3, 0, 0],
            boxShadow: '0 2px 8px rgba(24, 144, 255, 0.3)'
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: '#1890ff'},
                {offset: 1, color: '#096dd9'}
              ])
            }
          }
        }]
      }
      chart.setOption(option)
      this.charts.blogChart = chart
    },

    // 3. 活动信息折线图
    initActivityChart() {
      const chart = echarts.init(this.$refs.activityChart)
      const option = {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {color: '#333'},
          extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15)',
          axisPointer: {
            type: 'cross',
            crossStyle: {color: '#ccc'}
          }
        },
        legend: {
          data: ['浏览量', '评论数', '点赞数'],
          top: '8%',
          textStyle: {color: '#666', fontSize: 12}
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.staticData.activity.days,
          axisLine: {lineStyle: {color: '#e8e8e8'}},
          axisLabel: {color: '#666'}
        },
        yAxis: {
          type: 'value',
          axisLine: {lineStyle: {color: '#e8e8e8'}},
          axisLabel: {color: '#666'},
          splitLine: {lineStyle: {color: '#f5f5f5'}}
        },
        series: [
          {
            name: '浏览量',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 5,
            lineStyle: {color: '#ff7875', width: 3},
            itemStyle: {
              color: '#ff7875',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: 'rgba(255, 120, 117, 0.2)'},
                {offset: 1, color: 'rgba(255, 120, 117, 0.05)'}
              ])
            },
            data: this.staticData.activity.views
          },
          {
            name: '评论数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 5,
            lineStyle: {color: '#b7eb8f', width: 3},
            itemStyle: {
              color: '#b7eb8f',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: 'rgba(183, 235, 143, 0.2)'},
                {offset: 1, color: 'rgba(183, 235, 143, 0.05)'}
              ])
            },
            data: this.staticData.activity.comments
          },
          {
            name: '点赞数',
            type: 'line',
            smooth: true,
            symbol: 'circle',
            symbolSize: 5,
            lineStyle: {color: '#722ed1', width: 3},
            itemStyle: {
              color: '#722ed1',
              borderColor: '#fff',
              borderWidth: 2
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                {offset: 0, color: 'rgba(114, 46, 209, 0.2)'},
                {offset: 1, color: 'rgba(114, 46, 209, 0.05)'}
              ])
            },
            data: this.staticData.activity.likes
          }
        ]
      }
      chart.setOption(option)
      this.charts.activityChart = chart
    },

    // 4. 报名信息环形图
    initRegistrationChart() {
      const chart = echarts.init(this.$refs.registrationChart)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.98)',
          borderColor: '#eee',
          borderWidth: 1,
          textStyle: {color: '#333'},
          extraCssText: 'box-shadow: 0 4px 12px rgba(0,0,0,0.15)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          top: 'center',
          textStyle: {color: '#666', fontSize: 13},
          itemWidth: 14,
          itemHeight: 14
        },
        series: [{
          name: '报名状态',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['65%', '50%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 2,
            shadowBlur: 4,
            shadowColor: 'rgba(0, 0, 0, 0.1)'
          },
          label: {
            show: false
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold',
              formatter: '{b}\n{c}人'
            },
            itemStyle: {
              shadowBlur: 8,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.2)'
            }
          },
          labelLine: {
            show: false
          },
          data: this.staticData.registrations.map(item => ({
            ...item,
            itemStyle: {color: item.color}
          }))
        }]
      }
      chart.setOption(option)
      this.charts.registrationChart = chart
    },

    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart) chart.resize()
      })
    }
  }
}
</script>

<style scoped>
.blog-stats-dashboard {
  height: calc(100vh - 60px);
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  overflow: auto;
}

/* 页面头部 */
.dashboard-header {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
}

.header-content {
  margin-bottom: 10px;
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #2c3e50;
}

.title-icon {
  font-size: 32px;
}

.subtitle {
  font-size: 16px;
  margin: 0;
  opacity: 0.8;
  font-weight: 400;
}

.update-info {
  font-size: 14px;
  color: #7f8c8d;
  background: rgba(255, 255, 255, 0.8);
  padding: 8px 16px;
  border-radius: 20px;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 图表容器 */
.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(520px, 1fr));
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.chart-wrapper {
  height: fit-content;
}

.chart-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.chart-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.15);
}

.card-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid #f8f9fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.card-icon {
  font-size: 20px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #2c3e50;
}

.card-meta {
  font-size: 12px;
  color: #95a5a6;
  background: #f8f9fa;
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.chart-content {
  height: 350px;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: repeat(auto-fit, minmax(480px, 1fr));
    gap: 20px;
  }
}

@media (max-width: 768px) {
  .blog-stats-dashboard {
    padding: 15px;
  }

  .charts-container {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .chart-content {
    height: 300px;
    padding: 15px;
  }

  .main-title {
    font-size: 24px;
    flex-direction: column;
    gap: 8px;
  }

  .card-header {
    padding: 16px 20px 12px;
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .update-info {
    align-self: flex-end;
  }
}

@media (max-width: 480px) {
  .chart-content {
    height: 250px;
    padding: 10px;
  }

  .card-header h3 {
    font-size: 16px;
  }

  .main-title {
    font-size: 20px;
  }
}

/* 滚动条美化 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
