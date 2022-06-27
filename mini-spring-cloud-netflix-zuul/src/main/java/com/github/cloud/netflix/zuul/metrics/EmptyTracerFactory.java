package com.github.cloud.netflix.zuul.metrics;

import com.netflix.zuul.monitoring.Tracer;
import com.netflix.zuul.monitoring.TracerFactory;

/**
 * @author derek(易仁川)
 * @date 2022/6/27 
 */
public class EmptyTracerFactory extends TracerFactory {
	@Override
	public Tracer startMicroTracer(String name) {
		return null;
	}
}
