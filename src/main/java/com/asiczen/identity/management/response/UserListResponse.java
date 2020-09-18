package com.asiczen.identity.management.response;

import java.time.LocalDate;

import com.asiczen.identity.management.dto.Attributes;
import com.asiczen.identity.management.dto.UserCustomDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserListResponse extends UserCustomDetails {

	//private LocalDate createdTimestamp;
	private Attributes attributes;
}
