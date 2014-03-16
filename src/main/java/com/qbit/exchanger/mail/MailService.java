package com.qbit.exchanger.mail;

import com.qbit.exchanger.env.Env;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Александр
 */
@Singleton
public class MailService {

	private final Logger logger = LoggerFactory.getLogger(MailService.class);

	@Inject
	private Env env;

	public void send(String to, String subject, String text) {
		try {
			Email email = new SimpleEmail();
			email.setHostName(env.getMailHost());
			email.setSmtpPort(465);
			email.setAuthenticator(new DefaultAuthenticator(env.getMailBotAddress(),
					env.getMailBotPersonal()));
			email.setSSL(true);
			email.setFrom(env.getMailBotAddress());
			email.addTo(to);
			email.setSubject(subject);
			email.setMsg(text);
			email.send();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
}
