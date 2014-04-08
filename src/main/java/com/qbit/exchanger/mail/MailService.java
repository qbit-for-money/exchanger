package com.qbit.exchanger.mail;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.order.model.OrderInfo;
import com.qbit.exchanger.order.model.OrderStatus;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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

	@Inject
	private MailNotificationDAO mailNotificationDAO;

	private final Executor executor = Executors.newCachedThreadPool();

	private static final Configuration FREE_MAKER_CFG = new Configuration();

	static {
		FREE_MAKER_CFG.setClassForTemplateLoading(MailService.class, "templates");
		FREE_MAKER_CFG.setDefaultEncoding("UTF-8");
		FREE_MAKER_CFG.setLocale(Locale.US);
		FREE_MAKER_CFG.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	public void send(OrderInfo orderInfo) {
		if ((orderInfo == null) || (orderInfo.getId() == null) || orderInfo.getId().isEmpty()
				|| (orderInfo.getUserPublicKey() == null) || orderInfo.getUserPublicKey().isEmpty()) {
			return;
		}
		final OrderInfo safeOrderInfo = OrderInfo.clone(orderInfo);
		executor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					String tmplPrefix;
					if (safeOrderInfo.isValid()) {
						if ((OrderStatus.INITIAL == safeOrderInfo.getStatus())
								&& mailNotificationDAO.isNotificationSent(safeOrderInfo.getId(), safeOrderInfo.getStatus())) {
							return;
						}
						tmplPrefix = safeOrderInfo.getStatus().name().toLowerCase();
					} else {
						tmplPrefix = "invalid";
					}
					mailNotificationDAO.registerNotification(safeOrderInfo.getId(), safeOrderInfo.getStatus());

					send(safeOrderInfo.getUserPublicKey(), "[INFO] Order #" + safeOrderInfo.getId(),
							processTemplate(tmplPrefix, safeOrderInfo));
				} catch (Exception ex) {
					logger.error(ex.getMessage(), ex);
				}
			}
		});
	}

	private String processTemplate(String tmplPrefix, OrderInfo orderInfo) throws TemplateException, IOException {
		Map<String, Object> templateInput = new HashMap<>();
		templateInput.put("order", orderInfo);
		Template template = FREE_MAKER_CFG.getTemplate(tmplPrefix + "-order.tmpl");
		Writer text = new StringWriter();
		template.process(templateInput, text);
		return text.toString();
	}

	public void send(final String to, final String subject, final String text) {
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
