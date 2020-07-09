package com.asiczen.identity.management.request;

import java.io.Serializable;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class UserDto implements Serializable {

	private static final long serialVersionUID = -4144288653555563016L;

	@Email
	@NotEmpty(message = "User Name field is mandatory")
	private String userName;

	@NotEmpty(message = "First Name is required/Can't be blank")
	@Size(min = 1, max = 16, message = "First Name should be between 1 to 16 characters")
	private String firstName;

	@NotEmpty(message = "Last Name is required/Can't be blank")
	@Size(min = 1, max = 16, message = "Last Name should be between 1 to 16 characters")
	private String lastName;
}
