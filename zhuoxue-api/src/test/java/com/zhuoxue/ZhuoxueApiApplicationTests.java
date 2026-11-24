package com.zhuoxue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Spring Boot 自带的上下文加载测试。
 *
 * <p>@SpringBootTest 会真正启动一次 Spring 容器（但不会启动对外服务的端口）。
 * 如果各个 Bean 配置正确，这个测试就能通过；如果某个 Bean 注入失败，
 * 运行它就会直接报错，是检查"项目配置有没有问题"最简单的方式。</p>
 *
 * <p>运行前提：数据库、Redis 等外部依赖需要可用（因为启动时会去连接）。</p>
 */
@SpringBootTest
class ZhuoxueApiApplicationTests {

    /**
     * 空方法即可：只要上下文能成功加载，测试就算通过。
     */
    @Test
    void contextLoads() {
    }

}
