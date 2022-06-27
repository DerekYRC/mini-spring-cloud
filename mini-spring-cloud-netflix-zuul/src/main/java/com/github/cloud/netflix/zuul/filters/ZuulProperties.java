package com.github.cloud.netflix.zuul.filters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author derek(易仁川)
 * @date 2022/6/23 
 */
@ConfigurationProperties("zuul")
public class ZuulProperties {

	private String servletPath = "/zuul/*";

	private Map<String, ZuulRoute> routes = new LinkedHashMap<>();

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}

	public Map<String, ZuulRoute> getRoutes() {
		return routes;
	}

	public void setRoutes(Map<String, ZuulRoute> routes) {
		this.routes = routes;
	}

	public static class ZuulRoute {

		private String path;

		private String serviceId;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
	}
}