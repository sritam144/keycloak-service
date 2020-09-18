package com.asiczen.identity.management.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCustomDetails {

	private String id;
	private String username;
	private boolean enabled;
	private boolean totp;
	private boolean emailVerified;
	private String firstName;
	private String lastName;
	private String email;

}
