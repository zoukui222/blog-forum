package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Activity;
import com.example.entity.ActivitySign;
import com.example.exception.CustomException;
import com.example.mapper.ActivitySignMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ActivitySignService {

    @Resource
    private ActivityService activityService;

    @Resource
    ActivitySignMapper activitySignMapper;

    public void add(ActivitySign activitySign) {
        Account currentUser = TokenUtils.getCurrentUser();
        Activity activity = activityService.selectById(activitySign.getActivityId());
        ActivitySign as = this.selectByActivityIdAndUserId(activitySign.getActivityId(), currentUser.getId());  // 查看用户是否已经报名
        if (as != null) {
            throw new CustomException(ResultCodeEnum.ACTIVITY_SIGN_ERROR);
        }
        activitySign.setUserId(currentUser.getId());
        activitySign.setUserName(currentUser.getName());
        activitySign.setActivityName(activity.getName());
        activitySign.setSignTime(new Date());
        activitySignMapper.insert(activitySign);
    }

    public ActivitySign selectByActivityIdAndUserId(Integer actId, Integer userId) {
        return activitySignMapper.selectByActivityIdAndUserId(actId, userId);
    }

    public PageInfo<ActivitySign> selectPage(ActivitySign activitySign, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ActivitySign> list = activitySignMapper.selectAll(activitySign);
        return PageInfo.of(list);
    }

    public void deleteById(Integer id) {
        activitySignMapper.deleteById(id);
    }

    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    public void userDelete(Integer activityId, Integer useId) {
        // 归属校验：非管理员只能取消自己的报名
        Account currentUser = TokenUtils.getCurrentUser();
        Integer targetUserId = useId;
        if (!RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            targetUserId = currentUser.getId();
        }
        activitySignMapper.userDelete(activityId, targetUserId);
    }
}
