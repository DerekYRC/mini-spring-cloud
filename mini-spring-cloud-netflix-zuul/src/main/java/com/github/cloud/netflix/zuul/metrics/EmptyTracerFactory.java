package com.github.cloud.netflix.zuul.metrics;

import com.netflix.zuul.monitoring.Tracer;
import com.netflix.zuul.monitoring.TracerFactory;

/**
 * @author derek(易仁川)
 * @date 2022/6/27
 */
public class EmptyTracerFactory extends TracerFactory {

	private final EmptyTracer emptyTracer = new EmptyTracer();

	@Override
	public Tracer startMicroTracer(String name) {
		return emptyTracer;
	}

	private static final class EmptyTracer implements Tracer {

		@Override
		public void setName(String name) {
		}

		@Override
		public void stopAndLog() {
		}
	}
}