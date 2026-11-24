package com.zhuoxue.api.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云短信相关配置项的承载类。
 *
 * <p>@ConfigurationProperties(prefix = "aliyun.sms") 表示读取 application.yaml 中
 * aliyun.sms 下面的配置，并自动绑定到本类属性上。</p>
 */
@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {

    /** 阿里云账号的 AccessKeyId（标识是哪个账号） */
    private String accessKeyId;

    /** 阿里云账号的 AccessKeySecret（账号密钥，属于敏感信息，不要泄露） */
    private String accessKeySecret;

    /** 短信签名，例如"恒创联众"（需在阿里云后台审核通过后才能使用） */
    private String signName;

    /** 短信模板编号（需在阿里云后台申请） */
    private String templateCode;
}
