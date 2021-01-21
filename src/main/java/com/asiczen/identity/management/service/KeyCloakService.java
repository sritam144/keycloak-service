package com.asiczen.identity.management.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.asiczen.identity.management.request.UserCredentials;
import com.asiczen.identity.management.request.UserDto;
import com.asiczen.identity.management.response.CurrentUserResponse;
import com.asiczen.identity.management.response.LoginResponse;
import com.asiczen.identity.management.response.RefreshTokenResponse;
import com.asiczen.identity.management.response.UserListResponse;

@Service
public interface KeyCloakService {

	public LoginResponse getToken(UserCredentials userCredentials);

	public RefreshTokenResponse getByRefreshToken(String refreshToken);

	public String createUserInKeyCloak(UserDto userDTO, String token);

	public void logoutUser(String userId);

	public void resetPassword(String token, String newPassword, String userId);

	// public void test(Principal principal);

	public CurrentUserResponse getCurrentUserInfo(String token);

	public CurrentUserResponse getUserwithAttributes(String token);

	public boolean deleteUser(String token, String uuid);

	public void deleteAnyUser(String token, String uuid);

	public List<UserListResponse> getAllUsersOrgSpecific(String token);

	public UserListResponse getUserByEmailAddressOrgSpecific(String token,String emailid);

	public List<UserListResponse> getAllUsers(String token);

	public UserListResponse getUserByUseId(String uuid, String token);

}