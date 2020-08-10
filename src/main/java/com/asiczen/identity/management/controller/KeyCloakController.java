package com.asiczen.identity.management.controller;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.asiczen.identity.management.request.UserCredentials;
import com.asiczen.identity.management.request.UserDto;
import com.asiczen.identity.management.service.KeyCloakService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class KeyCloakController {

	@Autowired
	KeyCloakService keyClockService;

	@PostMapping(value = "/login")
	public ResponseEntity<?> getTokenUsingCredentials(@Valid @RequestBody UserCredentials userCredentials) {

		log.trace("User login process started ..");
		log.trace("Connecting keycloak service to get access token and refersh token ..");
		return new ResponseEntity<>(keyClockService.getToken(userCredentials), HttpStatus.OK);
	}

	@GetMapping(value = "/refreshtoken")
	public ResponseEntity<?> getTokenUsingRefreshToken(@Valid @RequestParam String refreshToken) {

		return new ResponseEntity<>(keyClockService.getByRefreshToken(refreshToken), HttpStatus.OK);

	}

	@PostMapping(value = "/createuser")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<?> createUserinKeyCloak(@Valid @RequestBody UserDto userDto,
			@RequestHeader String Authorization) {

		log.trace("Token is --->" + Authorization);
		log.trace("Request object --> {}", userDto.toString());
		log.info("Request object --> {}", userDto.toString());

		return new ResponseEntity<>(keyClockService.createUserInKeyCloak(userDto, Authorization), HttpStatus.CREATED);
	}

//	@PutMapping(value = "/updateuser")
//	@RolesAllowed({"admin"})
//	public ResponseEntity<?> updateKeycloakUser() {
//		
//	}

	@GetMapping(value = "/currentuser")
	public ResponseEntity<?> getCurrentUser(@RequestHeader String Authorization) {
		return new ResponseEntity<>(keyClockService.getCurrentUserInfo(Authorization), HttpStatus.OK);
	}

	@GetMapping(value = "/logout")
	public ResponseEntity<?> logoutUser(@RequestHeader String Authorization) {

		log.trace("Bearer token: {} ", Authorization);

		keyClockService.logoutUser(Authorization);
		return new ResponseEntity<>("Hi!, you have logged out successfully!", HttpStatus.OK);
	}

	@GetMapping(value = "/update/password")
	public ResponseEntity<?> updatePassword(HttpServletRequest request, String newPassword) {

		request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		AccessToken token = ((KeycloakPrincipal<?>) request.getUserPrincipal()).getKeycloakSecurityContext().getToken();
		String userId = token.getSubject();

		keyClockService.resetPassword(newPassword, userId);

		return new ResponseEntity<>("Hi!, your password has been successfully updated!", HttpStatus.OK);

	}

}
