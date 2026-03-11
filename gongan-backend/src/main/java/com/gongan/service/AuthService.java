package com.gongan.service;

import com.gongan.dto.LoginRequest;
import com.gongan.dto.LoginResponse;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);
}
