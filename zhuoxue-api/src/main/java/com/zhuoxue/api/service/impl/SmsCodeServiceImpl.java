package com.zhuoxue.api.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.zhuoxue.api.exception.BusinessException;
import com.zhuoxue.api.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;


/**
 * 短信验证码服务的具体实现。
 *
 * <p>整体流程：</p>
 * <ol>
 *     <li>检查是否发送太频繁（60 秒内只能发一次）</li>
 *     <li>生成 6 位随机验证码，存到 Redis，有效期 5 分钟</li>
 *     <li>调用阿里云短信接口把验证码发到手机</li>
 *     <li>发送成功后，记一个 60 秒的"冷却标记"用于频率限制</li>
 * </ol>
 *
 * <p>为什么用 Redis 存验证码？因为验证码有有效期、需要快速读写，
 * 而且服务重启后不能丢失，这些正是 Redis 擅长的。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    /** Redis 中存储验证码的 key 前缀，完整 key 形如 sms:code:13800000000 */
    private static final String CODE_KEY_PREFIX = "sms:code:";

    /** Redis 中存储"发送冷却"标记的 key 前缀，完整 key 形如 sms:sendtimelimit:13800000000 */
    private static final String LIMIT_KEY_PREFIX = "sms:sendtimelimit:";

    /** 安全随机数生成器，比 Random 更难被预测，适合生成验证码这类安全相关数据 */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Redis 操作模板：Spring 提供的 Redis 客户端，StringRedisTemplate 表示 key 和 value 都按字符串处理 */
    private final StringRedisTemplate redisTemplate;

    /** 阿里云短信客户端（在 AliyunSmsConfig 中创建好并注册到 Spring 容器） */
    private final Client aliyunSmsClient;

    /**
     * 发送短信验证码。
     *
     * @param phone 手机号
     */
    @Override
    public void sendSmsCode(String phone) {
        // ---------- 第 1 步：频率限制 ----------
        String limitKey = LIMIT_KEY_PREFIX + phone;
        // 查询冷却标记还剩多少秒过期；不存在时返回 -2，没有过期时间时返回 -1
        Long ttl = redisTemplate.getExpire(limitKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            throw new BusinessException(429, "短信发送过于频繁，请等待" + ttl + "秒再试");
        }

        // ---------- 第 2 步：生成验证码并存入 Redis ----------
        // nextInt(1000000) 生成 0~999999 的随机数，%06d 补足 6 位（不足前面补 0）
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        // set(key, value, 5, MINUTES)：5 分钟后 Redis 会自动删除这个 key，即验证码过期
        redisTemplate.opsForValue().set(CODE_KEY_PREFIX + phone, code, 5, TimeUnit.MINUTES);

        // ---------- 第 3 步：调用阿里云接口发送短信 ----------
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest();
        request.setPhoneNumber(phone);
        request.setSignName("恒创联众");       // 短信签名（需在阿里云后台申请通过）
        request.setTemplateCode("100001");    // 短信模板编号
        request.setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}"); // 模板变量：验证码和有效分钟数

        try {
            SendSmsVerifyCodeResponse response = aliyunSmsClient.sendSmsVerifyCode(request);
            // ---------- 第 4 步：发送成功，写入 60 秒冷却标记 ----------
            redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);
            log.info("发送短信成功，response:{}", response);
        } catch (Exception e) {
            // 发送失败：把刚才存进 Redis 的验证码删掉，避免用户拿到一个永远收不到短信的验证码
            redisTemplate.delete(CODE_KEY_PREFIX + phone);
            log.error("发送短信失败", e);
            throw new BusinessException(500, "发送短信失败");
        }
    }

    /**
     * 校验验证码。
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return 校验通过返回 true
     */
    @Override
    public boolean verifyCode(String phone, String code) {
        String codeKey = CODE_KEY_PREFIX + phone;
        // 从 Redis 里取出之前保存的验证码
        String codeValue = redisTemplate.opsForValue().get(codeKey);

        // 取不到说明没发过或者已经超过 5 分钟被自动删除
        if (codeValue == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        // 内容不一致说明填错了
        if (!codeValue.equals(code)) {
            throw new BusinessException(400, "验证码错误");
        }

        // 校验成功后立即删除，保证一个验证码只能用一次
        // （如果这里不删，同一个验证码在 5 分钟内可以被反复使用，存在风险）
        redisTemplate.delete(codeKey);
        return true;
    }
}
