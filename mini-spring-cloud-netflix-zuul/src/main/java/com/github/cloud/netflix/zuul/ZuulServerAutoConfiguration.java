package com.github.cloud.netflix.zuul;

import com.github.cloud.netflix.zuul.filters.ZuulProperties;
import com.netflix.zuul.http.ZuulServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author derek(易仁川)
 * @date 2022/6/23 
 */
@Configuration
@EnableConfigurationProperties({ZuulProperties.class})
public class ZuulServerAutoConfiguration {

	@Autowired
	protected ZuulProperties zuulProperties;

	/**
	 * 注册ZuulServlet
	 * @return
	 */
	@Bean
	public ServletRegistrationBean zuulServlet() {
		return new ServletRegistrationBean<>(new ZuulServlet(), zuulProperties.getServletPattern());
	}
}
