package com.aroha.membervalidation.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "validated_members", uniqueConstraints = {
		@UniqueConstraint(columnNames = {"first_name", "last_name", "date_of_birth", "gender"})
})
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "first_name")
	private String firstName;
	@Column(name = "last_name")
	private String lastName;
	 @Column(name = "date_of_birth")
	private LocalDate dateOfBirth;
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
	
	private Double monthlySalary;

}
