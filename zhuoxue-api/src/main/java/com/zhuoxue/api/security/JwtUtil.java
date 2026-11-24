package com.zhuoxue.api.security;

import com.zhuoxue.api.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类：负责"生成 token"和"解析 token"。
 *
 * <p>JWT（JSON Web Token）是一种"自带信息的登录凭证"：
 * 服务器把用户 id 等信息写进 token 并用密钥签名，之后前端每次请求都带着它；
 * 服务器只要验证签名没问题，就能信任其中的信息，不需要在服务器端保存登录状态。</p>
 *
 * <p>@Component 把这个类注册为 Spring 组件，其它类可以直接注入使用。</p>
 */
@Component
public class JwtUtil {

    /** 签名密钥，来自 application.yaml 的 jwt.secret。密钥一旦泄露，别人就能伪造 token */
    @Value("${jwt.secret}")
    private String secret;

    /** token 有效期（分钟），来自 jwt.access-expire-minutes */
    @Value("${jwt.access-expire-minutes}")
    private long expiration;

    /**
     * 根据密钥字符串生成用于签名的密钥对象。
     *
     * <p>HMAC 算法要求密钥足够长（至少 32 字节），这里的字符串长度是够的。</p>
     *
     * @return 用于签名和验签的密钥
     */
    public SecretKey getSecret() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 为指定用户生成 token。
     *
     * @param userId 用户 id
     * @return 生成的 JWT 字符串
     */
    public String createToken(Long userId) {
        Date now = new Date();
        long expireMills = expiration * 60 * 1000L;          // 分钟换算成毫秒
        Date expireTime = new Date(now.getTime() + expireMills); // 过期时间点

        return Jwts.builder()
                .claim("userId", userId)    // 把用户 id 作为自定义信息写进 token
                .issuedAt(now)              // 签发时间
                .expiration(expireTime)     // 过期时间，超时后 token 失效
                .signWith(getSecret())      // 用密钥签名，防止内容被篡改
                .compact();                 // 生成最终的字符串
    }

    /**
     * 解析并校验 token。
     *
     * @param token 前端传来的 token
     * @return token 中的信息（Claims 可以理解为 Map）
     */
    public Claims parseToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSecret()) // 用同一个密钥验签
                    .build();
            return parser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // 过期是常见情况，单独给出明确提示
            throw new BusinessException(401, "Token 已过期");
        } catch (JwtException | IllegalArgumentException e) {
            // 签名不对、格式错误、内容被篡改等
            throw new BusinessException(401, "Token 无效");
        }
    }

    /**
     * 从 token 中取出用户 id。
     *
     * @param token 前端传来的 token
     * @return 用户 id
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        // 注意：JSON 解析出来的数字默认是 Integer，这里指定按 Long 读取避免类型问题
        return claims.get("userId", Long.class);
    }
}
