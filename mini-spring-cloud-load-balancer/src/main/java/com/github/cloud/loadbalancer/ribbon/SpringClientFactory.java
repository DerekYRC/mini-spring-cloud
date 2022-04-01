package com.github.cloud.loadbalancer.ribbon;

import com.github.cloud.loadbalancer.ribbon.config.RibbonClientConfiguration;
import org.springframework.cloud.context.named.NamedContextFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * 为每个负载均衡客户端创建一个应用上下文（ApplicationContext）
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
public class SpringClientFactory extends NamedContextFactory<RibbonClientSpecification> {

    private static final String NAMESPACE = "ribbon";

    public SpringClientFactory() {
        super(RibbonClientConfiguration.class, NAMESPACE, "ribbon.client.name");
    }

    @Override
    public <C> C getInstance(String name, Class<C> type) {
        return super.getInstance(name, type);
    }

    @Override
    protected AnnotationConfigApplicationContext getContext(String name) {
        return super.getContext(name);
    }
}
