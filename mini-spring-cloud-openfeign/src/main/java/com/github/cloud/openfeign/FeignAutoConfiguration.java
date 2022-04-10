package com.github.cloud.openfeign;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author derek(易仁川)
 * @date 2022/4/9
 */
@Configuration
public class FeignAutoConfiguration {

    @Bean
    public FeignContext feignContext() {
        return new FeignContext();
    }
}
