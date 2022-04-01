package com.github.cloud.loadbalancer.ribbon;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.cloud.tutu.TutuDiscoveryProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 查询图图服务实例列表
 *
 * @author derek(易仁川)
 * @date 2022/3/13
 */
public class TutuServerList extends AbstractServerList<TutuServer> {
    private static Logger logger = LoggerFactory.getLogger(TutuServerList.class);

    private TutuDiscoveryProperties discoveryProperties;

    private String serviceId;

    public TutuServerList(TutuDiscoveryProperties discoveryProperties) {
        this.discoveryProperties = discoveryProperties;
    }

    /**
     * 查询服务实例列表
     *
     * @return
     */
    @Override
    public List<TutuServer> getInitialListOfServers() {
        return getServer();
    }

    /**
     * 查询服务实例列表
     *
     * @return
     */
    @Override
    public List<TutuServer> getUpdatedListOfServers() {
        return getServer();
    }

    private List<TutuServer> getServer() {
        Map<String, Object> param = new HashMap<>();
        param.put("serviceName", serviceId);

        String response = HttpUtil.get(discoveryProperties.getServerAddr() + "/list", param);
        logger.info("query service instance, serviceId: {}, response: {}", serviceId, response);
        return JSON.parseArray(response).stream().map(hostInfo -> {
            String ip = ((JSONObject) hostInfo).getString("ip");
            Integer port = ((JSONObject) hostInfo).getInteger("port");
            return new TutuServer(ip, port);
        }).collect(Collectors.toList());
    }

    public String getServiceId() {
        return serviceId;
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {
        this.serviceId = iClientConfig.getClientName();
    }
}
