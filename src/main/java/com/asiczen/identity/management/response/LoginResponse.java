package com.asiczen.identity.management.response;

import lombok.Data;

@Data
public class LoginResponse {

	String access_token;
	String refresh_token;
	int expires_in;
	int refresh_expires_in;
	String role;
	String preferredName;
}
