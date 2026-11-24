package com.zhuoxue.api.dto;

import lombok.Data;

/**
 * 修改密码接口（PUT /api/user/password）的请求参数。
 *
 * <p>修改密码为什么要带手机号和验证码？因为仅凭登录状态就允许改密码风险较高，
 * 要求再验证一次手机号 + 短信验证码，可以确认确实是本人在操作。</p>
 */
@Data
public class ChangePasswordRequest {

    /** 新密码 */
    private String newPassword;

    /** 当前账号绑定的手机号 */
    private String phone;

    /** 短信验证码 */
    private String code;
}
