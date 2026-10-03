package com.example.ai.tool.impl;

import cn.hutool.http.HtmlUtil;
import com.example.ai.core.AgentContext;
import com.example.ai.mapper.AiQueryMapper;
import com.example.ai.tool.AiTool;
import com.example.entity.Blog;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：读取单篇文章正文
 * 只读，任何登录用户都能用。
 */
@Component
public class BlogDetailTool implements AiTool {

    /** 正文回灌给模型的字符上限：全文可能上万字，必须截断 */
    private static final int CONTENT_LIMIT = 1500;

    @Resource
    private AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "get_blog_detail";
    }

    @Override
    public String description() {
        return "根据文章 ID 读取某一篇博客的正文内容与元信息。"
                + "当用户要求'总结这篇文章''这篇讲了什么''文章里提到的方案是什么'时使用，"
                + "ID 通常来自 search_blogs 的结果。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> props = new LinkedHashMap<String, Object>();

        Map<String, Object> blogId = new LinkedHashMap<String, Object>();
        blogId.put("type", "integer");
        blogId.put("description", "文章 ID");
        props.put("blogId", blogId);

        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", Arrays.asList("blogId"));
        return schema;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        if (!args.hasNonNull("blogId")) {
            return "参数缺失：需要提供 blogId";
        }
        int blogId = args.get("blogId").asInt();

        Blog blog = aiQueryMapper.selectBlogDetail(blogId);
        if (blog == null) {
            return "不存在 ID=" + blogId + " 的文章，请先确认 ID 是否正确（可用 search_blogs 查询）。";
        }

        String plain = HtmlUtil.cleanHtmlTag(blog.getContent() == null ? "" : blog.getContent())
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        StringBuilder sb = new StringBuilder();
        sb.append("标题：").append(blog.getTitle()).append("\n");
        sb.append("作者：").append(blog.getUserName() == null ? "未知" : blog.getUserName()).append("\n");
        sb.append("分类：").append(blog.getCategoryName() == null ? "未分类" : blog.getCategoryName()).append("\n");
        sb.append("发布日期：").append(blog.getDate() == null ? "未知" : blog.getDate()).append("\n");
        sb.append("阅读量：").append(blog.getReadCount() == null ? 0 : blog.getReadCount()).append("\n");
        if (blog.getTags() != null && !blog.getTags().isEmpty()) {
            sb.append("标签：").append(blog.getTags()).append("\n");
        }
        sb.append("正文：\n");
        sb.append(plain.length() <= CONTENT_LIMIT
                ? plain
                : plain.substring(0, CONTENT_LIMIT) + "\n…（正文过长，此处已截断）");
        return sb.toString();
    }
}
