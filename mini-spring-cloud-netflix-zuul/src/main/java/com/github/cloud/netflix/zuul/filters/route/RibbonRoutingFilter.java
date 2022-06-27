package com.github.cloud.netflix.zuul.filters.route;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

/**
 * route类型过滤器，使用ribbon负载均衡器进行http请求
 *
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public class RibbonRoutingFilter extends ZuulFilter {

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
		return true;
	}

	@Override
	public Object run() throws ZuulException {
		return null;
	}
}
