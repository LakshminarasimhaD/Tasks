package com.aroha.membervalidation.CONTROLLER;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aroha.membervalidation.service.MemberService;
import com.opencsv.exceptions.CsvException;

/**
 * REST Controller for handling member data operations, specifically CSV file uploads.
 * This class exposes a POST endpoint to receive and process CSV files.
 */
@RestController // Marks this class as a REST Controller
@RequestMapping("/api/members") // Base path for all endpoints in this controller
public class MemberController {

    private final MemberService memberService;

    /**
     * Constructor for MemberController.
     * Spring will automatically inject the MemberService instance.
     *
     * @param memberService The service responsible for CSV processing and member validation.
     */
    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    /**
     * Handles the HTTP POST request for uploading a CSV file.
     * The CSV file should be sent as a 'multipart/form-data' request with the parameter name "file".
     *
     * @param file The uploaded CSV file encapsulated in a MultipartFile object.
     * @return A ResponseEntity containing a status message and an appropriate HTTP status code.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        // Check if the uploaded file is empty
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please select a CSV file to upload.", HttpStatus.BAD_REQUEST);
        }

        // Check if the uploaded file has a CSV content type
        if (!"text/csv".equals(file.getContentType())) {
            // Note: Some systems might send "application/vnd.ms-excel" for CSVs.
            // For production, you might want to broaden this check or rely on file extension.
            return new ResponseEntity<>("Only CSV files are allowed. Detected content type: " + file.getContentType(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        try {
            // Call the service layer to process the CSV file
            String resultMessage = memberService.processCsv(file);
            // Return a success response with the processing summary
            return new ResponseEntity<>(resultMessage, HttpStatus.OK);
        } catch (IOException e) {
            // Handle I/O errors during file reading
            return new ResponseEntity<>("Failed to read CSV file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (CsvException e) {
            // Handle errors specific to CSV parsing (e.g., malformed CSV)
            return new ResponseEntity<>("Failed to parse CSV file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Catch any other unexpected exceptions during the process
            // Log the exception for debugging purposes in a real application
            e.printStackTrace(); // For demonstration, print stack trace
            return new ResponseEntity<>("An unexpected error occurred during processing: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
