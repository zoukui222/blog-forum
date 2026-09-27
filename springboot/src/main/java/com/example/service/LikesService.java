package com.example.service;

import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Likes;
import com.example.mapper.BlogMapper;
import com.example.mapper.LikesMapper;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LikesService {

    @Resource
    LikesMapper likesMapper;

    @Resource
    BlogMapper blogMapper;

    public void set(Likes likes) {
        Account currentUser = TokenUtils.getCurrentUser();
        likes.setUserId(currentUser.getId());
        Likes dblLikes = likesMapper.selectUserLikes(likes);
        Blog blog = blogMapper.selectById(likes.getFid());
        if (dblLikes == null) {
            likesMapper.insert(likes);
            if(blog != null) {
                if(blog.getLikesCount() == null) {
                    blog.setLikesCount(1);
                } else {
                    blog.setLikesCount(blog.getLikesCount() + 1);
                }
                blogMapper.updateById(blog);
            }
        } else {
            if(blog != null) {
                if(blog.getLikesCount() != null) {
                    blog.setLikesCount(blog.getLikesCount() - 1);
                } else {
                    blog.setLikesCount(1);
                }
                blogMapper.updateById(blog);
            }
            likesMapper.deleteById(dblLikes.getId());
        }
    }

    /**
     * 查询当前用户是否点过赞
     */
    public Likes selectUserLikes(Integer fid, String module) {
        Account currentUser = TokenUtils.getCurrentUser();
        Likes likes = new Likes();
        likes.setUserId(currentUser.getId());
        likes.setFid(fid);
        likes.setModule(module);
        return likesMapper.selectUserLikes(likes);
    }

    public int selectByFidAndModule(Integer fid, String module) {
        return likesMapper.selectByFidAndModule(fid, module);
    }

}
