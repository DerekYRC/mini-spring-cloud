package com.github.cloud.loadbalancer.ribbon;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * 处理注解RibbonClients的配置类
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
public class RibbonClientConfigurationRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(RibbonClients.class.getName(), true);

        if (attrs != null && attrs.containsKey("defaultConfiguration")) {
            String name = "default." + metadata.getClassName();
            registerClientConfiguration(registry, name, attrs.get("defaultConfiguration"));
        }
    }

    private void registerClientConfiguration(BeanDefinitionRegistry registry, Object name,
                                             Object configuration) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(RibbonClientSpecification.class);
        builder.addConstructorArgValue(name);
        builder.addConstructorArgValue(configuration);
        registry.registerBeanDefinition(name + ".RibbonClientSpecification",
                builder.getBeanDefinition());
    }
}
