package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class Activity {

    /** ID */
    private Integer id;
    /** 活动名称 */
    private String name;
    /** 活动简介 */
    private String descr;
    /** 开始时间 */
    private String start;
    /** 结束时间 */
    private String end;
    /** 活动形式 */
    private String form;
    /** 活动地址 */
    private String address;
    /** 主办方 */
    private String host;
    /** 浏览量 */
    private Integer readCount;
    private String content;
    private String cover;
    @TableField(exist = false)
    private boolean IsEnd;
    private Integer blogCount;
    private Integer likesCount;
    private Integer collectCount;
    @TableField(exist = false)
    private boolean IsLike;
    @TableField(exist = false)
    private boolean IsCollect;
    @TableField(exist = false)
    private boolean IsSign;
    private Integer userId;

}
