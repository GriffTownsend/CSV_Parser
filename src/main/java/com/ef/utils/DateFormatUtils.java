package com.ef.utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class DateFormatUtils {

	private static final String[] DATE_PATTERNS = {"yyyy-MM-dd.HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SSS","yyyy-MM-dd.HH:mm:ss","yyyy-MM-dd HH:mm:ss"};

	public static LocalDateTime toParsedDateTime(String input) throws ParseException {
		Date parsed = DateUtils.parseDate(input, DATE_PATTERNS);
		return LocalDateTime.ofInstant(parsed.toInstant(), ZoneId.of("America/New_York"));
	}
}
