package com.zhuoxue.api.config;

import com.zhuoxue.api.security.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 的配置类：主要是注册 JWT 登录拦截器。
 *
 * <p>实现 WebMvcConfigurer 接口并重写 addInterceptors 方法，
 * 就可以在 Spring MVC 启动时把我们写的拦截器挂到指定的请求路径上。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** 我们自己编写的 JWT 登录校验拦截器 */
    private final JwtAuthInterceptor jwtAuthInterceptor;

    /**
     * 注册拦截器。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                // 拦截所有 /api 开头的请求
                .addPathPatterns("/api/**")
                // 但排除下面这几个"还没登录就要访问"的接口，
                // 否则用户无法登录（因为登录时还没有 token 可带）
                .excludePathPatterns("/api/auth/sms/send",
                        "/api/auth/sms/login",
                        "/api/auth/register",
                        "/api/auth/login");
    }
}
