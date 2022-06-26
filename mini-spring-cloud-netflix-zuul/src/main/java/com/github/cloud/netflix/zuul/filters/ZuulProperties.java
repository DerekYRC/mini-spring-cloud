package com.github.cloud.netflix.zuul.filters;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author derek(易仁川)
 * @date 2022/6/23 
 */
@ConfigurationProperties("zuul")
public class ZuulProperties {

	private String servletPath = "/zuul";


	public String getServletPattern() {
		String path = this.servletPath;
		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		if (!path.contains("*")) {
			path = path.endsWith("/") ? (path + "*") : (path + "/*");
		}
		return path;
	}

	String getServletPath() {
		return servletPath;
	}

	void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}
}
