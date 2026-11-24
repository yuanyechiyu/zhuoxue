package com.zhuoxue.api.service.impl;

import com.zhuoxue.api.common.MinioProperties;
import com.zhuoxue.api.dto.AvatarUploadResponse;
import com.zhuoxue.api.exception.BusinessException;
import com.zhuoxue.api.service.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 基于 MinIO 的文件存储实现。
 *
 * <p>MinIO 是一个可以自己部署的"对象存储"服务，用法类似阿里云 OSS：
 * 文件不是存在服务器的文件夹里，而是上传到一个"存储桶（bucket）"中，每个文件有一个对象键（objectKey）。</p>
 *
 * <p>本类只处理头像上传，做了三重校验：文件非空、大小不超限、类型必须是图片。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageServiceImpl implements FileStorageService {

    /** 允许上传的图片类型（MIME 类型），不在这个集合里的一律拒绝 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    /** MIME 类型 → 文件扩展名的映射，用来根据文件类型生成正确的后缀名 */
    private static final Map<String, String> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    /** MinIO 客户端（在 MinioConfig 中创建），用来真正执行上传、删除操作 */
    private final MinioClient minioClient;

    /** MinIO 配置（地址、密钥、桶名、大小上限等），来自 application.yaml 的 minio 配置 */
    private final MinioProperties minioProperties;

    /**
     * 上传头像。
     *
     * @param file   前端上传的文件
     * @param userId 当前用户 id，用于把不同用户的头像放在不同目录
     * @return 包含访问地址和对象键的响应对象
     */
    @Override
    public AvatarUploadResponse uploadAvatar(MultipartFile file, Long userId) {
        // ---------- 校验 1：文件不能为空 ----------
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的头像");
        }

        // ---------- 校验 2：文件大小不能超过配置的上限 ----------
        long maxBytes = minioProperties.getMaxSizeMb() * 1024 * 1024; // MB 换算成字节
        if (file.getSize() > maxBytes) {
            throw new BusinessException(400, "头像大小不能超过" + minioProperties.getMaxSizeMb() + "MB");
        }

        // ---------- 校验 3：文件类型必须是允许的图片格式 ----------
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(400, "头像仅支持 jpg、png、webp 格式");
        }

        // 生成对象键：avatar/用户id/随机字符串.扩展名
        // 用 UUID 做文件名，避免用户上传同名文件互相覆盖，也避免暴露原始文件名
        String objectKey = "avatar/" + userId + "/"
                + UUID.randomUUID().toString().replace("-", "")
                + "." + CONTENT_TYPE_EXTENSIONS.get(contentType);

        // try-with-resources 语法：括号里的流会在代码块结束后自动关闭，防止资源泄漏
        try (InputStream inputStream = file.getInputStream()) {
            // 把文件流上传到 MinIO 指定的桶里
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1) // -1 表示分片大小由 MinIO SDK 自动决定
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("头像上传失败, userId={}, objectKey={}", userId, objectKey, e);
            throw new BusinessException(500, "头像上传失败，请稍后重试");
        }

        // 上传成功，拼装返回结果
        AvatarUploadResponse response = new AvatarUploadResponse();
        response.setObjectKey(objectKey);
        response.setAvatarUrl(buildUrl(objectKey));
        return response;
    }

    /**
     * 判断头像地址是否属于本站存储。
     *
     * <p>原理：本站生成的头像地址一定以 publicBaseUrl() 开头，
     * 外部随便填的地址一般不会以它开头。</p>
     *
     * @param avatarUrl 头像地址
     * @return 是本站地址返回 true
     */
    @Override
    public boolean isOwnedUrl(String avatarUrl) {
        return avatarUrl != null && avatarUrl.startsWith(publicBaseUrl() + "/");
    }

    /**
     * 根据头像地址删除存储中的文件。
     *
     * @param avatarUrl 头像地址
     */
    @Override
    public void deleteByUrl(String avatarUrl) {
        // 不是本站地址就不处理，避免误删别人的资源
        if (!isOwnedUrl(avatarUrl)) {
            return;
        }
        // 从完整地址中截取对象键：去掉前面 "基础地址/" 的部分
        String objectKey = avatarUrl.substring(publicBaseUrl().length() + 1);
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            // 删除旧头像失败不影响用户操作，只记录警告日志即可
            log.warn("删除旧头像失败, objectKey={}", objectKey, e);
        }
    }

    /**
     * 把对象键拼成完整的访问地址。
     *
     * @param objectKey 对象键，例如 avatar/1/xxx.png
     * @return 完整地址，例如 http://localhost:9000/zhuoxue/avatar/1/xxx.png
     */
    private String buildUrl(String objectKey) {
        return publicBaseUrl() + "/" + objectKey;
    }

    /**
     * 计算文件访问的"基础地址"。
     *
     * <p>如果配置了 public-domain（对外访问域名）就用它，否则用 endpoint + 桶名。
     * 最后统一去掉末尾多余的斜杠，保证拼接出来的地址格式正确。</p>
     *
     * @return 基础地址
     */
    private String publicBaseUrl() {
        String publicDomain = minioProperties.getPublicDomain();
        String baseUrl = (publicDomain == null || publicDomain.isBlank())
                ? minioProperties.getEndpoint() + "/" + minioProperties.getBucket()
                : publicDomain;
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
