package com.zhuoxue.api.config;

import com.zhuoxue.api.common.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储的配置类。
 *
 * <p>@Configuration 表示这是一个配置类，里面可以用 @Bean 方法向 Spring 容器注册对象。</p>
 *
 * <p>@EnableConfigurationProperties(MinioProperties.class) 让 Spring 读取 application.yaml 中
 * 以 minio 开头的那一段配置，并自动填充到 MinioProperties 对象里。</p>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    /** 读取到的 MinIO 配置 */
    private final MinioProperties minioProperties;

    /**
     * 创建一个 MinioClient 并交给 Spring 管理。
     *
     * <p>@Bean 表示这个方法的返回值会成为一个"Bean"：Spring 会调用一次这个方法，
     * 把返回的客户端对象存起来，之后任何类只要声明需要 MinioClient，就会拿到这一个实例。</p>
     *
     * @return 配置好的 MinIO 客户端
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint()) // 服务地址，例如 http://localhost:9000
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey()) // 账号密钥
                .build();
    }
}
