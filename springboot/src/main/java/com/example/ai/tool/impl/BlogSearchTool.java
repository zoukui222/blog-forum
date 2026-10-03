package com.example.ai.tool.impl;

import cn.hutool.http.HtmlUtil;
import com.example.ai.core.AgentContext;
import com.example.ai.dto.ToolInvokeResult;
import com.example.ai.mapper.AiQueryMapper;
import com.example.ai.tool.AiTool;
import com.example.entity.Blog;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 工具：检索博客文章
 * 只读，任何登录用户都能用。
 */
@Component
public class BlogSearchTool implements AiTool {

    @Resource
    private AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "search_blogs";
    }

    @Override
    public String description() {
        return "检索站内博客文章，可按关键词或分类过滤，返回文章 ID、标题、摘要、作者、分类、阅读量。"
                + "当用户询问'有没有关于X的文章''站里写了什么和Y相关的内容'，或需要给出文章列表时使用。"
                + "拿到返回的 id 后，可再用 get_blog_detail 查看某一篇的正文。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> props = new LinkedHashMap<String, Object>();

        Map<String, Object> keyword = new LinkedHashMap<String, Object>();
        keyword.put("type", "string");
        keyword.put("description", "搜索关键词，会模糊匹配标题、摘要、标签。例如 'Vue'、'算法'、'面试'");
        props.put("keyword", keyword);

        Map<String, Object> categoryId = new LinkedHashMap<String, Object>();
        categoryId.put("type", "integer");
        categoryId.put("description", "分类 ID。" + categoryOptions());
        props.put("categoryId", categoryId);

        Map<String, Object> limit = new LinkedHashMap<String, Object>();
        limit.put("type", "integer");
        limit.put("description", "返回条数，范围 1-10，默认 5");
        props.put("limit", limit);

        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", Collections.emptyList());
        return schema;
    }

    /** 分类 ID 会随运营变动，这里从库里实时读出拼进工具说明，避免模型拿着过期的映射猜 */
    private String categoryOptions() {
        try {
            List<Map<String, Object>> categories = aiQueryMapper.selectCategoryList();
            if (categories == null || categories.isEmpty()) {
                return "当前站点暂无分类";
            }
            String options = categories.stream()
                    .map(c -> c.get("id") + "=" + c.get("name"))
                    .collect(Collectors.joining("，"));
            return "可选值：" + options;
        } catch (Exception e) {
            return "分类列表暂不可用";
        }
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        String keyword = args.hasNonNull("keyword") ? args.get("keyword").asText().trim() : null;
        Integer categoryId = args.hasNonNull("categoryId") ? args.get("categoryId").asInt() : null;

        int limit = args.hasNonNull("limit") ? args.get("limit").asInt() : 5;
        if (limit < 1) {
            limit = 1;
        }
        if (limit > 10) {
            limit = 10;
        }
        if (keyword != null && keyword.isEmpty()) {
            keyword = null;
        }

        List<Blog> list = aiQueryMapper.searchBlogs(keyword, categoryId, limit);

        if (list == null || list.isEmpty()) {
            // 空结果也要给出信息量，否则模型只能干巴巴说"没找到"，体验很差
            List<Blog> fallback = aiQueryMapper.searchBlogs(null, null, 3);
            StringBuilder miss = new StringBuilder();
            miss.append("没有检索到匹配");
            if (keyword != null) {
                miss.append("关键词 '").append(keyword).append("'");
            }
            if (categoryId != null) {
                miss.append("分类 ID=").append(categoryId);
            }
            miss.append("的文章。");
            if (fallback != null && !fallback.isEmpty()) {
                miss.append("站内当前文章较少，最近较热的是：\n");
                miss.append(format(fallback));
            }
            return miss.toString();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("检索到 ").append(list.size()).append(" 篇文章：\n");
        sb.append(format(list));
        return sb.toString();
    }

    /** 用紧凑文本而非 JSON 回灌给模型：同样的信息量省下大量 token */
    private String format(List<Blog> list) {
        StringBuilder sb = new StringBuilder();
        for (Blog b : list) {
            sb.append("- id=").append(b.getId())
                    .append(" 《").append(b.getTitle()).append("》");
            if (b.getUserName() != null) {
                sb.append(" 作者:").append(b.getUserName());
            }
            if (b.getCategoryName() != null) {
                sb.append(" 分类:").append(b.getCategoryName());
            }
            sb.append(" 阅读:").append(b.getReadCount() == null ? 0 : b.getReadCount());
            if (b.getDate() != null && !b.getDate().isEmpty()) {
                sb.append(" 发布:").append(b.getDate());
            }
            sb.append("\n");
            String descr = b.getDescr();
            if (descr != null && !descr.isEmpty()) {
                sb.append("  摘要:").append(abbreviate(HtmlUtil.cleanHtmlTag(descr), 80)).append("\n");
            }
            if (b.getTags() != null && !b.getTags().isEmpty()) {
                sb.append("  标签:").append(b.getTags()).append("\n");
            }
        }
        return sb.toString();
    }

    private static String abbreviate(String text, int max) {
        if (text == null) {
            return "";
        }
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= max ? flat : flat.substring(0, max) + "…";
    }
}
