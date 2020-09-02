package com.asiczen.identity.management.service;

import org.springframework.stereotype.Service;

import com.asiczen.identity.management.dto.UserRepresentation;
import com.asiczen.identity.management.request.UserCredentials;
import com.asiczen.identity.management.request.UserDto;
import com.asiczen.identity.management.response.CurrentUserResponse;
import com.asiczen.identity.management.response.LoginResponse;
import com.asiczen.identity.management.response.RefreshTokenResponse;

@Service
public interface KeyCloakService {

	public LoginResponse getToken(UserCredentials userCredentials);

	public RefreshTokenResponse getByRefreshToken(String refreshToken);

	public String createUserInKeyCloak(UserDto userDTO, String token);

	public void logoutUser(String userId);

	public void resetPassword(String newPassword, String userId);

	// public void test(Principal principal);

	public CurrentUserResponse getCurrentUserInfo(String token);

	public CurrentUserResponse getUserwithAttributes(String token);

}