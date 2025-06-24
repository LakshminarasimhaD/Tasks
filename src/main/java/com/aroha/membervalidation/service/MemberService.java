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
import java.util.Optional;
import java.util.Set; 
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.membervalidation.entity.FailedRecord;
import com.aroha.membervalidation.entity.Member;
import com.aroha.membervalidation.repository.MemberRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private static final Pattern MOBILE_NUMBER_PATTERN = Pattern.compile("^[789]\\d{9}$");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /**
     * Processes an uploaded CSV file. It reads each record, performs validation,
     * and categorizes records into 'validated_members' or 'failed_records' tables.
     * 
     */
    @Transactional
    public String processCsv(MultipartFile file) throws IOException, CsvException {
    	
    	//Start time 
    	 long startTime = System.currentTimeMillis();
        List<Member> validatedMembers = new ArrayList<>();
        List<FailedRecord> failedRecords = new ArrayList<>();
        int recordCounter = 0;

        // Set to store composite keys (FName, LName, DOB, Gender) encountered within the current batch.
        // Helps in detect duplicates *within the same uploaded CSV file*.
        Set<String> batchProcessedCompositeKeys = new HashSet<>();

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(file.getInputStream())).build()) {
            List<String[]> allRecords = reader.readAll();

            if (!allRecords.isEmpty()) {
                allRecords.remove(0); // Remove header row
            }

            for (String[] recordArray : allRecords) {
                recordCounter++;

                String failureReason = validateRecord(recordArray);

                if (failureReason == null) {
                    Member newMember = mapToMember(recordArray);

                    // Create a string representation of the composite key for in-batch uniqueness check
                    String compositeKey = newMember.getFirstName() + "-" +
                                          newMember.getLastName() + "-" +
                                          newMember.getDateOfBirth().format(DateTimeFormatter.ISO_LOCAL_DATE) + "-" + // Ensure consistent date string
                                          newMember.getGender();

                    // Check for uniqueness against previously processed records in the current batch
                    if (batchProcessedCompositeKeys.contains(compositeKey)) {
                        FailedRecord failedRecord = mapToFailedRecord(recordArray, recordCounter,
                                "Duplicate record found within the current CSV batch based on Composite Primary Key.");
                        failedRecords.add(failedRecord);
                        continue; // Skip further checks for this record
                    }

                    // Check for uniqueness against existing records in the database
                    Optional<Member> existingMemberInDb = memberRepository.findByFirstNameAndLastNameAndDateOfBirthAndGender(
                            newMember.getFirstName(),
                            newMember.getLastName(),
                            newMember.getDateOfBirth(),
                            newMember.getGender()
                    );

                    if (existingMemberInDb.isPresent()) {
                        FailedRecord failedRecord = mapToFailedRecord(recordArray, recordCounter,
                                "Duplicate record found in the database based on Composite Primary Key.");
                        failedRecords.add(failedRecord);
                    } else {
                        validatedMembers.add(newMember);
                        // Add the composite key to the set to mark it as processed in this batch
                        batchProcessedCompositeKeys.add(compositeKey);
                    }
                } else {
                    FailedRecord failedRecord = mapToFailedRecord(recordArray, recordCounter, failureReason);
                    failedRecords.add(failedRecord);
                }
            }
        }

        for (Member member : validatedMembers) {
            insertValidatedMemberNative(member);
        }

        for (FailedRecord failedRecord : failedRecords) {
            insertFailedRecordNative(failedRecord);
        }
        //End Time 
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime; // duration in milliseconds
        return String.format("CSV processing complete. %d records validated and inserted, %d records failed validation, % d MilliSeconds is time duration taken to exceute ",
                validatedMembers.size(), failedRecords.size(), duration);
    }

    private void insertValidatedMemberNative(Member member) {
        String sql = "INSERT INTO validated_members (first_name, last_name, date_of_birth, gender, " +
                     "education, house_number, address1, address2, city, pincode, mobile_number, company, monthly_salary) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        entityManager.createNativeQuery(sql)
                .setParameter(1, member.getFirstName())
                .setParameter(2, member.getLastName())
                .setParameter(3, member.getDateOfBirth())
                .setParameter(4, member.getGender())
                .setParameter(5, member.getEducation())
                .setParameter(6, member.getHouseNumber())
                .setParameter(7, member.getAddress1())
                .setParameter(8, member.getAddress2())
                .setParameter(9, member.getCity())
                .setParameter(10, member.getPincode())
                .setParameter(11, member.getMobileNumber())
                .setParameter(12, member.getCompany())
                .setParameter(13, member.getMonthlySalary())
                .executeUpdate();
    }

    private void insertFailedRecordNative(FailedRecord failedRecord) {
        String sql = "INSERT INTO failed_records (record_number, first_name, last_name, date_of_birth, gender, " +
                     "education, house_number, address1, address2, city, pincode, mobile_number, company, " +
                     "monthly_salary, failure_reason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        entityManager.createNativeQuery(sql)
                .setParameter(1, failedRecord.getRecordNumber())
                .setParameter(2, failedRecord.getFirstName())
                .setParameter(3, failedRecord.getLastName())
                .setParameter(4, failedRecord.getDateOfBirth())
                .setParameter(5, failedRecord.getGender())
                .setParameter(6, failedRecord.getEducation())
                .setParameter(7, failedRecord.getHouseNumber())
                .setParameter(8, failedRecord.getAddress1())
                .setParameter(9, failedRecord.getAddress2())
                .setParameter(10, failedRecord.getCity())
                .setParameter(11, failedRecord.getPincode())
                .setParameter(12, failedRecord.getMobileNumber())
                .setParameter(13, failedRecord.getCompany())
                .setParameter(14, failedRecord.getMonthlySalary())
                .setParameter(15, failedRecord.getFailureReason())
                .executeUpdate();
    }

    private String validateRecord(String[] recordArray) {
        final int EXPECTED_COLUMNS = 14;

        if (recordArray.length < EXPECTED_COLUMNS) {
            return "Missing data. Expected " + EXPECTED_COLUMNS + " columns, but found " + recordArray.length + ".";
        }

        for (int i = 0; i < EXPECTED_COLUMNS; i++) {
            if (recordArray[i] == null || recordArray[i].trim().isEmpty()) {
                return "Mandatory field missing at column " + getColumnName(i) + ".";
            }
        }

        String dateOfBirthStr = recordArray[3].trim();
        String mobileNumber = recordArray[11].trim();

        if (!MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches()) {
            return "Mobile number '" + mobileNumber + "' is invalid. Must be 10 digits and start with 7, 8, or 9.";
        }

        LocalDate dateOfBirth;
        try {
            dateOfBirth = LocalDate.parse(dateOfBirthStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return "Invalid Date of Birth format: '" + dateOfBirthStr + "'. Expected DD-MM-YYYY.";
        }

        if (dateOfBirth.isAfter(LocalDate.now())) {
            return "Date of Birth '" + dateOfBirthStr + "' cannot be a future date.";
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age > 100) {
            return "Age calculated from Date of Birth '" + dateOfBirthStr + "' is greater than 100 years. (Age: " + age + ")";
        }

        return null;
    }

    private Member mapToMember(String[] recordArray) {
        Member member = new Member();

        member.setFirstName(recordArray[1].trim());
        member.setLastName(recordArray[2].trim());
        member.setDateOfBirth(LocalDate.parse(recordArray[3].trim(), DATE_FORMATTER));
        member.setGender(recordArray[4].trim());
        member.setEducation(recordArray[5].trim());
        member.setHouseNumber(recordArray[6].trim());
        member.setAddress1(cleanAddressField(recordArray[7]));
        member.setAddress2(cleanAddressField(recordArray[8]));
        member.setCity(recordArray[9].trim());
        member.setPincode(recordArray[10].trim());
        member.setMobileNumber(recordArray[11].trim());
        member.setCompany(recordArray[12].trim());

        try {
            member.setMonthlySalary(Double.parseDouble(recordArray[13].trim()));
        } catch (NumberFormatException e) {
            member.setMonthlySalary(null);
        }
        return member;
    }

    private FailedRecord mapToFailedRecord(String[] recordArray, int recordNumber, String failureReason) {
        FailedRecord failedRecord = new FailedRecord();
        failedRecord.setRecordNumber(recordNumber);
        failedRecord.setFirstName(getRecordValue(recordArray, 1));
        failedRecord.setLastName(getRecordValue(recordArray, 2));
        failedRecord.setDateOfBirth(getRecordValue(recordArray, 3));
        failedRecord.setGender(getRecordValue(recordArray, 4));
        failedRecord.setEducation(getRecordValue(recordArray, 5));
        failedRecord.setHouseNumber(getRecordValue(recordArray, 6));
        failedRecord.setAddress1(getRecordValue(recordArray, 7));
        failedRecord.setAddress2(getRecordValue(recordArray, 8));
        failedRecord.setCity(getRecordValue(recordArray, 9));
        failedRecord.setPincode(getRecordValue(recordArray, 10));
        failedRecord.setMobileNumber(getRecordValue(recordArray, 11));
        failedRecord.setCompany(getRecordValue(recordArray, 12));
        failedRecord.setMonthlySalary(getRecordValue(recordArray, 13));
        failedRecord.setFailureReason(failureReason);
        return failedRecord;
    }

    private String getRecordValue(String[] recordArray, int index) {
        if (index >= 0 && index < recordArray.length && recordArray[index] != null) {
            return recordArray[index].trim();
        }
        return null;
    }

    private String cleanAddressField(String address) {
        if (address == null) {
            return "";
        }
        return address.replaceAll("[^a-zA-Z0-9\\s]", "").trim();
    }

    private String getColumnName(int index) {
        switch (index) {
            case 0: return "Record#";
            case 1: return "First Name";
            case 2: return "Last Name";
            case 3: return "Date of Birth";
            case 4: return "Gender";
            case 5: return "Education";
            case 6: return "House#";
            case 7: return "Address1";
            case 8: return "Address2";
            case 9: return "City";
            case 10: return "Pincode";
            case 11: return "Mobile#";
            case 12: return "Company";
            case 13: return "Monthly Salary";
            default: return "Unknown Column " + index;
        }
    }
}
