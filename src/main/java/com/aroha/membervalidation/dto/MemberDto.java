package com.aroha.membervalidation.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberDto {
	private String memberId;
	private String firstName;
	private String lastName;
	private LocalDate dob;
	private String gender;
	private String city;

}