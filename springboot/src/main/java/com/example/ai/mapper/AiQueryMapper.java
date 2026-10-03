package com.example.ai.mapper;

import com.example.entity.Blog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * AI Agent 专用只读查询
 *
 * 单独放一套而不复用 BlogMapper.selectAll：Agent 要的是"轻量摘要 + 排序 + 限量"，
 * 与页面接口要的"全字段 + 分页"取向不同；分开写也避免为了 AI 需求去改页面接口的 SQL，
 * 降低把线上功能改坏的风险。
 */
public interface AiQueryMapper {

    /** 按关键词/分类检索文章，返回轻量摘要字段（不含 content），按阅读量倒序 */
    List<Blog> searchBlogs(@Param("keyword") String keyword,
                           @Param("categoryId") Integer categoryId,
                           @Param("limit") Integer limit);

    /** 最新发布 */
    List<Blog> selectLatestBlogs(@Param("limit") Integer limit);

    /** 单篇文章详情（含正文） */
    Blog selectBlogDetail(@Param("id") Integer id);

    /** 站点汇总：文章数、总阅读量、总评论数、用户数（commentModule 用于区分博客评论/活动评论） */
    Map<String, Object> selectBlogSummary(@Param("commentModule") String commentModule);

    /** 分类维度分布 */
    List<Map<String, Object>> selectCategoryStats();

    /** 分类列表（供模型把自然语言里的分类名映射成 id） */
    List<Map<String, Object>> selectCategoryList();
}
