package com.example.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码工具：统一 BCrypt 加解密校验
 * 兼容存量明文数据：老数据首次登录校验通过后由业务层自动升级为密文
 */
public class PasswordUtils {

    /** BCrypt 密文格式：$2a$ / $2b$ / $2y$ 开头 */
    private static final String BCRYPT_PATTERN = "^\\$2[aby]\\$.*";

    public static String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public static boolean isEncoded(String value) {
        return value != null && value.matches(BCRYPT_PATTERN);
    }

    /**
     * 校验密码：密文用 BCrypt 比对，存量明文用等值比对
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (!isEncoded(storedPassword)) {
            return rawPassword.equals(storedPassword);
        }
        try {
            return BCrypt.checkpw(rawPassword, storedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
