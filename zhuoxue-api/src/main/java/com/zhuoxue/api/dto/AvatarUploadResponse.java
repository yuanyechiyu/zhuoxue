package com.zhuoxue.api.dto;

import lombok.Data;

/**
 * 上传头像接口返回的数据。
 *
 * <p>上传成功后，前端拿到这两个字段：</p>
 * <ul>
 *     <li>avatarUrl：可以直接放在 &lt;img src="..."&gt; 里展示的完整地址</li>
 *     <li>objectKey：文件在 MinIO 对象存储中的路径（调试或后续删除时有用）</li>
 * </ul>
 */
@Data
public class AvatarUploadResponse {

    /** 头像的完整访问地址，例如 http://localhost:9000/zhuoxue/avatar/1/xxx.png */
    private String avatarUrl;

    /** 文件在存储桶中的对象键（路径），例如 avatar/1/xxx.png */
    private String objectKey;
}
