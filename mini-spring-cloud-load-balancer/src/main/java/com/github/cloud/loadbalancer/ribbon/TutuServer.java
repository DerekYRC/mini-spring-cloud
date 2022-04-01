package com.github.cloud.loadbalancer.ribbon;

import com.netflix.loadbalancer.Server;

/**
 * 图图服务实例
 *
 * @author derek(易仁川)
 * @date 2022/3/13
 */
public class TutuServer extends Server {

    public TutuServer(String host, int port) {
        super(host, port);
    }
}
