package com.zhuoxue.api.security;

/**
 * 当前登录用户上下文工具类。
 *
 * <p>作用：在一次请求处理过程中，让任意位置的代码都能方便地拿到"当前登录用户 id"，
 * 而不用每次都从请求头里重新解析 token。</p>
 *
 * <p>实现原理是 ThreadLocal：它为每个线程单独保存一份数据。
 * 每个 HTTP 请求由 Tomcat 的一个线程处理，所以"线程的数据"就相当于"这个请求的数据"，
 * 多个请求之间不会互相干扰。</p>
 *
 * <p>使用流程：拦截器在请求开始时 set(userId)，Controller/Service 里 getUserId()，
 * 请求结束时拦截器调用 clear() 清理。</p>
 */
public final class UserContext {

    /** 存放当前线程对应的用户 id */
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造方法：防止外部 new 这个类，因为它只提供静态方法。
     */
    private UserContext() {
    }

    /**
     * 保存当前登录用户 id（由拦截器在请求开始时调用）。
     *
     * @param userId 用户 id
     */
    public static void set(Long userId) {
        HOLDER.set(userId);
    }

    /**
     * 获取当前登录用户 id。
     *
     * @return 用户 id；如果没登录过则为 null
     */
    public static Long getUserId() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程保存的用户 id（由拦截器在请求结束时调用）。
     *
     * <p>必须清理，否则线程被复用时可能残留上一个请求的用户 id，造成数据串号。</p>
     */
    public static void clear() {
        HOLDER.remove();
    }
}
