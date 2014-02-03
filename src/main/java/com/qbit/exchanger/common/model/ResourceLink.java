package com.qbit.exchanger.common.model;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Александр
 */
@XmlRootElement
public class ResourceLink implements Serializable {

	private final String id;

	private final String href;

	public ResourceLink(String id, String href) {
		this.id = id;
		this.href = href;
	}

	public String getId() {
		return id;
	}

	public String getHref() {
		return href;
	}
}
