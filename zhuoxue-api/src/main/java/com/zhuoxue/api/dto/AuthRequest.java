package com.zhuoxue.api.dto;

import lombok.Data;

/**
 * 认证相关接口的请求参数类。
 *
 * <p>DTO 是 Data Transfer Object（数据传输对象）的缩写，专门用来接收前端传来的数据。
 * 不直接用实体类 User 接收，是为了避免前端能随意传入 status、createdAt 等不该由用户修改的字段。</p>
 *
 * <p>本项目的几个认证接口共用这个类，不同接口只会用到其中一部分字段，用不到的字段就是 null：</p>
 * <ul>
 *     <li>发送验证码：phone</li>
 *     <li>短信验证码登录：phone + code</li>
 *     <li>注册：phone + password + code</li>
 *     <li>密码登录：phone + password</li>
 * </ul>
 */
@Data
public class AuthRequest {

    /** 手机号 */
    private String phone;

    /** 密码（仅"密码登录"和"注册"时使用） */
    private String password;

    /** 短信验证码（短信登录、注册时使用） */
    private String code;
}
