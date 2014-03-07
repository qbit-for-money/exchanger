package com.qbit.exchanger.util;

import com.qbit.exchanger.common.model.Identifiable;
import com.qbit.exchanger.common.model.ResourceLink;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Александр
 */
public final class RESTUtil {

	private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static final DateFormat DATE_TIME_FORMAT_WITH_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	private RESTUtil() {
	}

	public static List<ResourceLink> toLinks(String basePath, List<? extends Identifiable<?>> objs) {
		List<ResourceLink> result = new ArrayList<>();
		if (objs != null) {
			for (Identifiable<?> obj : objs) {
				result.add(new ResourceLink(obj.getId().toString(), basePath + "/" + obj.getId().toString()));
			}
		}
		return result;
	}

	public static Date toDate(String dateStr) throws ParseException {
		Date result;
		try {
			result = DATE_TIME_FORMAT_WITH_MILLIS.parse(dateStr);
		} catch (ParseException ex) {
			result = DATE_TIME_FORMAT.parse(dateStr);
		}
		return result;
	}
}
