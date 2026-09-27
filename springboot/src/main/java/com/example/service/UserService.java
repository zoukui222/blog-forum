package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import javax.annotation.Resource;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户信息业务处理
 **/
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Value("${server.port:9090}")
    private String port;

    @Value("${ip:localhost}")
    private String ip;

    /**
     * 新增
     */
    public void add(User user) {
        //  1. 校验你的用户名是否重复
        User dbUser = userMapper.selectByUsername(user.getUsername());
        if (ObjectUtil.isNotEmpty(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_EXIST_ERROR);
        }
        // 设置名称
        if (ObjectUtil.isEmpty(user.getName())) {
            user.setName(user.getUsername());
        }
        // 设置头像
        if(user.getAvatar()==null){
            String http = "http://" + ip + ":" + port + "/files/";
            user.setAvatar(http + "default-user.png");
        }
        // 设置默认密码
        if (ObjectUtil.isEmpty(user.getPassword())) {
            user.setPassword("123");
        }
        user.setRole(RoleEnum.USER.name());
        userMapper.insert(user);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        userMapper.deleteById(id);
    }

    /**
     * 批量删除
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            userMapper.deleteById(id);
        }
    }

    /**
     * 修改
     */
    public void updateById(User user) {
        // 归属校验 + 字段白名单：非管理员只能改自己，且不允许改角色与密码（改密码走 /updatePassword）
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            user.setId(currentUser.getId());
            user.setRole(null);
            user.setPassword(null);
            // 白名单过滤后若无任何可更新字段，直接返回，避免生成 "update user set where id=?" 非法 SQL
            if (StrUtil.isBlank(user.getUsername()) && StrUtil.isBlank(user.getName())
                    && StrUtil.isBlank(user.getAvatar()) && StrUtil.isBlank(user.getSex())
                    && StrUtil.isBlank(user.getPhone()) && StrUtil.isBlank(user.getEmail())
                    && StrUtil.isBlank(user.getInfo()) && StrUtil.isBlank(user.getBirth())) {
                return;
            }
        }
        userMapper.updateById(user);
    }

    /**
     * 根据ID查询
     */
    public User selectById(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<User> selectAll(User user) {
        return userMapper.selectAll(user);
    }

    /**
     * 分页查询
     */
    public PageInfo<User> selectPage(User user, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = userMapper.selectAll(user);
        return PageInfo.of(list);
    }

    /**
     * 用户登录
     */
    public Account login(Account account) {
        Account dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.USER_ACCOUNT_ERROR);
        }
        // 生成token：角色取库中真实值（管理员为 ADMIN），兜底 USER
        String role = ObjectUtil.isNotEmpty(dbUser.getRole()) ? dbUser.getRole() : RoleEnum.USER.name();
        String tokenData = dbUser.getId() + "-" + role;
        String token = TokenUtils.createToken(tokenData, dbUser.getPassword());
        dbUser.setToken(token);
        return dbUser;
    }
    /**
     * 用户注册
     */
    public void register(Account account) {
        User user = new User();
        BeanUtils.copyProperties(account, user);  // 拷贝 账号和密码2个属性
        this.add(user);  // 添加账户信息
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException(ResultCodeEnum.PARAM_PASSWORD_ERROR);
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }

}
