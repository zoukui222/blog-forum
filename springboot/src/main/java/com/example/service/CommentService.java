package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.exception.CustomException;
import com.example.utils.TokenUtils;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Comment;
import com.example.entity.User;
import com.example.mapper.BlogMapper;
import com.example.mapper.CommentMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.Resource;
import java.util.List;

/**
 * 业务处理
 **/
@Service
public class CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;

    @Resource
    private BlogMapper blogMapper;


    /**
     * 删除
     */
    public void deleteById(Integer id) {
        Comment comment = commentMapper.selectById(id);
        Blog blog = blogMapper.selectById(comment.getFid());
        if(blog.getCommentCount() != null) {
            blog.setCommentCount(blog.getCommentCount() - 1);
        }
        blogMapper.updateById(blog);
        commentMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            commentMapper.deleteById(id);
        }
    }
    /**
     * 新增评论
     */
    public void add(Comment comment) {
        comment.setTime(DateUtil.now());
        commentMapper.insert(comment);

        Integer id = comment.getId();
        // 更新root_id
        if (comment.getPid() == null) {
            comment.setRootId(id);
        } else {
            Comment parentComment = commentMapper.selectById(comment.getPid());
            comment.setRootId(parentComment.getRootId());
        }

        Blog blog = blogMapper.selectById(comment.getFid());
        if(blog != null) {
            if(blog.getCommentCount() == null) {
                blog.setCommentCount(1);
            } else {
                blog.setCommentCount(blog.getCommentCount() + 1);
            }
            blogMapper.updateById(blog);
        }

        this.updateById(comment);
    }

    /**
     * 递归删除
     */
    public void deleteDeep(Integer id) {
        // 归属校验：管理员可删除全部，普通用户仅能删除自己的评论
        Comment dbComment = commentMapper.selectById(id);
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(currentUser.getRole())
                && (dbComment == null || !currentUser.getId().equals(dbComment.getUserId()))) {
            throw new CustomException(ResultCodeEnum.NO_AUTH);
        }
        this.deepDelete(id);
    }

    private void deepDelete(Integer pid) {
        List<Comment> children = commentMapper.selectByPid(pid);
        commentMapper.deleteById(pid);
        for (Comment child : children) {
            this.deepDelete(child.getId());
        }
    }


    /**
     * 修改
     */
    public void updateById(Comment comment) {
        commentMapper.updateById(comment);
    }

    /**
     * 根据ID查询
     */
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Comment> selectAll(Comment comment) {
        return commentMapper.selectAll(comment);
    }

    /**
     * 分页查询
     */
    public PageInfo<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.selectAll(comment);
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        list.forEach(com -> {
            if(com.getUserId() != null) {
                User user = userService.selectById(com.getUserId());
                comment.setUserName(!StringUtils.isEmpty(user.getName()) ? user.getName() : user.getUsername() );
            }
        });
        return PageInfo.of(list);
    }


    public Integer selectCount(Integer fid, String module) {
        return commentMapper.selectCount(fid,module);
    }

    public PageInfo<Comment> selectTree(Integer fid, String module, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> rootList = commentMapper.selectRoot(fid, module);
        PageInfo<Comment> pageInfo = PageInfo.of(rootList);
        for (Comment root : rootList) {
            Integer userId = root.getUserId();
            root.setAvatar(userService.selectById(userId).getAvatar());
            Integer rootId = root.getRootId();
            List<Comment> children = commentMapper.selectByRootId(rootId);
            for (Comment child : children) {
                Integer childUserId = child.getUserId();
                child.setAvatar(userService.selectById(childUserId).getAvatar());
            }
            root.setChildren(children);
        }
        return pageInfo;
    }
}
