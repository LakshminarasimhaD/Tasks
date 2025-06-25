package com.aroha.membervalidation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.membervalidation.dto.CsvProcessingResult;
import com.aroha.membervalidation.dto.MemberDto;
import com.aroha.membervalidation.entity.Member;
import com.aroha.membervalidation.service.MemberService;

@RestController
@RequestMapping("/api/member")
public class MemberController {

	@Autowired
	private MemberService memberService;

	@PostMapping("/uploadcsv")
	public ResponseEntity<CsvProcessingResult> upload(@RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(memberService.processCsvFile(file));
	}

	@GetMapping("/getMemberByFirstName")
	public ResponseEntity<List<Member>> getMemberByFirstName(@RequestParam("firstName") String firstName) {
		return ResponseEntity.ok(memberService.getMemberByFirstName(firstName));

	}

	@GetMapping("/getMemberByLastName")
	public ResponseEntity<List<Member>> getMemberByLastName(@RequestParam("lastName") String lastName) {
		return ResponseEntity.ok(memberService.getMemberByLastName(lastName));
	}

//	@GetMapping("/name-pattern")
//	public ResponseEntity<List<MemberDto>> getMembersByNamePattern(
//			@RequestParam(defaultValue = "FirstName2") String firstNamePrefix,
//			@RequestParam(defaultValue = "LastName2") String lastNamePrefix) {
//
//		List<MemberDto> members = memberService.getMembersByNamePattern(firstNamePrefix, lastNamePrefix);
//
//		return ResponseEntity.ok(members);
//	}
	
	@GetMapping("/name-pattern")
	public ResponseEntity<List<MemberDto>> getMembersByNamePattern(
	        @RequestParam(defaultValue = "FirstName2") String firstNamePrefix,
	        @RequestParam(defaultValue = "LastName2") String lastNamePrefix) {
	    List<MemberDto> members = memberService.getMembersByNamePattern(firstNamePrefix, lastNamePrefix);
	    return ResponseEntity.ok(members);
	}

	@GetMapping("/by-dob-range")
	public ResponseEntity<List<Member>> getMembersByDobRange(@RequestParam String startDate,
			@RequestParam String endDate) {

		List<Member> members = memberService.getMembersByDobRange(startDate, endDate);
		return ResponseEntity.ok(members);
	}

	@GetMapping("/by-salary")
	public ResponseEntity<?> getMembersBySalary(@RequestParam(defaultValue = "20000") String salary) {

		return ResponseEntity.ok(memberService.getMembersBySalary(salary));
	}
}