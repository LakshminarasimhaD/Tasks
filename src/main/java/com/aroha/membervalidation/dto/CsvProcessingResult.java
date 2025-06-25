package com.aroha.membervalidation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CsvProcessingResult {

	private int validCount;
	private int invalidCount;
	private long processingTimeMs;
	private int commits;

}