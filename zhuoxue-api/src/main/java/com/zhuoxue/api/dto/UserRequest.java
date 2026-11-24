package com.zhuoxue.api.dto;

import lombok.Data;

/**
 * 修改个人资料接口（PUT /api/user/profile）的请求参数。
 *
 * <p>只允许用户修改昵称和头像地址，手机号、密码等敏感信息不放在这里，
 * 这样可以从源头上防止用户"越权"修改不该改的字段。</p>
 */
@Data
public class UserRequest {

    /** 新昵称（不修改时可以传 null） */
    private String nickname;

    /** 新头像地址（必须先通过上传接口拿到，不修改时可以传 null） */
    private String avatarUrl;
}
