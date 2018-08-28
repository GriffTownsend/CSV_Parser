package com.ef;

import com.ef.components.LogRecordCSVParser;
import com.ef.model.TimeDuration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class ParserApplicationTests {

	@Resource
	LogRecordCSVParser logRecordCSVParser;

	@Value("${spring.datasource.url}")
	String databaseUrl;

	@Value("${spring.datasource.username}")
	String username;

	@Value("${spring.datasource.password}")
	String password;


	/**
	 * Verifies db connection, and ability to load test CSV records
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void contextLoads() throws SQLException, IOException {
		try(Connection connection = clearLogRecordTable()) {
			Long expected = 525l;
			loadLogRecords("test.log");
			Long count = getLogRecordCount(connection);
			Assert.assertEquals(expected, count);
		}
	}

	private Connection clearLogRecordTable() throws IOException, SQLException {
		String sql = "DELETE FROM log_record;"; // typically, we use TRUNCATE TABLE to clear data, but this hangs in mySql using jpa.

		Connection connection = DriverManager.getConnection(databaseUrl, username, password);
		Statement statement = connection.createStatement();
		statement.execute(sql);
		statement.close();
		return connection;
	}

	private Long getLogRecordCount(Connection connection) throws SQLException {
		String sql = "SELECT COUNT(1) FROM log_record";
		Statement statement;
		statement = connection.createStatement();
		statement.execute(sql);
		ResultSet resultSet = statement.getResultSet();
		Long count = null;
		while(resultSet.next()) {
			count = resultSet.getLong(1);
		}
		return count;
	}

	/***
	 * Hourly threshold test against the test data, checking against a subset of log data.
	 * @throws SQLException
	 */
	@Test
	public void testHourlyThreshold() throws SQLException {
		try(Connection connection = clearLogRecordTable()) {
			loadLogRecords("test.log");
			List<String> results = logRecordCSVParser.findThresholdByDuration("2017-01-01 00:00:00.000", TimeDuration.HOURLY, 100l);
			Assert.assertEquals("Results should be empty for hourly threshold of 100, there are only 8 records", results.size(), 0);

			results = logRecordCSVParser.findThresholdByDuration("2017-01-01 00:00:00.000", TimeDuration.HOURLY, 10l);
			Assert.assertEquals("Results should have 2 records", results.size(), 2);

			Assert.assertTrue(results.contains("192.168.169.194"));
			Assert.assertTrue(results.contains("192.168.234.82"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Tests parsing start date values as entered.
	 */
	@Test
	public void testStartDateEndDate() {
		String date = "2017-01-01 00:00:00.000";
		LocalDateTime startTime = logRecordCSVParser.parseDateTime(date);
		LocalDateTime endTime = startTime.plusHours(1l);
		String startDate = DateTimeFormatter.ofPattern("yyyy-dd-MM HH:mm:ss.SSS").format(startTime);
		String endDate = DateTimeFormatter.ofPattern("yyyy-dd-MM HH:mm:ss.SSS").format(endTime);
		Assert.assertEquals(date, startDate);
		Assert.assertEquals("2017-01-01 01:00:00.000", endDate);

	}

	/****
	 * Daily threshold test, this verifies against the full log.
	 * @throws SQLException
	 * @throws IOException
	 */
	@Test
	public void testDailyThreshold() throws SQLException, IOException {
		try(Connection connection = clearLogRecordTable()) {
			loadLogRecords("access.log");
			List<String> results = logRecordCSVParser.findThresholdByDuration("2017-01-01 00:00:00.000", TimeDuration.DAILY, 1000l);
			Assert.assertEquals("Results should be empty for hourly threshold of 1000, there are only 8 records", results.size(), 0);

			results = logRecordCSVParser.findThresholdByDuration("2017-01-01 00:00:00.000", TimeDuration.DAILY, 500l);
			Assert.assertEquals("Results should have 15 records", results.size(), 15);
			// check for the presence of a few of the known IPs that exceed this threshold
			List<String> ipsToCheck = new ArrayList<String>(){{ add("192.168.162.248"); add("192.168.199.209");
					add("192.168.102.136"); add("192.168.38.77"); add("192.168.62.176"); }};
			Assert.assertTrue("Expected IPs missing", results.containsAll(ipsToCheck));
		}

	}

	private void loadLogRecords(String filename) throws SQLException, IOException {
		try(InputStream is =  getClass().getClassLoader().getResourceAsStream(filename)) {
			logRecordCSVParser.importCSVFile(is);
		}
	}

}
