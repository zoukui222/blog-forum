<template>
  <div class="ai-assistant">
    <!-- 对话面板 -->
    <transition name="ai-pop">
      <div v-show="visible" class="ai-panel">
        <div class="ai-panel-header">
          <div class="ai-panel-title">
            <i class="el-icon-chat-dot-round"></i>
            <span>博客智能助手</span>
          </div>
          <div class="ai-panel-sub">{{ statusText }}</div>
          <i class="el-icon-close ai-panel-close" @click="visible = false"></i>
        </div>

        <div ref="body" class="ai-panel-body">
          <div v-if="messages.length === 0" class="ai-empty">
            <div class="ai-empty-title">可以这样问我</div>
            <div class="ai-samples">
              <span v-for="s in samples" :key="s" class="ai-sample" @click="ask(s)">{{ s }}</span>
            </div>
            <div class="ai-empty-tip" v-if="!hasToken">
              <i class="el-icon-warning-outline"></i> 当前未登录，请先登录后使用
            </div>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="ai-msg" :class="'ai-msg--' + m.role">
            <div class="ai-bubble">
              <div v-for="(t, ti) in m.tools" :key="ti" class="ai-tool" :class="{ 'ai-tool--fail': !t.running && !t.success }">
                <i class="el-icon-loading" v-if="t.running"></i>
                <i class="el-icon-circle-check" v-else-if="t.success"></i>
                <i class="el-icon-warning-outline" v-else></i>
                <span>{{ t.label }}</span>
              </div>
              <div class="ai-text" v-html="render(m.text)"></div>
              <span v-if="m.streaming && m.text" class="ai-cursor"></span>
            </div>
            <div v-if="m.meta" class="ai-meta">
              {{ m.meta.iterations }} 次工具调用 · {{ m.meta.durationMs }}ms ·
              {{ m.meta.promptTokens + m.meta.completionTokens }} tokens
            </div>
          </div>
        </div>

        <div class="ai-panel-footer">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="问站内文章、统计数据，或让我创建文章草稿…"
            @keydown.enter.native.exact.prevent="ask()"
          />
          <div class="ai-actions">
            <span class="ai-hint">Enter 发送 · Shift+Enter 换行</span>
            <el-button type="primary" size="small" :loading="streaming" @click="ask()">发送</el-button>
          </div>
        </div>
      </div>
    </transition>

    <!-- 悬浮按钮：面板展开时隐藏，避免与标题栏的关闭按钮形成两个关闭入口 -->
    <div v-show="!visible" class="ai-fab" @click="toggle">
      <i class="el-icon-chat-dot-round"></i>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AiAssistant',
  data () {
    return {
      visible: false,
      input: '',
      messages: [],
      streaming: false,
      samples: [
        '站内有哪些关于 Vue 的文章？',
        '这个站一共多少篇文章、总阅读量多少？',
        '哪个分类的文章最多？'
      ]
    }
  },
  computed: {
    hasToken () {
      return !!this.getToken()
    },
    statusText () {
      if (this.streaming) return '正在思考…'
      return 'DeepSeek · 可查站内文章与统计'
    }
  },
  methods: {
    getToken () {
      try {
        return (JSON.parse(localStorage.getItem('xm-user') || '{}') || {}).token || ''
      } catch (e) {
        return ''
      }
    },
    toggle () {
      this.visible = !this.visible
    },
    ask (text) {
      this.send(text)
    },
    async send (text) {
      const question = (typeof text === 'string' ? text : this.input).trim()
      if (!question || this.streaming) return
      const token = this.getToken()
      if (!token) {
        this.$message.warning('请先登录后再使用智能助手')
        return
      }

      this.input = ''
      this.messages.push({ role: 'user', text: question, tools: [] })
      const aiMsg = { role: 'ai', text: '', tools: [], streaming: true, meta: null }
      this.messages.push(aiMsg)
      this.streaming = true
      this.scrollToBottom()

      // 用 fetch 手动读取流：原生 EventSource 只能发 GET 且不支持自定义请求头，
      // 而本项目的鉴权 token 是放在 header 里的，只能自己解析 SSE 报文。
      const url = process.env.VUE_APP_BASEURL + '/ai/chat?message=' + encodeURIComponent(question)

      try {
        const resp = await fetch(url, { headers: { token } })
        if (!resp.ok) throw new Error('HTTP ' + resp.status)
        if (!resp.body) throw new Error('当前浏览器不支持流式响应')

        const reader = resp.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let buffer = ''

        while (true) {
          const { done, value } = await reader.read()
          if (done) break
          buffer += decoder.decode(value, { stream: true })
          buffer = buffer.replace(/\r\n/g, '\n')
          let sep
          while ((sep = buffer.indexOf('\n\n')) >= 0) {
            const block = buffer.slice(0, sep)
            buffer = buffer.slice(sep + 2)
            this.handleEvent(block, aiMsg)
          }
        }
      } catch (e) {
        aiMsg.text += (aiMsg.text ? '\n\n' : '') + '（请求失败：' + e.message + '）'
      } finally {
        aiMsg.streaming = false
        this.streaming = false
        this.scrollToBottom()
      }
    },
    handleEvent (block, aiMsg) {
      const lines = block.split('\n')
      let name = ''
      const dataLines = []
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i]
        if (line.indexOf('event:') === 0) name = line.slice(6).trim()
        else if (line.indexOf('data:') === 0) dataLines.push(line.slice(5).trim())
      }
      if (!dataLines.length) return

      let payload
      try {
        payload = JSON.parse(dataLines.join('\n'))
      } catch (e) {
        return
      }

      if (name === 'tool_call') {
        aiMsg.tools.push({
          name: payload.name,
          label: this.runningLabel(payload.name),
          running: true,
          success: true
        })
        this.scrollToBottom()
      } else if (name === 'tool_result') {
        const pending = aiMsg.tools.filter(t => t.running)
        const target = pending.length ? pending[pending.length - 1] : null
        if (target) {
          target.running = false
          target.success = !!payload.success
          target.label = (payload.success ? '已' : '失败：') + this.doneLabel(payload.name) +
            '（' + payload.durationMs + 'ms）'
        }
        this.scrollToBottom()
      } else if (name === 'content') {
        aiMsg.text += payload.text || ''
        this.scrollToBottom()
      } else if (name === 'error') {
        aiMsg.text += (aiMsg.text ? '\n\n' : '') + '（' + (payload.message || '服务异常') + '）'
      } else if (name === 'done') {
        aiMsg.meta = payload
      }
    },
    runningLabel (tool) {
      const map = {
        search_blogs: '正在检索站内文章…',
        get_blog_detail: '正在读取文章正文…',
        get_blog_stats: '正在统计站点数据…',
        create_blog_draft: '正在创建文章草稿…'
      }
      return map[tool] || ('正在调用 ' + tool + '…')
    },
    doneLabel (tool) {
      const map = {
        search_blogs: '检索站内文章',
        get_blog_detail: '读取文章正文',
        get_blog_stats: '统计站点数据',
        create_blog_draft: '创建文章草稿'
      }
      return map[tool] || ('调用 ' + tool)
    },
    // 模型输出是 Markdown 片段，这里只做最小渲染；先转义再替换，避免 v-html 注入
    render (text) {
      if (!text) return ''
      const escaped = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
      return escaped
        .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
    },
    scrollToBottom () {
      this.$nextTick(() => {
        const el = this.$refs.body
        if (el) el.scrollTop = el.scrollHeight
      })
    }
  }
}
</script>

<style scoped>
.ai-assistant {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 3000;
}
.ai-fab {
  width: 52px;
  height: 52px;
  margin-left: auto;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #2b7de9);
  color: #fff;
  font-size: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(64, 158, 255, 0.42);
  transition: transform 0.2s;
  user-select: none;
}
.ai-fab:hover {
  transform: scale(1.06);
}
.ai-panel {
  position: absolute;
  right: 0;
  bottom: 66px;
  width: 384px;
  height: 544px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.ai-panel-header {
  position: relative;
  padding: 14px 16px 12px;
  background: linear-gradient(135deg, #409eff, #2b7de9);
  color: #fff;
}
.ai-panel-title {
  font-size: 15px;
  font-weight: 600;
}
.ai-panel-title i {
  margin-right: 6px;
}
.ai-panel-sub {
  margin-top: 3px;
  font-size: 12px;
  opacity: 0.85;
}
.ai-panel-close {
  position: absolute;
  right: 14px;
  top: 16px;
  cursor: pointer;
  font-size: 16px;
  opacity: 0.9;
}
.ai-panel-close:hover {
  opacity: 1;
}
.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 14px;
  background: #f5f7fa;
}
.ai-empty {
  padding: 10px 4px;
}
.ai-empty-title {
  font-size: 13px;
  color: #909399;
  margin-bottom: 10px;
}
.ai-samples {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.ai-sample {
  padding: 8px 12px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.18s;
}
.ai-sample:hover {
  border-color: #409eff;
  color: #409eff;
}
.ai-empty-tip {
  margin-top: 16px;
  font-size: 12px;
  color: #e6a23c;
}
.ai-msg {
  margin-bottom: 14px;
  display: flex;
  flex-direction: column;
}
.ai-msg--user {
  align-items: flex-end;
}
.ai-msg--ai {
  align-items: flex-start;
}
.ai-bubble {
  max-width: 92%;
  padding: 9px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.65;
  word-break: break-word;
  white-space: normal;
}
.ai-msg--user .ai-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 2px;
}
.ai-msg--ai .ai-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e8eaed;
  border-bottom-left-radius: 2px;
}
.ai-tool {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 6px;
  padding: 3px 8px;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 4px;
}
.ai-tool--fail {
  color: #f56c6c;
  background: #fef0f0;
}
.ai-meta {
  margin-top: 5px;
  font-size: 11px;
  color: #b0b3b8;
}
.ai-cursor {
  display: inline-block;
  width: 7px;
  height: 13px;
  margin-left: 2px;
  vertical-align: -2px;
  background: #409eff;
  animation: ai-blink 1s steps(2, start) infinite;
}
@keyframes ai-blink {
  to { visibility: hidden; }
}
.ai-panel-footer {
  padding: 10px 12px 12px;
  border-top: 1px solid #ebeef5;
  background: #fff;
}
.ai-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 8px;
}
.ai-hint {
  font-size: 11px;
  color: #c0c4cc;
}
.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: opacity 0.18s, transform 0.18s;
}
.ai-pop-enter,
.ai-pop-leave-to {
  opacity: 0;
  transform: translateY(12px);
}
@media (max-width: 500px) {
  .ai-assistant {
    right: 14px;
    bottom: 14px;
  }
  .ai-panel {
    width: calc(100vw - 28px);
    height: 60vh;
  }
}
</style>
