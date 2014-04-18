package com.qbit.exchanger.rest.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Александр
 */
public final class RESTUtil {

	private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final DateFormat DATE_TIME_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private RESTUtil() {
	}

	public static Date toDate(String dateStr) throws ParseException {
		if ((dateStr == null) || dateStr.isEmpty()) {
			return null;
		}
		Date result;
		try {
			result = DATE_TIME_FORMAT_WITH_MILLIS.parse(dateStr);
		} catch (ParseException ex) {
			result = DATE_TIME_FORMAT.parse(dateStr);
		}
		return result;
	}
}
