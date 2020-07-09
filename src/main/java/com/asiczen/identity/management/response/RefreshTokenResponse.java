package com.asiczen.identity.management.response;

import lombok.Data;

@Data
public class RefreshTokenResponse {

	String access_token;
	String expires_in;
	String refresh_token;
	String refresh_expires_in;
}
