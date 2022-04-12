package com.github.cloud.openfeign;

import org.springframework.cloud.context.named.NamedContextFactory;

/**
 * 为每个feign客户端创建一个应用上下文(ApplicationContext)，隔离每个feign客户端的配置
 *
 * @author derek(易仁川)
 * @date 2022/4/9
 */
public class FeignContext extends NamedContextFactory<FeignClientSpecification> {

    public FeignContext() {
        super(FeignClientsConfiguration.class, "feign", "feign.client.name");
    }
}
