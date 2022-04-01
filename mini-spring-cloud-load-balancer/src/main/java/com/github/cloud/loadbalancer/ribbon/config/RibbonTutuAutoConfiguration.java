package com.github.cloud.loadbalancer.ribbon.config;

import com.github.cloud.loadbalancer.ribbon.RibbonClients;
import org.springframework.context.annotation.Configuration;

/**
 * @author derek(易仁川)
 * @date 2022/3/22
 */
@Configuration
@RibbonClients(defaultConfiguration = TutuRibbonClientConfiguration.class)
public class RibbonTutuAutoConfiguration {
}
