package io.securin.maven.plugin.model;

import lombok.Data;

@Data
public class Response<T> {
	private boolean success = false;
	private T resp;
	private int responseCode;

}
