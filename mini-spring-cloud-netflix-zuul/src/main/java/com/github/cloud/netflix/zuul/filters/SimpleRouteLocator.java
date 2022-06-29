package com.github.cloud.netflix.zuul.filters;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import java.util.Map;

/**
 * 路由定位器实现类
 *
 * @author derek(易仁川)
 * @date 2022/6/28
 */
public class SimpleRouteLocator implements RouteLocator {

	private ZuulProperties zuulProperties;

	private PathMatcher pathMatcher = new AntPathMatcher();

	public SimpleRouteLocator(ZuulProperties zuulProperties) {
		this.zuulProperties = zuulProperties;
	}

	@Override
	public Route getMatchingRoute(String path) {
		for (Map.Entry<String, ZuulProperties.ZuulRoute> entry : zuulProperties.getRoutes().entrySet()) {
			ZuulProperties.ZuulRoute zuulRoute = entry.getValue();
			String pattern = zuulRoute.getPath();
			if (pathMatcher.match(pattern, path)) {
				String targetPath = path.substring(pattern.indexOf("*") - 1);
				return new Route(targetPath, zuulRoute.getServiceId());
			}
		}

		return null;
	}
}