package com.github.cloud.netflix.zuul.filters.route;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.*;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * route类型过滤器，使用ribbon负载均衡器进行http请求
 *
 * @author derek(易仁川)
 * @date 2022/6/27
 */
public class RibbonRoutingFilter extends ZuulFilter {
	private static Logger logger = LoggerFactory.getLogger(RibbonRoutingFilter.class);

	private LoadBalancerClient loadBalancerClient;

	public RibbonRoutingFilter(LoadBalancerClient loadBalancerClient) {
		this.loadBalancerClient = loadBalancerClient;
	}

	@Override
	public String filterType() {
		return ROUTE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 10;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		return requestContext.get(SERVICE_ID_KEY) != null;
	}

	@Override
	public Object run() throws ZuulException {
		try {
			RequestContext requestContext = RequestContext.getCurrentContext();
			//使用ribbon的负载均衡能力发起远程调用
			//TODO 简单实现，熔断降级章节再完善
			String serviceId = (String) requestContext.get(SERVICE_ID_KEY);
			ServiceInstance serviceInstance = loadBalancerClient.choose(serviceId);
			if (serviceInstance == null) {
				logger.error("根据serviceId查询不到服务示例，serviceId: {}", serviceId);
				return null;
			}

			String requestURI = (String) requestContext.get(REQUEST_URI_KEY);
			String url = serviceInstance.getUri().toString() + requestURI;
			HttpRequest httpRequest = HttpUtil.createRequest(Method.POST, url);
			HttpResponse httpResponse = httpRequest.execute();

			//将响应报文的状态码和内容写进请求上下文中
			requestContext.setResponseStatusCode(httpResponse.getStatus());
			requestContext.setResponseDataStream(httpResponse.bodyStream());

			return httpResponse;
		} catch (Exception e) {
			rethrowRuntimeException(e);
		}
		return null;
	}
}