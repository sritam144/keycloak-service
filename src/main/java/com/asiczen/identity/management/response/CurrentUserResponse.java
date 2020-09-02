package com.asiczen.identity.management.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CurrentUserResponse {

	String uuid;
	String emailid;
	String given_name;
	String orgRefName;
}
