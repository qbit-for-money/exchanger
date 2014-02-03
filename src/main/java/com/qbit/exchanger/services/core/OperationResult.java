package com.qbit.exchanger.services.core;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OperationResult {

	private OperationStatus status;
	private String text;

	public OperationResult(OperationStatus result, String text) {
		this.status = result;
		this.text = text;
	}

	public OperationResult(OperationStatus status) {
		this.status = status;
	}

	public OperationResult() {}

	@XmlElement
	public OperationStatus getStatus() {
		return status;
	}

	public void setStatus(OperationStatus status) {
		this.status = status;
	}

	@XmlElement
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
