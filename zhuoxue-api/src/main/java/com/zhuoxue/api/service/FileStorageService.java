package com.zhuoxue.api.service;

import com.zhuoxue.api.dto.AvatarUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务接口：负责头像文件的上传与删除。
 *
 * <p>这里只定义"能力"，底层用 MinIO、阿里云 OSS 还是本地磁盘都可以。
 * 当前实现是 MinioFileStorageServiceImpl。</p>
 */
public interface FileStorageService {

    /**
     * 上传头像文件。
     *
     * @param file   前端上传的文件（Spring MVC 用 MultipartFile 表示上传的文件）
     * @param userId 当前登录用户 id，用于按用户分目录存放文件
     * @return 包含访问地址和对象键的响应对象
     */
    AvatarUploadResponse uploadAvatar(MultipartFile file, Long userId);

    /**
     * 判断一个头像地址是否是"我们自己存储服务里的地址"。
     *
     * <p>用于防止用户把外部的任意 URL 填成头像，从而绕过上传流程。</p>
     *
     * @param avatarUrl 头像地址
     * @return 是本站存储地址返回 true
     */
    boolean isOwnedUrl(String avatarUrl);

    /**
     * 根据头像地址删除对应的存储文件。
     *
     * <p>一般用于用户换头像时清理旧头像，节省存储空间。删除失败只记日志，不影响主流程。</p>
     *
     * @param avatarUrl 头像地址
     */
    void deleteByUrl(String avatarUrl);
}
