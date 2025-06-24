package com.aroha.membervalidation.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aroha.membervalidation.entity.Member;
@Repository
public interface MemberRepository extends JpaRepository<Member, Long>{
	Optional<Member> findByFirstNameAndLastNameAndDateOfBirthAndGender(
            String firstName, String lastName, LocalDate dateOfBirth, String gender);
}
