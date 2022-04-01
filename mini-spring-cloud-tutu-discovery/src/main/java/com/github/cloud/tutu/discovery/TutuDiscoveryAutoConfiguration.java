package com.github.cloud.tutu.discovery;

import com.github.cloud.tutu.TutuDiscoveryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author derek(易仁川)
 * @date 2022/3/20
 */
@Configuration
public class TutuDiscoveryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TutuDiscoveryProperties tutuDiscoveryProperties() {
        return new TutuDiscoveryProperties();
    }

    @Bean
    public TutuDiscoveryClient tutuDiscoveryClient(TutuDiscoveryProperties tutuDiscoveryProperties) {
        return new TutuDiscoveryClient(tutuDiscoveryProperties);
    }
}
