package com.ef.dao;

import com.ef.model.LogRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface LogRecordRepository extends CrudRepository<LogRecord, Integer> {
	@Query("SELECT lr.ipAddress FROM log_record lr where date >= :startDate AND date < :endDate GROUP BY ipAddress HAVING COUNT(ipAddress) > :threshold")
	List<String> findThresholdByDuration(@Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("threshold") Long threshold);
}
