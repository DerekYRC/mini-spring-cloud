package com.github.cloud.openfeign.support;

import feign.Contract;
import feign.MethodMetadata;
import feign.Request;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * feign支持Spring MVC的注解
 *
 * @author derek(易仁川)
 * @date 2022/4/9
 */
public class SpringMvcContract extends Contract.BaseContract {

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        //TODO 解析接口注解
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation annotation, Method method) {
        //解析方法注解
        //解析PostMapping注解
        if (annotation instanceof PostMapping) {
            PostMapping postMapping = (PostMapping) annotation;
            data.template().method(Request.HttpMethod.POST);
            String path = postMapping.value()[0];
            if (!path.startsWith("/") && !data.template().path().endsWith("/")) {
                path = "/" + path;
            }
            data.template().uri(path, true);
        }

        //TODO 解析其他注解
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        //TODO 解析参数
        return true;
    }
}
