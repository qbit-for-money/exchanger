package com.qbit.exchanger;

import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Exchanger properties
 * 
 * @author Alexander_Alexandrov
 */
@XmlRootElement
public class Env {

	private static final Env INST = new Env();
	
	public static Env inst() {
		return INST;
	}
	
	private final Properties properties;
	
	private Env() {
		properties = new Properties();
		try {
			properties.load(Env.class.getResourceAsStream("/com/qbit/exchanger/exchanger.properties"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@XmlElement
	public boolean isDemoEnabled() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("demo.enabled"));
	}
}
