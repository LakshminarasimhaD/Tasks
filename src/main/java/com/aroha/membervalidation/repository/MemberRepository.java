package com.aroha.membervalidation.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aroha.membervalidation.dto.MemberDto;
import com.aroha.membervalidation.dto.MemberSalaryDto;
import com.aroha.membervalidation.entity.Member;
import com.aroha.membervalidation.entity.MemberId;

import jakarta.transaction.Transactional;

@Repository
public interface MemberRepository extends JpaRepository<Member, MemberId> {

	List<Member> findByUniqId_FirstName(String firstName);

	List<Member> findByUniqId_LastName(String lastName);

	
	//Inserting records by using native query
	@Modifying
	@Transactional
	@Query(value = """
			INSERT IGNORE INTO members (
			    first_name, last_name, dob, gender,
			    member_id, education, house_number,
			    address1, address2, pin_code,
			    city, mobile, company, monthly_salary
			) VALUES (
			    :firstName, :lastName, :dob, :gender,
			    :memberId, :education, :houseNumber,
			    :address1, :address2, :pinCode,
			    :city, :mobile, :company, :monthlySalary
			)""", nativeQuery = true)
	void insertMember(@Param("firstName") String firstName, @Param("lastName") String lastName,
			@Param("dob") LocalDate dob, @Param("gender") String gender, @Param("memberId") String memberId,
			@Param("education") String education, @Param("houseNumber") String houseNumber,
			@Param("address1") String address1, @Param("address2") String address2, @Param("pinCode") String pinCode,
			@Param("city") String city, @Param("mobile") String mobile, @Param("company") String company,
			@Param("monthlySalary") String monthlySalary);

	// Selecting specific fields with dynamic parameters
//	@Query("SELECT NEW com.aroha.membervalidation.dto.MemberDto(" + "m.memberId, m.uniqId.firstName, m.uniqId.lastName, "
//			+ "m.uniqId.dob, m.uniqId.gender, m.city) " + "FROM Member m "
//			+ "WHERE m.uniqId.firstName LIKE :firstNamePattern " + "AND m.uniqId.lastName LIKE :lastNamePattern")
//List<MemberDto> findMembersByNamePattern(@Param("firstNamePattern") String firstNamePattern,
//			@Param("lastNamePattern") String lastNamePattern);
	
	
//	@Query("SELECT NEW com.example.demo.dto.MemberDto(" + "m.memberId, m.uniqId.firstName, m.uniqId.lastName, "
//			+ "m.uniqId.dob, m.uniqId.gender, m.city) " + "FROM Member m "
//			+ "WHERE m.uniqId.firstName LIKE :firstNamePattern " + "AND m.uniqId.lastName LIKE :lastNamePattern")
//	List<MemberDto> findMembersByNamePattern(@Param("firstNamePattern") String firstNamePattern,
//			@Param("lastNamePattern") String lastNamePattern);
	
	
	// FIX THIS QUERY: Change "com.example.demo.dto.MemberDto" to "com.aroha.membervalidation.dto.MemberDto"
    @Query("SELECT NEW com.aroha.membervalidation.dto.MemberDto(" + "m.memberId, m.uniqId.firstName, m.uniqId.lastName, "
            + "m.uniqId.dob, m.uniqId.gender, m.city) " + "FROM Member m "
            + "WHERE m.uniqId.firstName LIKE :firstNamePattern " + "AND m.uniqId.lastName LIKE :lastNamePattern")
    List<MemberDto> findMembersByNamePattern(@Param("firstNamePattern") String firstNamePattern,
            @Param("lastNamePattern") String lastNamePattern);
	
	
	// JPA query for Selecting records between two dates
	@Query("SELECT m FROM Member m " + "WHERE m.uniqId.dob BETWEEN :startDate AND :endDate")
	List<Member> findMembersByDobBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	
	//Fetching records whose salary is greater than given salary
	@Query("""
	        SELECT NEW com.aroha.membervalidation.dto.MemberSalaryDto(
	            m.memberId,
	            m.uniqId.firstName,
	            m.uniqId.lastName,
	            m.uniqId.dob,
	            m.uniqId.gender,
	            m.city,
	            m.monthlySalary
	        )
	        FROM Member m
	        WHERE m.monthlySalary >= :minSalary
	        """)
	List<MemberSalaryDto> findBySalary(@Param("minSalary") String minSalary);
}
