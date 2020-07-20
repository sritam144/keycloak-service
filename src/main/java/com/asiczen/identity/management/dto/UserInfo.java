package com.asiczen.identity.management.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserInfo {

	private String sub;
	private String name;
	private String preferred_username;
	private String given_name;
	private String family_name;
	private String email;
}
