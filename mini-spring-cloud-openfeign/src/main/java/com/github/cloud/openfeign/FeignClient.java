package com.github.cloud.openfeign;

/**
 * Feign客户端注解
 *
 * @author derek(易仁川)
 * @date 2022/4/7
 */
public @interface FeignClient {

    /**
     * 服务提供者应用名称
     *
     * @return
     */
    String value();
}
