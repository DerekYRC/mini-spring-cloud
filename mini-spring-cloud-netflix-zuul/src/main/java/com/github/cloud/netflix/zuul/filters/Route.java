package com.github.cloud.netflix.zuul.filters;

/**
 * 路由
 * @author derek(易仁川)
 * @date 2022/6/28 
 */
public class Route {

	private String path;

	private String location;

	public Route(String path, String location) {
		this.path = path;
		this.location = location;
	}


	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
