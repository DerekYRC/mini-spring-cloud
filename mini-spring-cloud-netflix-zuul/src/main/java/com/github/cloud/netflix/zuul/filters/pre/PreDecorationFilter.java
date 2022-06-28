package com.github.cloud.netflix.zuul.filters.pre;

import com.github.cloud.netflix.zuul.filters.Route;
import com.github.cloud.netflix.zuul.filters.RouteLocator;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.REQUEST_URI_KEY;
import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.SERVICE_ID_KEY;

/**
 * pre类型过滤器，根据RouteLocator来进行路由规则的匹配
 *
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public class PreDecorationFilter extends ZuulFilter {
	private static Logger logger = LoggerFactory.getLogger(PreDecorationFilter.class);

	private RouteLocator routeLocator;

	public PreDecorationFilter(RouteLocator routeLocator) {
		this.routeLocator = routeLocator;
	}

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
		RequestContext requestContext = RequestContext.getCurrentContext();
		String requestURI = requestContext.getRequest().getRequestURI();
		//获取匹配的路由
		Route route = routeLocator.getMatchingRoute(requestURI);
		if (route != null) {
			requestContext.put(REQUEST_URI_KEY, route.getPath());
			requestContext.set(SERVICE_ID_KEY, route.getLocation());
		}
		else {
			logger.error("获取不到匹配的路由, requestURI: {}", requestContext);
		}

		return null;
	}
}
