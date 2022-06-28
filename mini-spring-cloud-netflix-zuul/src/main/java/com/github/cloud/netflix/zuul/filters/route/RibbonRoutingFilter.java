package com.github.cloud.netflix.zuul.filters.route;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;
import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;
import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

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
		RequestContext requestContext = RequestContext.getCurrentContext();
		String serviceId = (String) requestContext.get(SERVICE_ID_KEY);
		String requestURI = (String) requestContext.get(REQUEST_URI_KEY);


		HttpServletRequest request = requestContext.getRequest();

		String method = request.getMethod();

		ServletInputStream inputStream = null;
		try {
			inputStream = request.getInputStream();
		}
		catch (IOException e) {
			logger.error("获取输入流失败", e);
		}

		//TODO 构造请求
		LoadBalancerRequest<ClientHttpResponse> loadBalancerRequest = null;

		ClientHttpResponse response = loadBalancerClient.execute(serviceId, loadBalancerRequest);
		int statusCode = response.getRawStatusCode();
		InputStream responseBody = response.getBody();

		requestContext.setResponseStatusCode(statusCode);
		requestContext.setResponseDataStream(responseBody);

		return response;
	}
}
