package com.ef.components;

import com.ef.dao.LogRecordRepository;
import com.ef.model.LogRecord;
import com.ef.model.TimeDuration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class LogRecordCSVParser {

	@Resource
	private LogRecordRepository recordRepository;

	@Value("${spring.datasource.url}")
	String databaseUrl;

	@Value("${spring.datasource.username}")
	String username;

	@Value("${spring.datasource.password}")
	String password;

	public void importCSVFile(String pathName) throws SQLException {
		verifyDatabase();
		try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(pathName)) {
			importCSVFile(is);
		} catch (IOException e) {
			log.error("IO Exception accessing CSV file", e);
		}
	}

	public void importCSVFile(InputStream is) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT
					 .withHeader("date", "ipAddress", "request", "status","userAgent").withSkipHeaderRecord(false).withTrim().withDelimiter('|')
			 ))
		{
			Iterator<CSVRecord> records = parser.iterator();
			final List<LogRecord> logRecords = new ArrayList<>();
			records.forEachRemaining(record -> {
				LocalDateTime date = parseDateTime(record.get("date"));
				String ipAddress = record.get("ipAddress");
				String request = record.get("request");
				Integer status = Integer.valueOf(record.get("status"));
				String userAgent = record.get("userAgent");

				LogRecord logRecord = new LogRecord(Date.from(date.atZone(ZoneId.systemDefault()).toInstant()), ipAddress, request, status, userAgent);
				logRecords.add(logRecord);
				if(logRecords.size()==200) {
					recordRepository.saveAll(logRecords);
					logRecords.clear();
				}
			});
			if(logRecords.size() > 0) {
				recordRepository.saveAll(logRecords);
			}
		}
	}

	private void verifyDatabase() throws SQLException {
			String sql="DELETE FROM log_record";
			Connection connection = DriverManager.getConnection(databaseUrl, username, password);
			Statement statement = connection.createStatement();
			statement.execute(sql);
			statement.close();
			connection.close();
	}

	public List<String> findThresholdByDuration(String startDate, TimeDuration duration, Long threshold) {
		List<String> results = new ArrayList<>();
		if(org.apache.commons.lang3.StringUtils.isBlank(startDate)) {
			throw new IllegalArgumentException("Start Date not provided");
		}
		LocalDateTime start = parseDateTime(startDate);
		Assert.notNull(duration, "Duration cannot be null");
		Assert.notNull(threshold, "Threshold cannot be null");
		return findThreshold(start, duration, threshold);
	}

	public List<String> findThreshold(LocalDateTime start, TimeDuration duration, Long threshold) {
		LocalDateTime end = (TimeDuration.DAILY.equals(duration))? start.plusDays(1) : start.plusHours(1);
		return recordRepository.findThresholdByDuration(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()),
				Date.from(end.atZone(ZoneId.systemDefault()).toInstant()), threshold);
	}

	public LocalDateTime parseDateTime(String date) {
		DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
		LocalDateTime dateTime = LocalDateTime.from(f.parse(date));
		return dateTime;
	}

	public LogRecordRepository getRepository() {
		return this.recordRepository;
	}
}
