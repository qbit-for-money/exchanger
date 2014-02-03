package com.qbit.exchanger.utils;

import com.qbit.exchanger.common.model.Identifiable;
import com.qbit.exchanger.common.model.ResourceLink;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Александр
 */
public final class RESTUtils {
	
	private RESTUtils() {
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
}
