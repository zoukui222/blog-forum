package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.common.Constants;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.AdminService;

import com.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Token工具类
 */
@Component
public class TokenUtils {

    private static final Logger log = LoggerFactory.getLogger(TokenUtils.class);

    private static AdminService staticAdminService;
    /*    private static BusinessService staticBusinessService;*/
    private static UserService staticUserService;
    @Resource
    AdminService adminService;

    /* @Resource
     BusinessService businessService;*/
    @Resource
    UserService userService;

    /** JWT 签名密钥：独立于用户密码，避免用户改密导致全部 token 失效，也避免弱密码被离线爆破 */
    private static String jwtSecret;

    @Value("${jwt.secret:blog-xs-default-secret-change-me}")
    private String jwtSecretValue;

    @PostConstruct
    public void setUserService() {
        staticAdminService = adminService;
        /*    staticBusinessService = businessService;*/
        staticUserService=userService;
        jwtSecret = jwtSecretValue;
    }

    public static String getSecret() {
        return jwtSecret;
    }

    /**
     * 生成token
     */
    public static String createToken(String data) {
        return JWT.create().withAudience(data) // 将 userId-role 保存到 token 里面,作为载荷
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2)) // 2小时后token过期
                .sign(Algorithm.HMAC256(jwtSecret)); // 用服务端独立密钥签名
    }

    /**
     * 获取当前登录的用户信息
     */
    public static Account getCurrentUser() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String token = request.getHeader(Constants.TOKEN);
            if (ObjectUtil.isEmpty(token)) {
                // 与 JwtInterceptor 保持一致：SSE / EventSource 这类无法自定义请求头的场景，
                // token 只能放在查询参数里。原先这里只认 header，会导致"拦截器放行了、业务层却拿不到用户"
                token = request.getParameter(Constants.TOKEN);
            }
            if (ObjectUtil.isNotEmpty(token)) {
                String userRole = JWT.decode(token).getAudience().get(0);
                String userId = userRole.split("-")[0];  // 获取用户id
                String role = userRole.split("-")[1];    // 获取角色
                if (RoleEnum.ADMIN.name().equals(role)) {
                    return staticAdminService.selectById(Integer.valueOf(userId));
                } /*else if (RoleEnum.BUSINESS.name().equals(role)) {
                    return staticBusinessService.selectBasicBusinessById(Integer.valueOf(userId));
                }*/else if (RoleEnum.USER.name().equals(role)) {
                    return staticUserService.selectById(Integer.valueOf(userId));
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户信息出错", e);
        }
        return new Account();  // 返回空的账号对象
    }
}
