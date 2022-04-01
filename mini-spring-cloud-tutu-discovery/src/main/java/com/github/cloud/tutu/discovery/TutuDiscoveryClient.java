package com.github.cloud.tutu.discovery;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cloud.tutu.TutuDiscoveryProperties;
import com.github.cloud.tutu.TutuServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务发现实现类
 *
 * @author derek(易仁川)
 * @date 2022/3/20
 */
public class TutuDiscoveryClient implements DiscoveryClient {
    private static final Logger logger = LoggerFactory.getLogger(TutuDiscoveryClient.class);

    private TutuDiscoveryProperties tutuDiscoveryProperties;

    public TutuDiscoveryClient(TutuDiscoveryProperties tutuDiscoveryProperties) {
        this.tutuDiscoveryProperties = tutuDiscoveryProperties;
    }

    @Override
    public List<ServiceInstance> getInstances(String serviceId) {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", serviceId);

        String response = HttpUtil.get(tutuDiscoveryProperties.getServerAddr() + "/list", param);
        logger.info("query service instance, serviceId: {}, response: {}", serviceId, response);
        return JSON.parseArray(response).stream().map(hostInfo -> {
            TutuServiceInstance serviceInstance = new TutuServiceInstance();
            serviceInstance.setServiceId(serviceId);
            String ip = ((JSONObject) hostInfo).getString("ip");
            Integer port = ((JSONObject) hostInfo).getInteger("port");
            serviceInstance.setHost(ip);
            serviceInstance.setPort(port);
            return serviceInstance;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getServices() {
        String response = HttpUtil.post(tutuDiscoveryProperties.getServerAddr() + "/listServiceNames", new HashMap<>());
        logger.info("query service instance list, response: {}", response);
        return JSON.parseArray(response, String.class);
    }

    @Override
    public String description() {
        return "Spring Cloud Tutu Discovery Client";
    }
}
