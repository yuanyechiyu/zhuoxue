package com.zhuoxue.api.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO 相关配置项的承载类。
 *
 * <p>@ConfigurationProperties(prefix = "minio") 表示：把 application.yaml 中
 * 以 minio 开头的那一段配置，按"短横线转驼峰"的规则自动绑定到本类的属性上。例如：</p>
 * <pre>
 * minio:
 *   endpoint: http://localhost:9000   →  endpoint
 *   access-key: minioadmin            →  accessKey
 *   max-size-mb: 2                    →  maxSizeMb
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** MinIO 服务地址，默认本地 9000 端口 */
    private String endpoint = "http://localhost:9000";

    /** 访问密钥（相当于账号） */
    private String accessKey;

    /** 访问密钥（相当于密码） */
    private String secretKey;

    /** 存储桶名称 */
    private String bucket = "zhuoxue";

    /** 对外的公共访问域名，配置了就用它拼接图片地址，否则用 endpoint + bucket */
    private String publicDomain;

    /** 头像文件大小上限（单位 MB） */
    private long maxSizeMb = 2;
}
