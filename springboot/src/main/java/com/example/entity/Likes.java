package com.example.entity;


/**
 * 点赞
*/
public class Likes {
    /** ID */
    private Integer id;
    /** 关联ID */
    private Integer fid;
    /** 点赞人ID */
    private Integer userId;
    /** 模块 */
    private String module;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

}
