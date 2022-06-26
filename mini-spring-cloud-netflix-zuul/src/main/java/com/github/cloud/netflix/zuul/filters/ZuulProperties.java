package com.github.cloud.netflix.zuul.filters;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author derek(易仁川)
 * @date 2022/6/23 
 */
@ConfigurationProperties("zuul")
public class ZuulProperties {

	private String servletPath = "/zuul/*";

	public String getServletPath() {
		return servletPath;
	}

	public void setServletPath(String servletPath) {
		this.servletPath = servletPath;
	}
}
