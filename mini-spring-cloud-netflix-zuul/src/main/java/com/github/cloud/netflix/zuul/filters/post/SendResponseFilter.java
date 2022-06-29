package com.github.cloud.netflix.zuul.filters.post;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

import static com.github.cloud.netflix.zuul.filters.support.FilterConstants.POST_TYPE;
import static org.springframework.util.ReflectionUtils.rethrowRuntimeException;

/**
 * post类型过滤器，向客户端输出响应报文
 *
 * @author derek(易仁川)
 * @date 2022/6/27
 */
public class SendResponseFilter extends ZuulFilter {
	private static Logger logger = LoggerFactory.getLogger(SendResponseFilter.class);

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
		return RequestContext.getCurrentContext()
				.getResponseDataStream() != null;
	}

	@Override
	public Object run() throws ZuulException {
		//向客户端输出响应报文
		RequestContext requestContext = RequestContext.getCurrentContext();
		InputStream inputStream = requestContext.getResponseDataStream();
		try {
			HttpServletResponse servletResponse = requestContext.getResponse();
			servletResponse.setCharacterEncoding("UTF-8");

			OutputStream outStream = servletResponse.getOutputStream();
			StreamUtils.copy(inputStream, outStream);
		} catch (Exception e) {
			rethrowRuntimeException(e);
		} finally {
			//关闭输入输出流
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					logger.error("关闭输入流失败", e);
				}
			}

			//Servlet容器会自动关闭输出流
		}
		return null;
	}
}