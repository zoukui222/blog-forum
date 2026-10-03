package com.example.ai.tool.impl;

import com.example.ai.core.AgentContext;
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

/**
 * 工具：站点数据统计
 * 只读，任何登录用户都能用。用一个工具覆盖"有多少文章""哪个分类最热""总阅读量多少"这类问题，
 * 而不是给每类统计各开一个工具——工具数量越多，模型选错的概率越高。
 */
@Component
public class BlogStatsTool implements AiTool {

    /** 与 comment.module 保持一致：项目里 LikesModuleEnum.BLOG 的值是中文"博客" */
    private static final String COMMENT_MODULE_BLOG = "博客";

    @Resource
    private AiQueryMapper aiQueryMapper;

    @Override
    public String name() {
        return "get_blog_stats";
    }

    @Override
    public String description() {
        return "获取站点整体统计：文章总数、累计阅读量、博客评论数、注册用户数、各分类文章分布、阅读量最高的文章。"
                + "当用户问'这个站有多少文章''哪类内容最多''最火的文章是哪篇'时使用，无需参数。";
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> schema = new LinkedHashMap<String, Object>();
        schema.put("type", "object");
        schema.put("properties", new LinkedHashMap<String, Object>());
        schema.put("required", Collections.emptyList());
        return schema;
    }

    @Override
    public boolean adminOnly() {
        return false;
    }

    @Override
    public String execute(JsonNode args, AgentContext context) {
        StringBuilder sb = new StringBuilder();

        Map<String, Object> summary = aiQueryMapper.selectBlogSummary(COMMENT_MODULE_BLOG);
        if (summary != null) {
            sb.append("【站点概览】\n");
            sb.append("文章总数：").append(summary.get("blogCount")).append("\n");
            sb.append("累计阅读量：").append(summary.get("totalReadCount")).append("\n");
            sb.append("博客评论数：").append(summary.get("commentCount")).append("\n");
            sb.append("注册用户数：").append(summary.get("userCount")).append("\n");
            sb.append("分类数：").append(summary.get("categoryCount")).append("\n");
        }

        List<Map<String, Object>> categoryStats = aiQueryMapper.selectCategoryStats();
        if (categoryStats != null && !categoryStats.isEmpty()) {
            sb.append("\n【分类分布】\n");
            for (Map<String, Object> row : categoryStats) {
                sb.append("- ").append(row.get("name")).append("：").append(row.get("value")).append(" 篇\n");
            }
        }

        List<Blog> hot = aiQueryMapper.searchBlogs(null, null, 5);
        if (hot != null && !hot.isEmpty()) {
            sb.append("\n【阅读量最高的文章】\n");
            for (Blog b : hot) {
                sb.append("- id=").append(b.getId())
                        .append(" 《").append(b.getTitle()).append("》")
                        .append(" 阅读:").append(b.getReadCount() == null ? 0 : b.getReadCount())
                        .append("\n");
            }
        }

        if (context != null && context.isAuthenticated()) {
            sb.append("\n（当前提问用户：").append(context.getName() == null ? context.getUsername() : context.getName())
                    .append("，角色：").append(context.getRole()).append("）");
        }
        return sb.toString();
    }
}
