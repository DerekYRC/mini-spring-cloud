package com.github.cloud.loadbalancer.ribbon.config;

import com.github.cloud.loadbalancer.ribbon.TutuServerList;
import com.github.cloud.tutu.TutuDiscoveryProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义ribbon组件
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
@Configuration
public class TutuRibbonClientConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(IClientConfig config,
                                          TutuDiscoveryProperties discoveryProperties) {
        TutuServerList serverList = new TutuServerList(discoveryProperties);
        serverList.initWithNiwsConfig(config);
        return serverList;
    }
}
