package com.qbit.exchanger.env;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.inject.Singleton;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Exchanger properties
 * 
 * @author Alexander_Alexandrov
 */
@Singleton
@XmlRootElement
public class Env {

	private final Properties properties;
	
	public Env() {
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
	
	@XmlElement
	public String getMailBotAddress() {
		return properties.getProperty("mail.bot.address");
	}
	
	@XmlTransient
	public String getMailBotPersonal() {
		return properties.getProperty("mail.bot.personal");
	}
	
	@XmlTransient
	public String getMailHost() {
		return properties.getProperty("mail.host");
	}
	
	@XmlTransient
	public InternetAddress getMailBotInternetAddress() throws UnsupportedEncodingException {
		return new InternetAddress(getMailBotAddress(), getMailBotPersonal(), "UTF-8");
	}
}
