package com.zhuoxue.api.service;

/**
 * 短信验证码服务接口：负责"发送验证码"和"校验验证码"。
 *
 * <p>验证码是本项目登录、注册、改密码的关键凭证，因此单独抽成一个服务，
 * 被 AuthServiceImpl 和 UserServiceImpl 复用。</p>
 */
public interface SmsCodeService {

    /**
     * 给指定手机号发送短信验证码。
     *
     * <p>内部会做两件事：生成 6 位数字验证码并存到 Redis（带有效期），
     * 再调用阿里云短信接口把验证码发送到手机上。</p>
     *
     * @param phone 手机号
     */
    void sendSmsCode(String phone);

    /**
     * 校验手机号 + 验证码是否匹配。校验通过后会立刻删除该验证码，保证"一次一用"。
     *
     * @param phone 手机号
     * @param code  用户填写的验证码
     * @return 校验通过返回 true；不通过会直接抛出 BizException
     */
    boolean verifyCode(String phone, String code);
}
