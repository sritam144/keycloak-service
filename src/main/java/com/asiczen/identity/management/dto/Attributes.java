package com.asiczen.identity.management.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Attributes {

	@JsonProperty("orgRefName")
	private List<String> orgRefName = null;

	@JsonProperty("contactNumber")
	private List<String> contactnumber = null;
}
