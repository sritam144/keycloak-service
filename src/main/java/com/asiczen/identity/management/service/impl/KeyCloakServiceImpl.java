package com.asiczen.identity.management.service.impl;

import java.util.HashMap;
import java.util.Map;

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

import com.asiczen.identity.management.dto.UserInfo;
import com.asiczen.identity.management.exception.AccessisDeniedException;
import com.asiczen.identity.management.exception.InternalServerError;
import com.asiczen.identity.management.exception.ResourceAlreadyExistException;
import com.asiczen.identity.management.request.UserCredentials;
import com.asiczen.identity.management.request.UserDto;
import com.asiczen.identity.management.response.CurrentUserResponse;
import com.asiczen.identity.management.response.LoginResponse;
import com.asiczen.identity.management.response.RefreshTokenResponse;
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

	@Override
	public LoginResponse getToken(UserCredentials userCredentials) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		String username = userCredentials.getUsername();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.add("grant_type", "password");
		map.add("client_id", CLIENTID);
		map.add("username", username);
		map.add("password", userCredentials.getPassword());
		map.add("client_secret", SECRETKEY);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

		ResponseEntity<LoginResponse> response = null;

		try {

			response = restTemplate.postForEntity(TOKENURL, request, LoginResponse.class);

			log.info("--------> {}", response.getBody());

			LoginResponse loginResponse = new LoginResponse();

			loginResponse.setAccess_token(response.getBody().getAccess_token());
			loginResponse.setRefresh_token(response.getBody().getRefresh_token());
			loginResponse.setRefresh_expires_in(response.getBody().getRefresh_expires_in());
			loginResponse.setExpires_in(response.getBody().getExpires_in());

			/* Test Code */

			return loginResponse;

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

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
		map.set("grant_type", "refresh_token");
		map.set("client_id", CLIENTID);
		map.set("refresh_token", refreshToken);
		map.set("client_secret", SECRETKEY);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

		try {

			ResponseEntity<RefreshTokenResponse> response = restTemplate.postForEntity(TOKENURL, request,
					RefreshTokenResponse.class);
			log.info("Refresh is" + response.getBody().getRefresh_token());
			log.info("Access token" + response.getBody().getAccess_token());
			log.info("Response body --> {}", response.getBody().toString());

			RefreshTokenResponse tokenResponse = new RefreshTokenResponse();

			tokenResponse.setAccess_token(response.getBody().getAccess_token());
			tokenResponse.setExpires_in(response.getBody().getExpires_in());
			tokenResponse.setRefresh_token(response.getBody().getRefresh_token());
			tokenResponse.setRefresh_expires_in(response.getBody().getRefresh_expires_in());

			return tokenResponse;

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

		Map<String, Object> requestBody = new HashMap<String, Object>();

		log.info(userDTO.toString());

		requestBody.put("email", userDTO.getUserName());
		requestBody.put("username", userDTO.getUserName());
		requestBody.put("firstName", userDTO.getFirstName());
		requestBody.put("lastName", userDTO.getLastName());
		requestBody.put("enabled", true);

		HttpEntity<Object> request = new HttpEntity<Object>(requestBody, headers);

		try {

			ResponseEntity<String> response = restTemplate.postForEntity(USERADDURL, request, String.class);
			log.debug(response.getStatusCode().toString());
			log.debug(response.getHeaders().toString());
			log.debug(response.getBody());
			log.debug(response.toString());

			if (response.getStatusCodeValue() == 201) {
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

		Map<String, String> params = new HashMap<String, String>();
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

//	@Override
//	public void resetPassword(String newPassword, String userId) {
//
//		UsersResource userResource = getKeycloakUserResource();
//
//		CredentialRepresentation passwordCred = new CredentialRepresentation();
//		passwordCred.setTemporary(false);
//		passwordCred.setType(CredentialRepresentation.PASSWORD);
//		passwordCred.setValue(newPassword.toString().trim());
//
//		// Set password credential
//		try {
//			userResource.get(userId).resetPassword(passwordCred);
//		} catch (Exception ep) {
//			log.error("Error in reseting password. Please try again {}" + ep.getLocalizedMessage());
//		}
//
//	}

//	public UsersResource getKeycloakUserResource() {
//
//		Keycloak kc = KeycloakBuilder.builder().serverUrl(AUTHURL).realm(REALM).username("sanjeet215@gmail.com")
//				.password("password").clientId("CLIENTID")
//				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
//
//		RealmResource realmResource = kc.realm(REALM);
//		UsersResource userRessource = realmResource.users();
//
//		return userRessource;
//	}

//	private RealmResource getRealmResource() {
//
//		Keycloak kc = KeycloakBuilder.builder().serverUrl(AUTHURL).realm(REALM).username("sanjeet215@gmail.com")
//				.password("password").clientId("CLIENTID")
//				.resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
//
//		RealmResource realmResource = kc.realm(REALM);
//
//		return realmResource;
//
//	}

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

			currentuserinfo.setUuid(response.getBody().getSub());
			currentuserinfo.setEmailid(response.getBody().getEmail());
			currentuserinfo.setGiven_name(response.getBody().getGiven_name());

		} catch (HttpClientErrorException.Forbidden ep) {
			log.error("Resource is forbidden");
			throw new AccessisDeniedException("Access is denied");
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
	public void resetPassword(String newPassword, String userId) {
		// PASSRESET
	}

}
