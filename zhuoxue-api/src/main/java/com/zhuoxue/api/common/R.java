package com.zhuoxue.api.common;

public class R<T> {

    /** 状态码：200 表示成功；400 参数错误；401 未登录/Token 失效；404 数据不存在；429 请求太频繁；500 服务器异常 */
    private int code;

    /** 提示信息：成功时一般是 "ok"，失败时是给用户看的中文提示 */
    private String message;

    /** 真正的业务数据，失败时通常为 null */
    private T data;

    /**
     * 构造方法：创建一个返回结果对象。
     * 业务代码一般不直接 new，而是使用下面几个更简洁的静态方法。
     *
     * @param code    状态码
     * @param message 提示信息
     * @param data    业务数据
     */
    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功且不返回数据时使用，例如"修改成功""删除成功"这类只关心成功与否的接口。
     *
     * @return 一个 code=200、data=null 的结果对象
     */
    public static <T> R<T> ok() {
        return new R<>(200, "ok", null);
    }

    /**
     * 成功且需要返回数据时使用，例如返回登录 token、用户信息等。
     *
     * @param data 要返回给前端的数据
     * @return 一个携带 data 的成功结果对象
     */
    public static <T> R<T> ok(T data) {
        return new R<>(200, "ok", data);
    }

    /**
     * 失败时使用。一般由全局异常处理器自动调用，业务代码里抛异常即可，不需要手动调用。
     *
     * @param code    错误码
     * @param message 错误提示
     * @return 一个失败结果对象
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
