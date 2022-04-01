package com.github.cloud.tutu.registry;

import com.github.cloud.tutu.TutuDiscoveryProperties;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import java.net.URI;
import java.util.Map;

/**
 * tutu服务
 *
 * @author derek(易仁川)
 * @date 2022/3/19
 */
public class TutuRegistration implements Registration {

    private TutuDiscoveryProperties tutuDiscoveryProperties;

    public TutuRegistration(TutuDiscoveryProperties tutuDiscoveryProperties) {
        this.tutuDiscoveryProperties = tutuDiscoveryProperties;
    }

    @Override
    public String getServiceId() {
        return tutuDiscoveryProperties.getService();
    }

    @Override
    public String getHost() {
        return tutuDiscoveryProperties.getIp();
    }

    @Override
    public int getPort() {
        return tutuDiscoveryProperties.getPort();
    }

    public void setPort(int port) {
        this.tutuDiscoveryProperties.setPort(port);
    }

    @Override
    public boolean isSecure() {
        return tutuDiscoveryProperties.isSecure();
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return null;
    }

    public TutuDiscoveryProperties getTutuDiscoveryProperties() {
        return tutuDiscoveryProperties;
    }
}
