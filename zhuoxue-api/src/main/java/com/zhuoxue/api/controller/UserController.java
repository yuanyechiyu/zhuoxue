package com.zhuoxue.api.controller;

import com.zhuoxue.api.common.R;
import com.zhuoxue.api.dto.ChangePasswordRequest;
import com.zhuoxue.api.dto.UserRequest;
import com.zhuoxue.api.dto.UserResponse;
import com.zhuoxue.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器：提供"查看/修改个人资料""修改密码"等需要登录后才能访问的接口。
 *
 * <p>这些接口的路径都以 /api/user 开头，且都会经过 JWT 拦截器校验，
 * 请求头里必须带 Authorization: Bearer &lt;token&gt;，否则会返回 401。</p>
 *
 * <p>接口清单：</p>
 * <ul>
 *     <li>GET  /api/user/profile       查询个人资料</li>
 *     <li>PUT  /api/user/profile       修改昵称/头像</li>
 *     <li>POST /api/user/password/code 发送改密码用的验证码</li>
 *     <li>PUT  /api/user/password      修改密码</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    /** 用户服务 */
    private final UserService userService;

    /**
     * 查询当前登录用户的个人资料。
     *
     * <p>请求方式：GET /api/user/profile（无需请求体，用户身份从 token 中获取）</p>
     *
     * @return data 为手机号、昵称、头像
     */
    @GetMapping("/profile")
    public R<UserResponse> getProfile() {
        return userService.getProfile();
    }

    /**
     * 修改当前登录用户的个人资料。
     *
     * <p>请求方式：PUT /api/user/profile，请求体 {"nickname": "...", "avatarUrl": "..."}，
     * 只想改其中一个字段时，另一个字段不传即可。</p>
     *
     * @param userRequest 要修改的内容
     * @return 操作结果
     */
    @PutMapping("/profile")
    public R<Void> putProfile(@RequestBody UserRequest userRequest) {
        return userService.putProfile(userRequest);
    }

    /**
     * 给当前用户绑定的手机号发送验证码（改密码前使用）。
     *
     * <p>请求方式：POST /api/user/password/code，无需请求体</p>
     *
     * @return 操作结果
     */
    @PostMapping("/password/code")
    public R<Void> sendPasswordCode() {
        userService.sendPasswordCode();
        return R.ok();
    }

    /**
     * 修改当前登录用户的密码。
     *
     * <p>请求方式：PUT /api/user/password，请求体
     * {"newPassword": "...", "phone": "...", "code": "..."}</p>
     *
     * @param req 新密码、手机号、验证码
     * @return 操作结果
     */
    @PutMapping("/password")
    public R<Void> changePassword(@RequestBody ChangePasswordRequest req) {
        return userService.changePassword(req);
    }
}
