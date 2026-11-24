package com.zhuoxue.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 的配置类。
 *
 * <p>本项目只使用了 Spring Security 的两项能力：</p>
 * <ol>
 *     <li>密码加密器（BCryptPasswordEncoder）</li>
 *     <li>关闭 Security 自带的登录/拦截逻辑，改由我们自己的 JWT 拦截器来控制登录校验</li>
 * </ol>
 */
@Configuration
public class SecurityConfig {

    /**
     * 注册密码加密器。
     *
     * <p>BCrypt 是一种专门用于密码的单向加密算法：同一个密码每次加密结果都不同（因为自带随机盐），
     * 但可以用 matches(明文, 密文) 判断是否匹配，非常适合存密码。</p>
     *
     * @return 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤器链配置。
     *
     * <p>这里把 Spring Security 自带的几种登录方式全部关闭，并放行所有请求（permitAll），
     * 因为本项目的"是否登录"是由 WebMvcConfig 中注册的 JwtAuthInterceptor 自己判断的，
     * 如果这里也拦截，就会出现两套重复的鉴权逻辑。</p>
     *
     * @param http Spring Security 的配置入口
     * @return 配置好的过滤器链
     * @throws Exception 配置出错时抛出
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())          // 关闭 CSRF 防护（前后端分离 + JWT 场景下一般关闭）
                .formLogin(form -> form.disable())  // 关闭默认的表单登录页
                .httpBasic(basic -> basic.disable())// 关闭 HTTP Basic 认证
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // 所有请求都由 JWT 拦截器负责校验
        return http.build();
    }
}
