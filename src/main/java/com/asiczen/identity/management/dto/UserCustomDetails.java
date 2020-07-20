package com.asiczen.identity.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
