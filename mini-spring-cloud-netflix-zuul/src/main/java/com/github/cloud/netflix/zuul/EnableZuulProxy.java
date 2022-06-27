package com.github.cloud.netflix.zuul;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

/**
 * 启用zuul网关
 * @author derek(易仁川)
 * @date 2022/6/23 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ZuulServerAutoConfiguration.class)
public @interface EnableZuulProxy {
}
