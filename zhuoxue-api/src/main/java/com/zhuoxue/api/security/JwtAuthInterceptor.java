package com.zhuoxue.api.security;

import com.zhuoxue.api.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 登录校验拦截器。
 *
 * <p>工作时机：每个被拦住的请求进入 Controller 之前，都会先执行 preHandle 方法。</p>
 *
 * <p>流程：从请求头 Authorization 中取出 "Bearer xxx" 形式的 token →
 * 解析出用户 id → 存进 UserContext → 放行；任何一步失败都抛出 401 异常。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    /** 请求头中 token 的固定前缀，注意后面有一个空格："Bearer " */
    public static final String BEARER_PREFIX = "Bearer ";

    /** JWT 工具类，用来解析 token */
    private final JwtUtil jwtUtil;

    /**
     * 请求处理前执行：校验 token 并保存当前用户 id。
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  即将处理该请求的处理器（就是 Controller 方法）
     * @return true 表示校验通过、继续执行；false 表示中断请求
     * @throws Exception 校验失败时抛出业务异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 取出请求头 Authorization，例如 "Bearer eyJhbGciOi..."
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(401, "未登录或登录已失效");
        }

        // 截掉 "Bearer " 前缀，只保留真正的 token
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 解析 token 拿到用户 id（token 无效或过期会在 JwtUtil 里抛异常）
        Long userId = jwtUtil.getUserId(token);

        // 把用户 id 存进当前线程的上下文，后面的 Controller/Service 就能直接取用
        UserContext.set(userId);
        return true;
    }

    /**
     * 请求处理完成后执行：清理 UserContext，防止用户 id 残留。
     *
     * <p>这一步非常重要！因为 Tomcat 会复用线程，如果不清理，
     * 下一个请求可能"误拿到"上一个用户的 id，造成严重的串号/越权问题。</p>
     *
     * @param request  当前请求
     * @param response 当前响应
     * @param handler  处理器
     * @param ex       处理过程中出现的异常（没有则为 null）
     * @throws Exception 抛出异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}
