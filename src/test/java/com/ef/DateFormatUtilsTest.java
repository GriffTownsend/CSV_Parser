package com.ef;


import com.ef.utils.DateFormatUtils;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;

public class DateFormatUtilsTest {

	@Test
	public void testSupportedDateInput() throws ParseException {
		String date = "2017-01-01.00:00:00.000";
		LocalDateTime dateTime = DateFormatUtils.toParsedDateTime(date);
		date = "2017-01-01 00:00:00.000";
		LocalDateTime dateTime1 = DateFormatUtils.toParsedDateTime(date);
		date = "2017-01-01.00:00:00";
		LocalDateTime dateTime2 = DateFormatUtils.toParsedDateTime(date);
		date = "2017-01-01 00:00:00";
		LocalDateTime dateTime3 = DateFormatUtils.toParsedDateTime(date);
		Assert.assertEquals("Dates must be equal", dateTime, dateTime1);
		Assert.assertEquals("Dates must be equal", dateTime, dateTime2);
		Assert.assertEquals("Dates must be equal", dateTime, dateTime3);

	}

}
