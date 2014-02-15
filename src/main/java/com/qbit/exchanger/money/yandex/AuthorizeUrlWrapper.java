package com.qbit.exchanger.money.yandex;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthorizeUrlWrapper {

	@XmlElement
	private String url;

	public AuthorizeUrlWrapper() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
