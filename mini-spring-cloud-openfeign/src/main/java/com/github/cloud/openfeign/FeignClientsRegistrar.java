package com.github.cloud.openfeign;

import cn.hutool.core.util.ClassUtil;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Set;

/**
 * 往bean容器中注册Feign客户端
 *
 * @author derek(易仁川)
 * @date 2022/4/7
 */
public class FeignClientsRegistrar implements ImportBeanDefinitionRegistrar {

    /**
     * 往bean容器中注册Feign客户端
     *
     * @param importingClassMetadata
     * @param registry
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        //为FeignClient注解修饰的接口生成代理bean即Feign客户端，并注册到bean容器
        String packageName = ClassUtils.getPackageName(importingClassMetadata.getClassName());
        //扫描所有被FeignClient注解修饰的接口
        Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(packageName, FeignClient.class);
        for (Class<?> clazz : classes) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            //使用FeignClientFactoryBean生成Feign客户端
            beanDefinition.setBeanClass(FeignClientFactoryBean.class);
            String clientName = clazz.getAnnotation(FeignClient.class).value();
            beanDefinition.getPropertyValues().addPropertyValue("contextId", clientName);
            beanDefinition.getPropertyValues().addPropertyValue("type", clazz);

            //将Feign客户端注册进bean容器
            String beanName = clazz.getName();
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }
}
