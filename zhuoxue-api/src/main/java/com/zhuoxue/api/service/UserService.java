package com.zhuoxue.api.service;

import com.zhuoxue.api.common.R;
import com.zhuoxue.api.dto.ChangePasswordRequest;
import com.zhuoxue.api.dto.UserRequest;
import com.zhuoxue.api.dto.UserResponse;

/**
 * 用户服务接口：负责"已登录用户"管理自己资料的相关业务。
 *
 * <p>注意：这些方法都默认当前用户已经登录（能拿到 userId），
 * userId 来自 JWT 拦截器解析后存入的 UserContext。</p>
 */
public interface UserService {

    /**
     * 查询当前登录用户的个人资料。
     *
     * @return 包装了用户资料的成功结果
     */
    R<UserResponse> getProfile();

    /**
     * 修改当前登录用户的昵称和头像。
     *
     * @param userRequest 要修改的字段（只修改传了值的字段）
     * @return 操作结果
     */
    R<Void> putProfile(UserRequest userRequest);

    /**
     * 给当前登录用户绑定的手机号发送一条短信验证码（用于修改密码前的身份确认）。
     */
    void sendPasswordCode();

    /**
     * 修改当前登录用户的密码，需要校验手机号和短信验证码。
     *
     * @param req 包含新密码、手机号、验证码的请求对象
     * @return 操作结果
     */
    R<Void> changePassword(ChangePasswordRequest req);
}
