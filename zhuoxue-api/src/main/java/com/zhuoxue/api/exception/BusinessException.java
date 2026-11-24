package com.zhuoxue.api.exception;

import lombok.Data;

/**
 * 业务异常类。
 *
 * <p>用于表示"程序没错，但业务上不满足条件"的情况，例如：
 * 用户已存在、验证码错误、密码不正确、手机号不匹配等。</p>
 *
 * <p>它继承 RuntimeException（运行时异常），所以代码里抛出后如果没人捕获，
 * 会被 GlobalExceptionHandler 统一接住，转换成 {"code": ..., "message": ...} 返回给前端。</p>
 *
 * <p>相比返回 null 或错误字符串，抛异常的好处是：业务代码可以随时"中断并说明原因"，
 * 不用一层层往上返回错误信息。</p>
 */
@Data
public class BusinessException extends RuntimeException {

    /** 业务错误码，例如 400、401、404、429、500 */
    private final int code;

    /**
     * 构造业务异常。
     *
     * @param code    错误码
     * @param message 错误提示（会直接展示给前端）
     */
    public BusinessException(int code, String message) {
        super(message); // 调用父类构造方法，把提示信息保存到异常里
        this.code = code;
    }
}
