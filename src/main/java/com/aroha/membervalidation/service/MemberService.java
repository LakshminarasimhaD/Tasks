package com.aroha.membervalidation.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.membervalidation.SpringBootCsvFileHandlingApplication;
import com.aroha.membervalidation.dto.CsvProcessingResult;
import com.aroha.membervalidation.dto.MemberDto;
import com.aroha.membervalidation.dto.MemberSalaryDto;
import com.aroha.membervalidation.entity.Member;
import com.aroha.membervalidation.entity.MemberId;
import com.aroha.membervalidation.exception.CsvProcessingException;
import com.aroha.membervalidation.exception.InvalidRecordException;
import com.aroha.membervalidation.exception.MemberNotFoundException;
import com.aroha.membervalidation.repository.MemberRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

@Service
public class MemberService {


	@Autowired
	private MemberRepository memberRepository;

	private static final int BATCH_SIZE = 100;

	int commitCount = 0;
	int validCount = 0;
	int invalidCount = 0;

	// Set to track unique records in memory
	private Set<String> uniqueRecords = new HashSet<>();

	List<Member> validRecords = new ArrayList<>();

	

	public CsvProcessingResult processCsvFile(MultipartFile file) {
		long startTime = System.currentTimeMillis();

		try (CSVReader cr = new CSVReader(new InputStreamReader(file.getInputStream()))) {
			String[] headers = cr.readNext();
			if (headers == null || headers.length < 14) {
				throw new CsvProcessingException("Invalid header or insufficient columns");
			}

			String[] data;
			while ((data = cr.readNext()) != null) {
				try {
					Member member = processRecord(data);
					validRecords.add(member);
					validCount++;

					// Insert in batches
					if (validRecords.size() >= BATCH_SIZE) {
						insertBatch(validRecords);
						commitCount++;
						validRecords.clear();
					}
				} catch (InvalidRecordException e) {
					invalidCount++;
				}
			}
			// Insert any remaining records
			if (!validRecords.isEmpty()) {
				insertBatch(validRecords);
			}
		} catch (IOException | CsvValidationException e) {
			throw new CsvProcessingException("Error processing CSV file", e);
		}

		System.out.println("Batch size is : " + BATCH_SIZE + " and Commits Count is : " + commitCount);
		long endTime = System.currentTimeMillis();
		return new CsvProcessingResult(validCount, invalidCount, endTime - startTime, commitCount);
	}

	@Transactional
	public Member processRecord(String[] data) throws InvalidRecordException {
		// Validate record length
		if (data.length < 14) {
			throw new InvalidRecordException("Insufficient fields");
		}

		// Trim all fields
		for (int i = 0; i < data.length; i++) {
			data[i] = data[i] != null ? data[i].trim() : "";
		}

		// Extract fields
		String id = data[0];
		String firstName = data[1];
		String lastName = data[2];
		String dob = data[3];
		String gender = data[4];
		String education = data[5];
		String houseNumber = data[6];
		String address1 = data[7];
		String address2 = data[8];
		String city = data[9];
		String pincode = data[10];
		String mobile = data[11];
		String company = data[12];
		String monthlySalary = data[13];

		// Validating required fields
		if (id.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || gender.isEmpty() || dob.isEmpty()
				|| city.isEmpty() || education.isEmpty() || address1.isEmpty() || address2.isEmpty()
				|| houseNumber.isEmpty() || pincode.isEmpty() || mobile.isEmpty() || monthlySalary.isEmpty()) {

			throw new InvalidRecordException("Empty fields");
		}

		// Checking for duplicates
		// Create a unique key for the record
		String recordKey = String.join("|", firstName, lastName, gender, dob.toString());

		// Check for duplicates using the Set
		if (uniqueRecords.contains(recordKey)) {
			throw new InvalidRecordException("Duplicate record");
		}

		// adding unique records to set
		uniqueRecords.add(recordKey);

		// Validating mobile number
		if (!Pattern.matches("^[789]\\d{9}$", mobile)) {
			throw new InvalidRecordException("Invalid mobile: " + mobile);
		}

		// validating date of birth
		LocalDate birthDate = parseDate(dob);
		if (birthDate.isAfter(LocalDate.now())) {
			throw new InvalidRecordException("Future date: " + dob);
		}

		// Validating age
		Period age = Period.between(birthDate, LocalDate.now());
		if (age.getYears() > 100) {
			throw new InvalidRecordException("Age is greater than 100: " + dob);
		}

		// Clean address fields
		address1 = cleanAddress(address1);
		address2 = cleanAddress(address2);

		// Creating member object and saving it to database
		MemberId memberId = new MemberId(firstName, lastName, birthDate, gender);
		Member member = new Member(memberId, id, address1, address2, city, company, education, houseNumber, mobile,
				monthlySalary, pincode);

		return member;
	}

	// Parsing date of birth
	private LocalDate parseDate(String dateStr) throws InvalidRecordException {
		try {
			if (dateStr.contains("-")) {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			} else if (dateStr.contains("/")) {
				return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("d/M/yyyy"));
			}
			throw new InvalidRecordException("Unsupported date format: " + dateStr);
		} catch (DateTimeParseException e) {
			throw new InvalidRecordException("Invalid date format: " + dateStr);
		}
	}

	// Removing SPecial characters from the address
	private String cleanAddress(String address) {
		return address.replaceAll("[^a-zA-Z0-9\\s,]", "");
	}

	// Iterating the list and inserting to database
	private void insertBatch(List<Member> members) {
		members.forEach(member -> {
			memberRepository.insertMember(member.getUniqId().getFirstName(), member.getUniqId().getLastName(),
					member.getUniqId().getDob(), member.getUniqId().getGender(), member.getMemberId(),
					member.getEducation(), member.getHouseNumber(), member.getAddress1(), member.getAddress2(),
					member.getPinCode(), member.getCity(), member.getMobile(), member.getCompany(),
					member.getMonthlySalary());
		});
	}

	// Creating service which fetches records by member first name
	public List<Member> getMemberByFirstName(String firstName) {

		List<Member> members = memberRepository.findByUniqId_FirstName(firstName);

		if (members.isEmpty()) {
			throw new MemberNotFoundException("Member not found for this : " + firstName);
		}
		return members;
	}

	// Creating service which fetches records by member Last name
	public List<Member> getMemberByLastName(String lastName) {
		List<Member> members = memberRepository.findByUniqId_LastName(lastName);
		if (members.isEmpty()) {
			throw new MemberNotFoundException("Member not found for this : " + lastName);
		}
		return members;
	}

	// get the names based on given pattern
	public List<MemberDto> getMembersByNamePattern(String firstNamePrefix, String lastNamePrefix) {
		String firstNamePattern = firstNamePrefix + "%";
		String lastNamePattern = lastNamePrefix + "%";

		List<MemberDto> members = memberRepository.findMembersByNamePattern(firstNamePattern, lastNamePattern);

		if (members.isEmpty()) {
			throw new MemberNotFoundException("No matched records found for this name pattern");
		}
		return members;
	}

	// fetch the records between two dates
	public List<Member> getMembersByDobRange(String startDateStr, String endDateStr) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

		try {
			LocalDate startDate = LocalDate.parse(startDateStr, formatter);
			LocalDate endDate = LocalDate.parse(endDateStr, formatter);

			List<Member> members = memberRepository.findMembersByDobBetween(startDate, endDate);

			if (members.isEmpty()) {
				throw new MemberNotFoundException("No members found in the specified date range");
			}
			return members;
		} catch (DateTimeParseException e) {
			throw new InvalidRecordException("Invalid date format. Please use dd-MM-yyyy format");
		}
	}

	// fetch the records Salary greater than 2000
	public List<MemberSalaryDto> getMembersBySalary(String salary) {
		return memberRepository.findBySalary(salary);
	}

}