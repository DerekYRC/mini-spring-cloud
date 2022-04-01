package com.github.cloud.tutu.registry;

import cn.hutool.http.HttpUtil;
import com.github.cloud.tutu.TutuDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 具体的服务注册实现类
 *
 * @author derek(易仁川)
 * @date 2022/3/19
 */
public class TutuServiceRegistry implements ServiceRegistry<Registration> {
    private static final Logger logger = LoggerFactory.getLogger(TutuServiceRegistry.class);

    private TutuDiscoveryProperties tutuDiscoveryProperties;

    public TutuServiceRegistry(TutuDiscoveryProperties tutuDiscoveryProperties) {
        this.tutuDiscoveryProperties = tutuDiscoveryProperties;
    }

    /**
     * 注册服务实例
     *
     * @param registration
     */
    @Override
    public void register(Registration registration) {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", tutuDiscoveryProperties.getService());
        param.put("ip", tutuDiscoveryProperties.getIp());
        param.put("port", tutuDiscoveryProperties.getPort());

        String result = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/register", param);
        if (Boolean.parseBoolean(result)) {
            logger.info("register service successfully, serviceName: {}, ip: {}, port: {}",
                    tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
        } else {
            logger.error("register service failed, serviceName: {}, ip: {}, port: {}",
                    tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
            throw new RuntimeException("register service failed, serviceName");
        }
    }

    /**
     * 注销服务实例
     *
     * @param registration
     */
    @Override
    public void deregister(Registration registration) {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", tutuDiscoveryProperties.getService());
        param.put("ip", tutuDiscoveryProperties.getIp());
        param.put("port", tutuDiscoveryProperties.getPort());

        String result = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/deregister", param);
        if (Boolean.parseBoolean(result)) {
            logger.info("de-register service successfully, serviceName: {}, ip: {}, port: {}",
                    tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
        } else {
            logger.warn("de-register service failed, serviceName: {}, ip: {}, port: {}",
                    tutuDiscoveryProperties.getService(), tutuDiscoveryProperties.getIp(), tutuDiscoveryProperties.getPort());
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void setStatus(Registration registration, String status) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getStatus(Registration registration) {
        return null;
    }
}
