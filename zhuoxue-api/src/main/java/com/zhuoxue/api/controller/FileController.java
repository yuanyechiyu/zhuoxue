package com.zhuoxue.api.controller;

import com.zhuoxue.api.common.R;
import com.zhuoxue.api.dto.AvatarUploadResponse;
import com.zhuoxue.api.security.UserContext;
import com.zhuoxue.api.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件控制器：目前只提供"上传头像"接口。
 *
 * <p>上传文件和普通 JSON 请求不一样：前端需要用 multipart/form-data 表单格式提交，
 * 参数名固定为 file，所以这里用 @RequestParam("file") 接收，而不是 @RequestBody。</p>
 *
 * <p>请求方式：POST /api/file/avatar（需要登录，请求头带 token）</p>
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    /** 文件存储服务 */
    private final FileStorageService fileStorageService;

    /**
     * 上传头像。
     *
     * @param file 前端表单中名为 file 的文件
     * @return data 为头像访问地址和对象键
     */
    @PostMapping("/avatar")
    public R<AvatarUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
        // UserContext.getUserId() 从拦截器存入的上下文中取出当前用户 id，
        // 这样文件会按用户分目录存放，也不用前端自己传用户 id（防止伪造）
        return R.ok(fileStorageService.uploadAvatar(file, UserContext.getUserId()));
    }
}
