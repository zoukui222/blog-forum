package com.example.ai.tool.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONArray;
import com.example.ai.core.AgentContext;
import com.example.ai.tool.AiTool;
import com.example.entity.Blog;
import com.example.service.BlogService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 工具：创建博客草稿
 *
 * 唯一一个会产生副作用的工具，因此 adminOnly=true：
 * 工具声明层面对普通用户隐藏（模型根本看不到它），执行层再校验一次角色（兜底，防止声明被绕过）。
 * 作者归属显式取 AgentContext.userId —— 不能用 TokenUtils.getCurrentUser()，
 * 那条路依赖请求线程的 ThreadLocal，在 SSE 的异步线程里会拿到空账号，导致文章没有作者。
 */
@Component
public class BlogDraftTool implements AiTool {

    private static final int DESCR_LIMIT = 100;

    @Resource
    private BlogService blogService;

    @Override
    public String name() {
        return "create_blog_draft";
    }

    @Override
    public String description() {
        return "创建一篇新的博客文章（草稿）。当用户明确要求'帮我写一篇文章''把刚才的内容发成博客'时使用。"
                + "需要提供标题和正文，正文支持 HTML。创建后会返回新文章 ID。"
                + "注意：只能由管理员调用，普通用户无权使用该工具。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> props = new LinkedHashMap<String, Object>();

        Map<String, Object> title = new LinkedHashMap<String, Object>();
        title.put("type", "string");
        title.put("description", "文章标题");
        props.put("title", title);

        Map<String, Object> content = new LinkedHashMap<String, Object>();
        content.put("type", "string");
        content.put("description", "文章正文，支持 HTML 标签");
        props.put("content", content);

        Map<String, Object> descr = new LinkedHashMap<String, Object>();
        descr.put("type", "string");
        descr.put("description", "文章摘要，不传则自动截取正文前 100 字");
        props.put("descr", descr);

        Map<String, Object> categoryId = new LinkedHashMap<String, Object>();
        categoryId.put("type", "integer");
        categoryId.put("description", "分类 ID，可不传，不传则归类为空");
        props.put("categoryId", categoryId);

        Map<String, Object> tags = new LinkedHashMap<String, Object>();
        tags.put("type", "string");
        tags.put("description", "标签，多个用逗号分隔，例如 'Java,Spring Boot'");
        props.put("tags", tags);

        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", Arrays.asList("title", "content"));
        return schema;
    }

    @Override
    public boolean adminOnly() {
        return true;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        if (!args.hasNonNull("title") || !args.hasNonNull("content")) {
            return "参数缺失：title 与 content 均为必填";
        }
        String title = args.get("title").asText().trim();
        String content = args.get("content").asText();
        if (title.isEmpty()) {
            return "参数错误：标题不能为空";
        }
        if (HtmlUtil.cleanHtmlTag(content).trim().isEmpty()) {
            return "参数错误：正文不能为空";
        }
        // 防御性校验：即便工具声明已按角色过滤，执行前再拦一道
        if (context == null || !context.isAdmin()) {
            return "拒绝执行：创建文章需要管理员权限";
        }

        Blog blog = new Blog();
        blog.setTitle(title);
        blog.setContent(content);
        blog.setUserId(context.getUserId());
        blog.setDate(DateUtil.today());
        blog.setReadCount(0);

        if (args.hasNonNull("descr")) {
            blog.setDescr(args.get("descr").asText().trim());
        } else {
            String plain = HtmlUtil.cleanHtmlTag(content).replaceAll("\\s+", " ").trim();
            blog.setDescr(plain.length() <= DESCR_LIMIT ? plain : plain.substring(0, DESCR_LIMIT));
        }
        if (args.hasNonNull("categoryId")) {
            blog.setCategoryId(args.get("categoryId").asInt());
        }
        if (args.hasNonNull("tags")) {
            blog.setTags(toJsonArray(args.get("tags").asText()));
        }

        blogService.add(blog);

        return "草稿创建成功：id=" + blog.getId() + "，标题《" + blog.getTitle() + "》，"
                + "作者 " + context.getName() + "，发布日期 " + blog.getDate()
                + "。可以在后台【博客管理】中查看并补充封面后发布。";
    }

    /** 前端的标签字段存的是 JSON 数组字符串（BlogService 里用 JSONUtil.parseArray 解析），这里对齐格式 */
    private String toJsonArray(String raw) {
        JSONArray arr = new JSONArray();
        for (String part : raw.split("[,，]")) {
            String tag = part.trim();
            if (!tag.isEmpty()) {
                arr.add(tag);
            }
        }
        return arr.toString();
    }
}
