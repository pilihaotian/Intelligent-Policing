package com.gongan.service.impl;

import com.gongan.dto.LoginRequest;
import com.gongan.dto.LoginResponse;
import com.gongan.entity.SysUser;
import com.gongan.exception.BusinessException;
import com.gongan.mapper.SysUserMapper;
import com.gongan.service.AuthService;
import com.gongan.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = userMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw BusinessException.of("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw BusinessException.of("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw BusinessException.of("用户名或密码错误");
        }

        // 获取角色和权限
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        List<String> permissions;

        // 管理员拥有所有权限
        if (user.getIsAdmin() == 1) {
            permissions = java.util.Collections.singletonList("*:*:*");
        } else {
            permissions = userMapper.selectPermissionsByUserId(user.getId());
        }

        // 生成令牌
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roles, permissions);

        // 更新登录信息
        SysUser updateUser = new SysUser();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(updateUser);

        // 构建响应
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .orgId(user.getOrgId())
                .deptId(user.getDeptId())
                .avatar(user.getAvatar())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public void logout() {
        // 可以在这里处理令牌失效逻辑（如加入黑名单）
        log.info("用户登出成功");
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌并生成新的访问令牌
        // 这里简化处理，实际项目中需要单独的刷新令牌机制
        Long userId = jwtUtils.getUserId(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);
        List<String> roles = jwtUtils.getRoles(refreshToken);
        List<String> permissions = jwtUtils.getPermissions(refreshToken);

        String newToken = jwtUtils.generateToken(userId, username, roles, permissions);

        return LoginResponse.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(86400L)
                .userId(userId)
                .username(username)
                .roles(roles)
                .permissions(permissions)
                .build();
    }
}
