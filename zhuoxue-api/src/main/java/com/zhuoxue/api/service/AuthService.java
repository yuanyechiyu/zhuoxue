package com.zhuoxue.api.service;

/**
 * 认证服务接口：负责验证码登录、注册、密码登录等"身份认证"相关业务。
 *
 * <p>接口只定义"能做什么"，具体怎么做由实现类 AuthServiceImpl 完成。
 * Controller 只依赖接口而不依赖实现类，这样以后想替换实现时，Controller 完全不用改。</p>
 */
public interface AuthService {

    /**
     * 使用手机号 + 短信验证码登录。
     *
     * <p>如果该手机号还没注册过，会自动创建一个新用户（业内常说的"免注册登录"）。</p>
     *
     * @param phone 手机号
     * @param code  短信验证码
     * @return 登录成功后生成的 JWT token，前端需要保存起来，之后请求时放在 Authorization 请求头里
     */
    String smsLogin(String phone, String code);

    /**
     * 使用手机号 + 密码 + 短信验证码注册新账号。
     *
     * @param phone    手机号
     * @param password 明文密码（保存到数据库前会被加密）
     * @param code     短信验证码
     */
    void register(String phone, String password, String code);

    /**
     * 使用手机号 + 密码登录。
     *
     * @param phone    手机号
     * @param password 明文密码（会与数据库中保存的密码哈希值比对）
     * @return 登录成功后生成的 JWT token
     */
    String login(String phone, String password);

}
