package com.asiczen.identity.management.request;

import java.io.Serializable;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserCredentials implements Serializable{

	private static final long serialVersionUID = -7233153334484992795L;

	@NotEmpty(message = "password is required/Can't be blank")
	private String password;
	
	@NotEmpty(message = "username is required/Can't be blank")
	@Size(min = 3, max = 50, message = "User name should be between 3 to 50 characters")
	private String username;
}
