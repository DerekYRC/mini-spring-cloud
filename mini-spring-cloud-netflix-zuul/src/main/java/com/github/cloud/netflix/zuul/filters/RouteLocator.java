package com.github.cloud.netflix.zuul.filters;

/**
 * 路由定位器
 *
 * @author derek(易仁川)
 * @date 2022/6/28
 */
public interface RouteLocator {

	/**
	 * 获取匹配的路由
	 *
	 * @param path
	 * @return
	 */
	Route getMatchingRoute(String path);
}
