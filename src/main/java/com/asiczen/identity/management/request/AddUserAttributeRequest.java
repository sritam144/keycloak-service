package com.asiczen.identity.management.request;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AddUserAttributeRequest implements Serializable {

	private static final long serialVersionUID = -1429494602574167246L;
	private String contactNumber;
	private String gender;
	private String employeeid;
}
