package com.zhuoxue.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库中的 t_user 表。
 *
 * <p>实体类（Entity）的作用：把数据库表中的"一行数据"映射成一个 Java 对象。
 * 例如表里有一行 (1, "13800000000", ...)，查询出来后就是一个 User 对象。</p>
 *
 * <p>@TableName("t_user") 告诉 MyBatis-Plus：这个类对应哪一张表。</p>
 */
@Data
@TableName("t_user")
public class User {

    /** 主键 id。@TableId 标记主键，IdType.AUTO 表示由数据库自增生成，插入时不用手动赋值 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号，同时也是登录账号 */
    private String phone;

    /** 密码的哈希值（注意：不是明文密码！注册时会把明文密码加密后再存到这里，防止数据库泄露导致密码外泄） */
    private String passwordHash;

    /** 用户昵称 */
    private String nickname;

    /** 头像图片的访问地址 */
    private String avatarUrl;

    /** 账号状态（具体含义由业务约定，例如 1=正常、0=禁用） */
    private Integer status;

    /** 创建时间（插入数据时由数据库默认值或代码写入） */
    private LocalDateTime createdAt;

    /** 最后更新时间 */
    private LocalDateTime updatedAt;

    /** 删除标记 */
    @TableLogic
    private Integer deleted;
}
