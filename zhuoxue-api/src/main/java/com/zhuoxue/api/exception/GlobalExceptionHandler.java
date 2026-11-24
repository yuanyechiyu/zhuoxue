package com.zhuoxue.api.exception;

import com.zhuoxue.api.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器。
 *
 * <p>@RestControllerAdvice 可以理解为"给所有 Controller 加一层统一的异常兜底"：
 * 任何一个接口抛出异常，都会被这里对应的 @ExceptionHandler 方法接住，
 * 转换成统一的 R 结构返回，而不是给前端返回一大段 500 错误页面。</p>
 *
 * <p>处理顺序：先找最具体的异常类型，找不到再交给 Exception（兜底）。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（我们自己主动抛出的 BizException）。
     *
     * @param e 业务异常
     * @return 携带原始错误码和提示的结果
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBizException(BusinessException e) {
        // 业务异常属于"预期内"的情况，用 warn 级别记录即可
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理上传文件超过大小限制的异常。
     *
     * <p>当上传的文件大于 application.yaml 中 spring.servlet.multipart.max-file-size 配置时，
     * Spring 会在进入 Controller 之前就抛出这个异常。</p>
     *
     * @param e 文件过大异常
     * @return 友好提示
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public R<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("上传文件过大: {}", e.getMessage());
        return R.fail(400, "上传文件过大，请重新选择");
    }

    /**
     * 兜底处理其它所有未预料的异常（例如空指针、数据库连接失败等）。
     *
     * <p>注意：这里不能把真实异常信息返回给前端，否则会暴露系统内部细节，
     * 所以只返回一句"系统繁忙"，真实原因记录到日志里供开发排查。</p>
     *
     * @param e 任意异常
     * @return 通用错误提示
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统繁忙，请稍后再试");
    }
}
