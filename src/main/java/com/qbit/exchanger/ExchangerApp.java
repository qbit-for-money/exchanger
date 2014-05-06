package com.qbit.exchanger;

import com.qbit.exchanger.buffer.BufferDAO;
import com.qbit.exchanger.dao.util.DAOExecutor;
import com.qbit.exchanger.dao.util.DefaultDAOExecutor;
import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.external.exchange.btce.BTCExchange;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.external.exchange.core.ExchangeFacade;
import com.qbit.exchanger.external.exchange.cryptsy.CryptsyExchange;
import com.qbit.exchanger.order.dao.MailNotificationDAO;
import com.qbit.exchanger.mail.MailService;
import com.qbit.exchanger.money.bitcoin.BitcoinMoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.dogecoin.DogecoinMoneyService;
import com.qbit.exchanger.money.litecoin.LitecoinMoneyService;
import com.qbit.exchanger.money.yandex.YandexMoneyService;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.service.OrderFlowScheduler;
import com.qbit.exchanger.order.service.OrderFlowWorker;
import com.qbit.exchanger.order.service.OrderService;
import com.qbit.exchanger.user.UserDAO;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
	private ServiceLocator serviceLocator;
	
	private EntityManagerFactory entityManagerFactory;

	public ExchangerApp() {
	}

	@PostConstruct
	public void init() {
		DynamicConfiguration configuration = getConfiguration(serviceLocator);

		addBinding(newBinder(Env.class).to(Env.class).in(Singleton.class), configuration);

		addBinding(newBinder(MailService.class).to(MailService.class).in(Singleton.class), configuration);

		entityManagerFactory = Persistence.createEntityManagerFactory("exchangerPU");
		addBinding(newBinder(entityManagerFactory).to(EntityManagerFactory.class), configuration);

		addBinding(newBinder(DefaultDAOExecutor.class).to(DAOExecutor.class).in(Singleton.class), configuration);
		
		addBinding(newBinder(UserDAO.class).to(UserDAO.class).in(Singleton.class), configuration);
		addBinding(newBinder(OrderDAO.class).to(OrderDAO.class).in(Singleton.class), configuration);
		addBinding(newBinder(BufferDAO.class).to(BufferDAO.class).in(Singleton.class), configuration);
		addBinding(newBinder(MailNotificationDAO.class).to(MailNotificationDAO.class).in(Singleton.class), configuration);

		addBinding(newBinder(BitcoinMoneyService.class).to(BitcoinMoneyService.class).in(Singleton.class), configuration);
		addBinding(newBinder(LitecoinMoneyService.class).to(LitecoinMoneyService.class).in(Singleton.class), configuration);
		addBinding(newBinder(DogecoinMoneyService.class).to(DogecoinMoneyService.class).in(Singleton.class), configuration);
		addBinding(newBinder(YandexMoneyService.class).to(YandexMoneyService.class).in(Singleton.class), configuration);

		addBinding(newBinder(MoneyServiceProvider.class).to(MoneyServiceProvider.class).in(Singleton.class), configuration);

		addBinding(newBinder(OrderService.class).to(OrderService.class).in(Singleton.class), configuration);
		addBinding(newBinder(OrderFlowWorker.class).to(OrderFlowWorker.class).in(Singleton.class), configuration);

		addBinding(newBinder(CryptsyExchange.class).to(CryptsyExchange.class).in(Singleton.class), configuration);
		addBinding(newBinder(ExchangeFacade.class).to(Exchange.class).in(Singleton.class), configuration);
		
		configuration.commit();

		serviceLocator.createAndInitialize(OrderFlowScheduler.class);
	}

	/**
	 * Called on application shutdown. We need this workaround because fucking
	 * Jersey 2.5.1 doesn't process @PreDestroy annotated methods in another
	 * classes except this one.
	 */
	@PreDestroy
	public void shutdown() {
		try {
			serviceLocator.shutdown();
		} catch (Throwable ex) {
			// Do nothing
		}
		try {
			entityManagerFactory.close();
		} catch (Throwable ex) {
			// Do nothing
		}
	}
}
