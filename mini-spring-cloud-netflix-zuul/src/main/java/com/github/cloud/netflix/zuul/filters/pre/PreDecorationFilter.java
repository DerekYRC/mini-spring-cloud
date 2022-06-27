package com.github.cloud.netflix.zuul.filters.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.exception.ZuulException;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * pre类型过滤器，根据RouteLocator来进行路由规则的匹配
 *
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public class PreDecorationFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return PRE_TYPE;
	}

	@Override
	public int filterOrder() {
		return 5;
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
