package com.github.cloud.netflix.zuul.metrics;

import com.netflix.zuul.monitoring.CounterFactory;

/**
 * @author derek(易仁川)
 * @date 2022/6/27
 */
public class EmptyCounterFactory extends CounterFactory {
	@Override
	public void increment(String name) {
	}
}
