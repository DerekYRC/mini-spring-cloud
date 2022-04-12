package com.github.cloud.openfeign;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Target.HardCodedTarget;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 生成Feign客户端的FactoryBean
 *
 * @author derek(易仁川)
 * @date 2022/4/7
 */
public class FeignClientFactoryBean implements FactoryBean<Object>, ApplicationContextAware {

    private String contextId;

    private Class<?> type;

    private ApplicationContext applicationContext;

    @Override
    public Object getObject() throws Exception {
        FeignContext feignContext = applicationContext.getBean(FeignContext.class);
        Encoder encoder = feignContext.getInstance(contextId, Encoder.class);
        Decoder decoder = feignContext.getInstance(contextId, Decoder.class);
        Contract contract = feignContext.getInstance(contextId, Contract.class);
        Client client = feignContext.getInstance(contextId, Client.class);

        return Feign.builder()
                .encoder(encoder)
                .decoder(decoder)
                .contract(contract)
                .client(client)
                .target(new HardCodedTarget<>(type, contextId, "http://" + contextId));
    }

    @Override
    public Class<?> getObjectType() {
        return this.type;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }
}
