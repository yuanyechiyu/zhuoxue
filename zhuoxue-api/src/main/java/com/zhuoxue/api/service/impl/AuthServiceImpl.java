package com.zhuoxue.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhuoxue.api.entity.User;
import com.zhuoxue.api.exception.BusinessException;
import com.zhuoxue.api.mapper.UserMapper;
import com.zhuoxue.api.security.JwtUtil;
import com.zhuoxue.api.service.AuthService;
import com.zhuoxue.api.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务的具体实现类。
 *
 * <p>@Service 注解：告诉 Spring 这是一个业务层组件，启动时自动创建对象放进容器，别处可以直接注入使用。</p>
 *
 * <p>@RequiredArgsConstructor 是 Lombok 注解，会为所有 final 字段生成一个构造方法。
 * Spring 看到这个构造方法，就会自动把容器里对应的对象"注入"进来，这就是常说的"构造器注入"，
 * 好处是字段可以是 final，创建后不会被改掉，更安全。</p>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    /** 用户表的数据访问对象，用来查询/插入用户 */
    private final UserMapper userMapper;

    /** JWT 工具类，用来生成登录 token */
    private final JwtUtil jwtUtil;

    /** 密码加密器（由 SecurityConfig 提供的 BCrypt 实现），用于加密密码和校验密码 */
    private final PasswordEncoder passwordEncoder;

    /** 短信验证码服务，用于校验验证码 */
    private final SmsCodeService smsCodeService;

    /**
     * 短信验证码登录：校验验证码 → 查手机号对应的用户 → 不存在则自动注册 → 生成 token。
     *
     * @param phone 手机号
     * @param code  短信验证码
     * @return 登录 token
     */
    @Override
    public String smsLogin(String phone, String code) {
        // 第 1 步：校验验证码（验证码错误或过期会直接抛出异常，后面代码不会执行）
        smsCodeService.verifyCode(phone, code);

        // 第 2 步：按手机号查询用户。LambdaQueryWrapper 是 MyBatis-Plus 的条件构造器，
        // eq 表示 "等于"，即 WHERE phone = ?
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        // 第 3 步：如果用户不存在，说明是第一次用这个手机号登录，自动创建一个新用户
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            userMapper.insert(user);
            // 注意：insert 之后 MyBatis-Plus 会自动把数据库生成的自增 id 回填到 user 对象里，
            // 所以下面才能直接拿到 user.getId()
        }

        // 第 4 步：生成并返回 JWT token，token 里包含了用户 id
        return jwtUtil.createToken(user.getId());
    }

    /**
     * 注册：校验验证码 → 检查手机号是否已注册 → 加密密码 → 插入新用户。
     *
     * @param phone    手机号
     * @param password 明文密码
     * @param code     短信验证码
     */
    @Override
    public void register(String phone, String password, String code) {
        // 第 1 步：校验验证码
        smsCodeService.verifyCode(phone, code);

        // 第 2 步：查询该手机号是否已经注册过
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        // 第 3 步：已存在则报错，阻止重复注册
        if (user != null) {
            throw new BusinessException(400, "用户已存在");
        }

        // 第 4 步：创建新用户，密码必须加密后再保存（BCrypt 加密后无法反推明文，安全性高）
        user = new User();
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        userMapper.insert(user);
    }

    /**
     * 密码登录：按手机号查用户 → 校验是否设置过密码 → 比对密码 → 生成 token。
     *
     * @param phone    手机号
     * @param password 明文密码
     * @return 登录 token
     */
    @Override
    public String login(String phone, String password) {
        // 第 1 步：按手机号查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);

        // 第 2 步：用户不存在
        if (user == null) {
            throw new BusinessException(400, "用户不存在");
        }

        // 第 3 步：如果用户从来没有设置过密码（例如一直是短信登录的账号），提示改用短信登录
        if (user.getPasswordHash() == null) {
            throw new BusinessException(400, "该账号尚未设置密码，请用短信登录");
        }

        // 第 4 步：比对密码。注意加密是不可逆的，不能用"解密后比较"，
        // 而是用 matches(明文, 密文) 让 BCrypt 自己判断是否匹配
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 第 5 步：登录成功，生成 token 返回
        return jwtUtil.createToken(user.getId());
    }

}
