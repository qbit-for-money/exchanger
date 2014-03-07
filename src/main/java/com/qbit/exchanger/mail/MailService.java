package com.qbit.exchanger.mail;

import com.qbit.exchanger.env.Env;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

/**
 *
 * @author Александр
 */
@Singleton
public class MailService {

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
			Logger.getLogger(MailService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
