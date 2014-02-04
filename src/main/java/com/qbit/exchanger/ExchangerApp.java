package com.qbit.exchanger;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.mail.MailService;
import com.qbit.exchanger.order.dao.CurrencyDAO;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.user.UserDAO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.ws.rs.core.Application;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import static org.glassfish.jersey.internal.inject.Injections.*;

/**
 *
 * @author Alexander_Alexandrov
 */
public class ExchangerApp extends Application {

	@Inject
	public ExchangerApp(ServiceLocator serviceLocator) {
		DynamicConfiguration configuration = getConfiguration(serviceLocator);

		addBinding(newBinder(Env.class).to(Env.class).in(Singleton.class), configuration);
		
		addBinding(newBinder(MailService.class).to(MailService.class).in(Singleton.class), configuration);
		
		addBinding(newBinder(Persistence.createEntityManagerFactory("exchangerPU"))
				.to(EntityManagerFactory.class), configuration);
		
		addBinding(newBinder(UserDAO.class).to(UserDAO.class).in(Singleton.class), configuration);
		
		addBinding(newBinder(CurrencyDAO.class).to(CurrencyDAO.class).in(Singleton.class), configuration);
		addBinding(newBinder(OrderDAO.class).to(OrderDAO.class).in(Singleton.class), configuration);

		// commits changes
		configuration.commit();
	}
}
