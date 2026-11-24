package com.zhuoxue.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zhuoxue.api.common.R;
import com.zhuoxue.api.dto.ChangePasswordRequest;
import com.zhuoxue.api.dto.UserRequest;
import com.zhuoxue.api.dto.UserResponse;
import com.zhuoxue.api.entity.User;
import com.zhuoxue.api.exception.BusinessException;
import com.zhuoxue.api.mapper.UserMapper;
import com.zhuoxue.api.security.UserContext;
import com.zhuoxue.api.service.FileStorageService;
import com.zhuoxue.api.service.SmsCodeService;
import com.zhuoxue.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务的具体实现类。
 *
 * <p>这里的方法都通过 UserContext.getUserId() 获取"当前登录用户 id"，
 * 而这个 id 是 JWT 拦截器在请求进来时解析 token 后存进去的，所以业务代码不需要再关心"是谁在请求"。</p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /** 用户表数据访问对象 */
    private final UserMapper userMapper;

    /** 密码加密器，用于把新密码加密后保存 */
    private final PasswordEncoder passwordEncoder;

    /** 短信验证码服务，用于改密码前校验验证码 */
    private final SmsCodeService smsCodeService;

    /** 文件存储服务，用于校验头像地址、删除旧头像 */
    private final FileStorageService fileStorageService;

    /**
     * 查询当前登录用户的资料。
     *
     * @return 包含手机号、昵称、头像的成功结果
     */
    @Override
    public R<UserResponse> getProfile() {
        // 从 UserContext 中拿到当前登录用户的 id（拦截器已经解析 token 并放进去）
        Long userId = UserContext.getUserId();

        // 按 id 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 把数据库实体转换成返回给前端的对象（只暴露安全字段，绝不返回密码哈希）
        UserResponse userResponse = new UserResponse();
        userResponse.setPhone(user.getPhone());
        userResponse.setNickname(user.getNickname());
        userResponse.setAvatarUrl(user.getAvatarUrl());
        return R.ok(userResponse);
    }

    /**
     * 修改当前登录用户的昵称和头像。
     *
     * <p>规则：只更新传了值的字段（值为 null 表示"这次不改这个字段"）；
     * 昵称不能为空且不能超过 20 个字；头像地址必须来自本站上传接口。</p>
     *
     * @param userRequest 要修改的内容
     * @return 操作结果
     */
    @Override
    public R<Void> putProfile(UserRequest userRequest) {
        Long userId = UserContext.getUserId();

        // 先查一次旧数据：一方面确认用户存在，另一方面后面要拿旧头像地址做删除
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // ---------- 处理昵称 ----------
        String nickname = userRequest.getNickname();
        if (nickname != null) {
            nickname = nickname.trim(); // 去掉首尾空格
            if (nickname.isEmpty()) {
                throw new BusinessException(400, "昵称不能为空");
            }
            if (nickname.length() > 20) {
                throw new BusinessException(400, "昵称长度不能超过20个字符");
            }
        }

        // ---------- 处理头像地址 ----------
        String avatarUrl = userRequest.getAvatarUrl();
        if (avatarUrl != null) {
            avatarUrl = avatarUrl.trim();
            if (avatarUrl.isEmpty()) {
                // 传空字符串表示"清空头像"，统一转成 null 方便后续判断
                avatarUrl = null;
            } else if (!fileStorageService.isOwnedUrl(avatarUrl)) {
                // 防止用户随便填一个外部链接当作头像
                throw new BusinessException(400, "头像地址不合法，请先通过上传接口获取");
            }
        }

        // 两个字段都没传，等于什么都没改，直接返回成功
        if (nickname == null && avatarUrl == null) {
            return R.ok();
        }

        // 用 UpdateWrapper 按需拼接 SET 子句，做到"只更新传了值的字段"
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userId);
        if (nickname != null) {
            updateWrapper.set(User::getNickname, nickname);
        }
        if (avatarUrl != null) {
            updateWrapper.set(User::getAvatarUrl, avatarUrl);
        }
        // 第一个参数传 null 表示：仅根据 wrapper 里的条件更新，不需要传实体对象
        userMapper.update(null, updateWrapper);

        // 如果头像确实换成了新地址，就把存储里的旧头像文件删掉，避免产生垃圾文件
        if (avatarUrl != null && user.getAvatarUrl() != null && !avatarUrl.equals(user.getAvatarUrl())) {
            fileStorageService.deleteByUrl(user.getAvatarUrl());
        }
        return R.ok();
    }

    /**
     * 给当前登录用户绑定的手机号发送验证码（用于改密码）。
     */
    @Override
    public void sendPasswordCode() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        // 手机号取自数据库，而不是前端传，防止给别人的手机号发验证码
        smsCodeService.sendSmsCode(user.getPhone());
    }

    /**
     * 修改密码：校验新密码 → 校验用户 → 校验手机号是否属于本人 → 校验验证码 → 加密并更新。
     *
     * @param req 包含新密码、手机号、验证码的请求对象
     * @return 操作结果
     */
    @Override
    public R<Void> changePassword(ChangePasswordRequest req) {
        // 第 1 步：新密码不能为空（isBlank 同时能判断空字符串和纯空格）
        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
            throw new BusinessException(400, "新密码不能为空");
        }

        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 第 2 步：前端传来的手机号必须与当前账号绑定的手机号一致，
        // 否则用户可能拿别人手机号的验证码来改自己的密码
        if (!user.getPhone().equals(req.getPhone())) {
            throw new BusinessException(403, "手机号与当前账号不匹配");
        }

        // 第 3 步：校验短信验证码
        smsCodeService.verifyCode(req.getPhone(), req.getCode());

        // 第 4 步：新密码加密后更新到数据库
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getId, userId)
                .set(User::getPasswordHash, passwordEncoder.encode(req.getNewPassword()));
        userMapper.update(null, updateWrapper);
        return R.ok();
    }
}
