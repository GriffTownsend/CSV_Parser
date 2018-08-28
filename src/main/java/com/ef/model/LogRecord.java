package com.ef.model;

import lombok.Data;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity(name = "log_record")
public class LogRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;

	@Column(name="log_date")
	Date date;

	String ipAddress;

	String request;

	Integer status;

	String userAgent;

	public LogRecord() {}

	public LogRecord(Date date, String ipAddress, String request, Integer status, String userAgent) {
		this.date = date;
		this.ipAddress = ipAddress;
		this.request = request;
		this.status = status;
		this.userAgent = userAgent;
	}
}
