package com.zhuoxue.api.config;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.zhuoxue.api.common.AliyunSmsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云短信服务的配置类。
 *
 * <p>负责读取 application.yaml 中 aliyun.sms 的配置，并创建一个可以调用阿里云短信接口的客户端。</p>
 *
 * <p>备注：发送短信时用的固定值（签名、模板编号）目前是写在 SmsCodeServiceImpl 里的，
 * 配置类里虽然读了 signName、templateCode，但暂时没有用到。</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AliyunSmsProperties.class)
public class AliyunSmsConfig {

    /** 阿里云短信相关配置（AccessKey 等） */
    private final AliyunSmsProperties aliyunSmsProperties;

    /**
     * 创建阿里云短信客户端并注册为 Bean。
     *
     * @return 阿里云短信客户端
     * @throws Exception 创建失败时抛出
     */
    @Bean
    public Client createClient() throws Exception {
        // 用 AccessKey 构造阿里云的统一配置对象
        Config config = new Config()
                .setAccessKeyId(aliyunSmsProperties.getAccessKeyId())
                .setAccessKeySecret(aliyunSmsProperties.getAccessKeySecret());
        // 指定短信服务（号码认证服务 dypnsapi）的接入地址
        config.endpoint = "dypnsapi.aliyuncs.com";
        return new Client(config);
    }
}
