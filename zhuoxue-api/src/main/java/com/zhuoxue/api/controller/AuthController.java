package com.zhuoxue.api.controller;

import com.zhuoxue.api.common.R;
import com.zhuoxue.api.dto.AuthRequest;
import com.zhuoxue.api.service.AuthService;
import com.zhuoxue.api.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器：对外提供"发送验证码、登录、注册"这几个 HTTP 接口。
 *
 * <p>Controller（控制层）的职责很单一：接收前端请求、把参数交给 Service 处理、
 * 再把结果包装成 R 返回给前端。真正的业务逻辑都写在 Service 里。</p>
 *
 * <p>@RestController = @Controller + @ResponseBody，表示这个类里所有方法的返回值
 * 都会自动被转成 JSON 写回给前端。</p>
 *
 * <p>@RequestMapping("/api/auth") 给这个类下所有接口加上统一前缀，最终路径如下：</p>
 * <ul>
 *     <li>POST /api/auth/sms/send  发送短信验证码</li>
 *     <li>POST /api/auth/sms/login 短信验证码登录</li>
 *     <li>POST /api/auth/register  注册</li>
 *     <li>POST /api/auth/login     密码登录</li>
 * </ul>
 *
 * <p>注意：这四个接口在 WebMvcConfig 中已被排除在 JWT 拦截之外，因为用户还没登录，
 * 不可能携带 token。</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 短信验证码服务 */
    private final SmsCodeService smsCodeService;

    /** 认证服务 */
    private final AuthService authService;

    /**
     * 发送短信验证码。
     *
     * <p>请求方式：POST /api/auth/sms/send，请求体 JSON 只需 {"phone": "13800000000"}</p>
     *
     * @param req 请求参数（这里只用到 phone）
     * @return 成功时 data 为 null
     */
    @PostMapping("/sms/send")
    public R<Void> sendCode(@RequestBody AuthRequest req) {
        // @RequestBody：把前端传来的 JSON 自动转换成 AuthRequest 对象
        smsCodeService.sendSmsCode(req.getPhone());
        return R.ok();
    }

    /**
     * 短信验证码登录。
     *
     * <p>请求方式：POST /api/auth/sms/login，请求体 {"phone": "...", "code": "123456"}</p>
     *
     * @param req 请求参数（phone + code）
     * @return 成功时 data 是 JWT token 字符串
     */
    @PostMapping("/sms/login")
    public R<String> smsLogin(@RequestBody AuthRequest req) {
        return R.ok(authService.smsLogin(req.getPhone(), req.getCode()));
    }

    /**
     * 注册账号。
     *
     * <p>请求方式：POST /api/auth/register，请求体 {"phone": "...", "password": "...", "code": "..."}</p>
     *
     * @param req 请求参数（phone + password + code）
     * @return 成功时 data 为 null
     */
    @PostMapping("/register")
    public R<Void> register(@RequestBody AuthRequest req) {
        authService.register(req.getPhone(), req.getPassword(), req.getCode());
        return R.ok();
    }

    /**
     * 手机号 + 密码登录。
     *
     * <p>请求方式：POST /api/auth/login，请求体 {"phone": "...", "password": "..."}</p>
     *
     * @param req 请求参数（phone + password）
     * @return 成功时 data 是 JWT token 字符串
     */
    @PostMapping("/login")
    public R<String> login(@RequestBody AuthRequest req) {
        String token = authService.login(req.getPhone(), req.getPassword());
        return R.ok(token);
    }

}
