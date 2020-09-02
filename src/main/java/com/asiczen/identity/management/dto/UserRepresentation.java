package com.asiczen.identity.management.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserRepresentation {

	@JsonProperty("id")
	private String id;
	@JsonProperty("createdTimestamp")
	private Date createdTimestamp;
	@JsonProperty("username")
	private String username;
	@JsonProperty("enabled")
	private Boolean enabled;
	@JsonProperty("totp")
	private Boolean totp;
	@JsonProperty("emailVerified")
	private Boolean emailVerified;
	@JsonProperty("attributes")
	private Attributes attributes;
}
