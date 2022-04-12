package com.github.cloud.openfeign;

import com.github.cloud.openfeign.ribbon.LoadBalancerFeignClient;
import com.github.cloud.openfeign.support.SpringMvcContract;
import feign.Client;
import feign.Contract;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置feign的核心API
 *
 * @author derek(易仁川)
 * @date 2022/4/9
 */
@Configuration
public class FeignClientsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public Encoder encoder() {
        return new Encoder.Default();
    }

    @Bean
    @ConditionalOnMissingBean
    public Decoder decoder() {
        return new Decoder.Default();
    }

    @Bean
    @ConditionalOnMissingBean
    public Contract contract() {
        return new SpringMvcContract();
    }

    @Bean
    @ConditionalOnMissingBean
    public Client client(LoadBalancerClient loadBalancerClient) {
        return new LoadBalancerFeignClient(loadBalancerClient, new Client.Default(null, null));
    }
}
