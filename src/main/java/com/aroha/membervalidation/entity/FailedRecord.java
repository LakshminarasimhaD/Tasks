package com.aroha.membervalidation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "failed_records")
public class FailedRecord {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Integer recordNumber;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	@Column(name = "date_of_birth")
	private String dateOfBirth;
	@Column(name = "gender")
	private String gender;
	
	private String education;
	
	private String houseNumber;
	
	private String address1;
	
	private String address2;
	
	private String city;
	
	private String pincode;
	
	private String mobileNumber;
	
	private String company;
	
	private String monthlySalary;
	
	@Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
}


