package dev.xframe.utils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class XDateFormatter {
	
	static DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	static DateTimeFormatter     DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	static DateTimeFormatter     TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

	public static LocalDateTime toLocalDateTime(String t) {
		return LocalDateTime.parse(t, DATETIME_FORMATTER);
	}
	
	public static Timestamp toTimestamp(String t) {
		return Timestamp.valueOf(toLocalDateTime(t));
	}
	
	public static Date toDate(String t) {
		return Date.from(toLocalDateTime(t).atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate toLocalDate(String t) {
		return LocalDate.parse(t, DATE_FORMATTER);
	}

	public static LocalTime toLocalTime(String t) {
		return LocalTime.parse(t, TIME_FORMATTER);
	}

	public static String from(Date t) {
		return from(t.toInstant());
	}
	
	public static String from(Timestamp t) {
		return from(t.toInstant());
	}

	public static String from(long n) {
		return from(Instant.ofEpochMilli(n));
	}

	public static String from(Calendar time) {
		return from(time.toInstant());
	}

	public static String from(Instant instant) {
		return from(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
	}

	public static String from(LocalDateTime t) {
		return DATETIME_FORMATTER.format(t);
	}

	public static String from(LocalDate t) {
		return DATE_FORMATTER.format(t);
	}

	public static String from(LocalTime t) {
		return TIME_FORMATTER.format(t);
	}

}
