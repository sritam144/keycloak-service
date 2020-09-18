package com.asiczen.identity.management.response;

import lombok.Data;

@Data
public class LoginResponse {

	private String access_token;
	private String refresh_token;
	private int expires_in;
	private int refresh_expires_in;
}
