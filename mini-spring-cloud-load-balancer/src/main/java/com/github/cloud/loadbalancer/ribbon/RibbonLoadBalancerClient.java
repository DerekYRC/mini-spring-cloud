package com.github.cloud.loadbalancer.ribbon;

import cn.hutool.core.util.StrUtil;
import com.github.cloud.tutu.TutuServiceInstance;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.client.loadbalancer.Request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * ribbon负载均衡客户端
 *
 * @author derek(易仁川)
 * @date 2022/3/22
 */
public class RibbonLoadBalancerClient implements LoadBalancerClient {

    private SpringClientFactory clientFactory;

    public RibbonLoadBalancerClient(SpringClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    /**
     * 选择服务实例
     *
     * @param serviceId
     * @return
     */
    @Override
    public ServiceInstance choose(String serviceId) {
        return choose(serviceId, null);
    }

    /**
     * 选择服务实例
     *
     * @param serviceId
     * @param request
     * @param <T>
     * @return
     */
    @Override
    public <T> ServiceInstance choose(String serviceId, Request<T> request) {
        ILoadBalancer loadBalancer = clientFactory.getInstance(serviceId, ILoadBalancer.class);
        Server server = loadBalancer.chooseServer("default");
        if (server != null) {
            return new TutuServiceInstance(serviceId, server.getHost(), server.getPort());
        }

        return null;
    }

    /**
     * 重建请求URI，将服务名称替换为服务实例的IP:端口
     *
     * @param server
     * @param original
     * @return
     */
    @Override
    public URI reconstructURI(ServiceInstance server, URI original) {
        try {
            //将服务名称替换为服务实例的IP:端口，例如http://provider-application/echo被重建为http://192.168.100.1:8888/echo
            StringBuilder sb = new StringBuilder();
            sb.append(original.getScheme()).append("://");
            sb.append(server.getHost());
            sb.append(":").append(server.getPort());
            sb.append(original.getRawPath());
            if (StrUtil.isNotEmpty(original.getRawQuery())) {
                sb.append("?").append(original.getRawQuery());
            }
            URI newURI = new URI(sb.toString());
            return newURI;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理http请求
     *
     * @param serviceId
     * @param request
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T execute(String serviceId, LoadBalancerRequest<T> request) throws IOException {
        ServiceInstance serviceInstance = choose(serviceId);
        return execute(serviceId, serviceInstance, request);
    }

    /**
     * 处理http请求
     *
     * @param serviceId
     * @param serviceInstance
     * @param request
     * @param <T>
     * @return
     * @throws IOException
     */
    @Override
    public <T> T execute(String serviceId, ServiceInstance serviceInstance, LoadBalancerRequest<T> request) throws IOException {
        try {
            return request.apply(serviceInstance);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
