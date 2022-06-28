package com.github.cloud.netflix.zuul.filters.support;

/**
 * 过滤器常量类
 *
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public interface FilterConstants {

	String REQUEST_URI_KEY = "requestURI";

	String SERVICE_ID_KEY = "serviceId";

	//过滤器类型常量-----------------------------------
	String PRE_TYPE = "pre";

	String ROUTE_TYPE = "route";

	String POST_TYPE = "post";

	String ERROR_TYPE = "error";
}
