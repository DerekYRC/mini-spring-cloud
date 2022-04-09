package com.github.cloud.openfeign;

import java.lang.annotation.*;

/**
 * Feign客户端注解
 *
 * @author derek(易仁川)
 * @date 2022/4/7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FeignClient {

    /**
     * 服务提供者应用名称
     *
     * @return
     */
    String value();
}
