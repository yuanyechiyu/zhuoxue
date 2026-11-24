package com.zhuoxue.api.dto;

import lombok.Data;

/**
 * 查询个人资料接口返回的数据。
 *
 * <p>为什么不直接返回 User 实体？因为 User 里有 passwordHash（密码哈希值）这种
 * 绝对不能返回给前端的敏感字段。返回一个只有安全字段的 Response 对象，可以避免密码泄露。</p>
 */
@Data
public class UserResponse {

    /** 手机号（一般会做脱敏展示，例如 138****0000） */
    private String phone;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatarUrl;
}
