package com.pay.model;

import org.springframework.stereotype.Component;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Component
@ConfigurationProperties(prefix = "people")
public class People {
	private String name;

	@Override
	public String toString() {
		return "People{" +
				"name='" + name + '\'' +
				'}';
	}
}
