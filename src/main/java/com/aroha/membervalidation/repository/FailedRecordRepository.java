package com.aroha.membervalidation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aroha.membervalidation.entity.FailedRecord;
@Repository
public interface FailedRecordRepository extends JpaRepository<FailedRecord, Long>{

}
