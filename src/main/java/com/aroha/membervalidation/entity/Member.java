package com.aroha.membervalidation.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	@EmbeddedId
	private MemberId uniqId;
	private String memberId;
	private String education;
	private String houseNumber;
	private String address1;
	private String address2;
	private String pinCode;
	private String city;
	private String mobile;
	private String company;
    private String monthlySalary; 
}