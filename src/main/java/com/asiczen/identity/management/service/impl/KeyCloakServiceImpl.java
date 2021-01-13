package com.asiczen.identity.management.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.asiczen.identity.management.service.MailSenderService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;
import org.springframework.web.client.RestTemplate;

import com.asiczen.identity.management.dto.Credentials;
import com.asiczen.identity.management.dto.UserInfo;
import com.asiczen.identity.management.dto.UserRepresentation;
import com.asiczen.identity.management.exception.AccessisDeniedException;
import com.asiczen.identity.management.exception.InternalServerError;
import com.asiczen.identity.management.exception.ResourceAlreadyExistException;
import com.asiczen.identity.management.exception.ResourceNotFoundException;
import com.asiczen.identity.management.request.UserCredentials;
import com.asiczen.identity.management.request.UserDto;
import com.asiczen.identity.management.response.CurrentUserResponse;
import com.asiczen.identity.management.response.LoginResponse;
import com.asiczen.identity.management.response.RefreshTokenResponse;
import com.asiczen.identity.management.response.UserListResponse;
import com.asiczen.identity.management.service.KeyCloakService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KeyCloakServiceImpl implements KeyCloakService {

	@Value("${keycloak.credentials.secret}")
	private String SECRETKEY;

	@Value("${keycloak.resource}")
	private String CLIENTID;

	@Value("${keycloak.auth-server-url}")
	private String AUTHURL;

	@Value("${keycloak.realm}")
	private String REALM;

	@Value("${app.url.token}")
	private String TOKENURL;

	@Value("${app.url.user}")
	private String USERADDURL;

	@Value("${app.url.userinfo}")
	private String USERINFO;

	@Value("${app.url.logout}")
	private String LOGOUTURL;

	@Value("${app.url.passreset}")
	private String PASSRESET;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	MailSenderService mailSenderService;

	private static final String charSequence = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	@Override
	public LoginResponse getToken(UserCredentials userCredentials) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String username = userCredentials.getUsername();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("client_id", CLIENTID);
		map.add("username", username);
		map.add("password", userCredentials.getPassword());
		map.add("client_secret", SECRETKEY);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<LoginResponse> response = null;

		try {

			response = restTemplate.postForEntity(TOKENURL, request, LoginResponse.class);
			log.trace("--------> {}", response.getBody());
			return response.getBody();

		} catch (Unauthorized ep) {
			log.error("Username or password validation failed.{}" + ep.getLocalizedMessage());
			throw new AccessisDeniedException("invalid username or password");
		} catch (Exception ep) {
			log.error("Some internal server error {}", ep.getLocalizedMessage());
			throw new AccessisDeniedException("Some internal server error occured.-- false one");
		}

	}

	@Override
	public RefreshTokenResponse getByRefreshToken(String refreshToken) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.set("grant_type", "refresh_token");
		map.set("client_id", CLIENTID);
		map.set("refresh_token", refreshToken);
		map.set("client_secret", SECRETKEY);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		try {

			ResponseEntity<RefreshTokenResponse> response = restTemplate.postForEntity(TOKENURL, request,
					RefreshTokenResponse.class);
			log.trace("Refresh is" + response.getBody().getRefresh_token());
			log.trace("Access token" + response.getBody().getAccess_token());
			log.trace("Response body --> {}", response.getBody().toString());

			return response.getBody();

		} catch (Unauthorized ep) {
			log.error("Username or password validation failed.{}" + ep.getLocalizedMessage());
			throw new AccessisDeniedException("invalid username or password");
		} catch (Exception ep) {
			log.error("Some internal server error {}", ep.getLocalizedMessage());
			throw new AccessisDeniedException("Some internal server error occured. -- false");
		}

	}

	@Override
	public String createUserInKeyCloak(UserDto userDTO, String token) {

		String returnResponse = null;

		log.trace("Controller came here");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		log.info(token);

		Map<String, Object> requestBody = new HashMap<>();

		log.info(userDTO.toString());

		requestBody.put("email", userDTO.getUserName());
		requestBody.put("username", userDTO.getUserName());
		requestBody.put("firstName", userDTO.getFirstName());
		requestBody.put("lastName", userDTO.getLastName());
		requestBody.put("enabled", true);

		/* Set Attributes */
		Map<String, String> attributes = new HashMap<>();
		attributes.put("contact", userDTO.getContactNumber());
		attributes.put("orgRefName", userDTO.getOrgRefName());

		requestBody.put("attributes", attributes);

		/* Set password */
		List<Credentials> credentials = new ArrayList<>();

		String password = RandomStringUtils.random(8, charSequence);
		Credentials data = new Credentials("password", password, false);
		credentials.add(data);


		// Need to email user credentials to newly created user.

		requestBody.put("credentials", credentials);

		HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);

		try {

			ResponseEntity<String> response = restTemplate.postForEntity(USERADDURL, request, String.class);
			log.trace(response.getStatusCode().toString());
			log.trace(response.getHeaders().toString());
			log.trace(response.getBody());
			log.trace(response.toString());

			if (response.getStatusCodeValue() == 201) {
				mailSenderService.sendEmail(password, userDTO.getUserName());
				returnResponse = "user created successfully";
			} else {
				returnResponse = "some issue while creating user";
			}

			return returnResponse;

		} catch (HttpClientErrorException.Conflict cep) {
			log.error("User already present in the system with {} user name", userDTO.getUserName());
			log.error(cep.getMessage());
			throw new ResourceAlreadyExistException(
					"user name already registered in system {}" + userDTO.getUserName());

		} catch (HttpClientErrorException.Forbidden ep) {
			log.error("Resource is forbidden for user {}", userDTO.getUserName());
			throw new AccessisDeniedException("Access is denied");
		} catch (HttpClientErrorException.Unauthorized ep) {
			throw new AccessisDeniedException("Access is denied");
		} catch (Exception ep) {
			throw new InternalServerError(ep.getLocalizedMessage());
		}

	}

	@Override
	public void logoutUser(String token) {

		CurrentUserResponse response = getCurrentUserInfo(token);
		String currentUserid = response.getUuid();

		log.trace("Current user: {}", response.getUuid());
		log.trace("Current email id {}", response.getEmailid());
		log.trace("Given user name {}", response.getGiven_name());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		Map<String, String> params = new HashMap<>();
		params.put("id", currentUserid);

		try {
			ResponseEntity<?> responseobj = restTemplate.postForEntity(LOGOUTURL, request, Object.class, params);

			if (responseobj.getStatusCodeValue() != 204) {
				log.error("Error while occured while terminating the session {} ", responseobj.getStatusCodeValue());
			} else {
				log.info("Successfully logged out of the system");
			}

		} catch (Exception ep) {
			log.error("Error while logout. {} ", ep.getMessage());
		}

	}

	/* This method returns current user information . Based on the supplied token */

	@Override
	public CurrentUserResponse getCurrentUserInfo(String token) {

		CurrentUserResponse currentuserinfo = new CurrentUserResponse();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		try {
			ResponseEntity<UserInfo> response = restTemplate.exchange(USERINFO, HttpMethod.GET, request,
					UserInfo.class);

			log.trace("Response Body {}", response.getBody().toString());

			currentuserinfo.setUuid(response.getBody().getSub());
			currentuserinfo.setEmailid(response.getBody().getEmail());
			currentuserinfo.setGiven_name(response.getBody().getGiven_name());

		} catch (HttpClientErrorException.Forbidden ep) {
			log.error("Resource is forbidden");
			throw new AccessisDeniedException("Access is Forbidden");
		} catch (HttpClientErrorException.Unauthorized ep) {
			throw new AccessisDeniedException("Access is denied");
		} catch (Exception ep) {
			throw new InternalServerError(ep.getLocalizedMessage());
		}

		log.trace("User info {} ", currentuserinfo.toString());

		return currentuserinfo;
	}

	/*
	 * Set up a new password for the user. password reset
	 */
	@Override
	public void resetPassword(String token, String newPassword, String userId) {

		String password = RandomStringUtils.random(8, charSequence);
		Credentials newCred = new Credentials("password", password, false);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);

		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("type", "password");
		requestBody.put("value", newCred);
		requestBody.put("temporary", false);

		Map<String, String> params = new HashMap<>();
		params.put("id", userId);

		HttpEntity<Object> request = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<?> responseobj = restTemplate.exchange(PASSRESET, HttpMethod.PUT, request, Object.class,
					params);

			if (responseobj.getStatusCodeValue() != 204) {
				log.error("Error while resetting the password for user ", responseobj.getStatusCodeValue());
			} else {
				log.info("password reset is successful");
			}

		} catch (Exception ep) {
			log.error("Error while logout. {} ", ep.getMessage());
		}

	}

	@Override
	public CurrentUserResponse getUserwithAttributes(String token) {

		CurrentUserResponse response = this.getCurrentUserInfo(token);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		ResponseEntity<UserRepresentation> userinfo = restTemplate.exchange(USERADDURL + "/" + response.getUuid(),
				HttpMethod.GET, request, UserRepresentation.class);

		response.setEmailid(userinfo.getBody().getUsername());
		response.setOrgRefName(userinfo.getBody().getAttributes().getOrgRefName().get(0));

		return response;
	}

	@Override
	public boolean deleteUser(String token, String uid) {

		boolean status = false;

		CurrentUserResponse response = this.getUserwithAttributes(token);

		// response.getOrgRefName()

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		try {

			UserListResponse userdtl = this.getUserByUseId(uid, token);

			if (userdtl.getAttributes() != null) {
				if (!response.getOrgRefName().equalsIgnoreCase(userdtl.getAttributes().getOrgRefName().get(0))) {
					throw new AccessisDeniedException("You don't have access to delete a user");
				}
			}

			// restTemplate.exchange(url, method, requestEntity, responseType)
			ResponseEntity<?> deleteResponse = restTemplate.exchange(USERADDURL + "/" + uid, HttpMethod.DELETE, request,
					Object.class);
			if (deleteResponse.getStatusCodeValue() == 204) {
				log.info("User was deleted successfully.");
				status = true;
			}

		} catch (Unauthorized ex) {
			log.error("Unauthorized access prohibited" + ex.getLocalizedMessage());
			throw new AccessisDeniedException(ex.getLocalizedMessage());
		} catch (Exception ep) {
			log.error("There is some error whiel getting the user list");
			throw new ResourceNotFoundException("No users registered for the organization yet");
		}

		return status;
	}

	// Delete any user -- should be for super admin profile.
	@Override
	public void deleteAnyUser(String token, String uuid) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);
		try {
			ResponseEntity<?> deleteResponse = restTemplate.exchange(USERADDURL + "/" + uuid, HttpMethod.DELETE,
					request, Object.class);
			if (deleteResponse.getStatusCodeValue() == 204) {
				log.info("User was deleted successfully.");
			}

		} catch (Unauthorized ex) {
			log.error("Unauthorized access prohibited" + ex.getLocalizedMessage());
			throw new AccessisDeniedException(ex.getLocalizedMessage());
		} catch (Exception ep) {
			log.error("There is some error whiel getting the user list");
			throw new ResourceNotFoundException("No users registered for the organization yet");
		}

	}

	// Get Organization Specific users
	@Override
	public List<UserListResponse> getAllUsersOrgSpecific(String token) {

		CurrentUserResponse response = this.getUserwithAttributes(token);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		if (response.getOrgRefName() != null) {

			try {

				ResponseEntity<UserListResponse[]> userList = restTemplate.exchange(USERADDURL, HttpMethod.GET, request,
						UserListResponse[].class);
				if (userList.getStatusCodeValue() == 200) {
					return Arrays.stream(userList.getBody()).filter(item -> (item.getAttributes() != null))
							.filter(item -> item.getAttributes().getOrgRefName().get(0)
									.equalsIgnoreCase(response.getOrgRefName()))
							.collect(Collectors.toList());

				} else {
					throw new ResourceNotFoundException("No users registered for the organization yet");
				}

			} catch (Unauthorized ex) {
				log.error("Unauthorized access prohibited" + ex.getLocalizedMessage());
				throw new AccessisDeniedException(ex.getLocalizedMessage());
			} catch (Exception ep) {
				log.error("There is some error whiel getting the user list");
				throw new ResourceNotFoundException("No users registered for the organization yet");
			}

		} else {
			throw new AccessisDeniedException("user not registered/access is denied");
		}

	}

	// This will be used by super admin profile to get all users registered.
	@Override
	public List<UserListResponse> getAllUsers(String token) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		try {

			ResponseEntity<UserListResponse[]> userList = restTemplate.exchange(USERADDURL, HttpMethod.GET, request,
					UserListResponse[].class);
			if (userList.getStatusCodeValue() == 200) {
				return Arrays.stream(userList.getBody()).collect(Collectors.toList());
			} else {
				throw new ResourceNotFoundException("No users registered for the organization yet");
			}

		} catch (Unauthorized ex) {
			log.error("Unauthorized access prohibited" + ex.getLocalizedMessage());
			throw new AccessisDeniedException(ex.getLocalizedMessage());
		} catch (Exception ep) {
			log.error("There is some error whiel getting the user list" + ep.getLocalizedMessage());
			throw new ResourceNotFoundException("No users registered for the organization yet");
		}

	}

	@Override
	public UserListResponse getUserByUseId(String uuid, String token) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", token);
		HttpEntity<Object> request = new HttpEntity<>(headers);

		try {

			ResponseEntity<UserListResponse> response = restTemplate.exchange(USERADDURL, HttpMethod.GET, request,UserListResponse.class);

			if (response.getStatusCodeValue() == 200) {
				return response.getBody();
			} else {
				log.error("There is some error whiel getting the user list. Response code : {}",
						response.getStatusCodeValue());
			}

		} catch (Unauthorized ex) {
			log.error("Unauthorized access prohibited" + ex.getLocalizedMessage());
			throw new AccessisDeniedException(ex.getLocalizedMessage());
		} catch (Exception ep) {
			log.error("There is some error whiel getting the user list" + ep.getLocalizedMessage());
			throw new ResourceNotFoundException("No users registered for the organization yet");
		}
		return null;
	}

}
