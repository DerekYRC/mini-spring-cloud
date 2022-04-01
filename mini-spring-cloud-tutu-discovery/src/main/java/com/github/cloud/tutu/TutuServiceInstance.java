package com.github.cloud.tutu;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.Map;

/**
 * 服务实例
 *
 * @author derek(易仁川)
 * @date 2022/3/20
 */
public class TutuServiceInstance implements ServiceInstance {

    private String serviceId;

    private String host;

    private int port;

    private boolean secure = false;

    private Map<String, String> metadata;

    public TutuServiceInstance() {
    }

    public TutuServiceInstance(String serviceId, String host, int port) {
        this.serviceId = serviceId;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public URI getUri() {
        return DefaultServiceInstance.getUri(this);
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
