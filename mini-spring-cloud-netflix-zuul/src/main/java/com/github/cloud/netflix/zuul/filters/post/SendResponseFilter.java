package com.github.cloud.netflix.zuul.filters.post;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;

/**
 * post类型过滤器，向客户端输出响应报文
 *
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public class SendResponseFilter extends ZuulFilter {

	@Override
	public String filterType() {
		return POST_TYPE;
	}

	@Override
	public int filterOrder() {
		return 1000;
	}

	@Override
	public boolean shouldFilter() {
		RequestContext requestContext = RequestContext.getCurrentContext();
		return requestContext.getResponseDataStream() != null;
	}

	@Override
	public Object run() throws ZuulException {
		RequestContext requestContext = RequestContext.getCurrentContext();
		HttpServletResponse servletResponse = requestContext.getResponse();
		if (servletResponse.getCharacterEncoding() == null) {
			servletResponse.setCharacterEncoding("UTF-8");
		}

		OutputStream outStream = servletResponse.getOutputStream();


		return null;
	}
}
