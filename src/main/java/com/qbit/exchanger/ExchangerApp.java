package com.qbit.exchanger;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.external.exchange.btce.BTCExchange;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.external.exchange.core.ExchangeFacade;
import com.qbit.exchanger.mail.MailService;
import com.qbit.exchanger.money.bitcoin.Bitcoin;
import com.qbit.exchanger.money.core.MoneyService;
import com.qbit.exchanger.money.core.MoneyServiceProvider;
import com.qbit.exchanger.money.litecoin.Litecoin;
import com.qbit.exchanger.money.yandex.YandexMoneyService;
import com.qbit.exchanger.order.dao.OrderDAO;
import com.qbit.exchanger.order.service.OrderFlowScheduler;
import com.qbit.exchanger.order.service.OrderFlowWorker;
import com.qbit.exchanger.order.service.OrderService;
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

		EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("exchangerPU");
		addBinding(newBinder(entityManagerFactory).to(EntityManagerFactory.class), configuration);

		addBinding(newBinder(UserDAO.class).to(UserDAO.class).in(Singleton.class), configuration);
		addBinding(newBinder(OrderDAO.class).to(OrderDAO.class).in(Singleton.class), configuration);

		addBinding(newBinder(Bitcoin.class).to(Bitcoin.class).in(Singleton.class), configuration);
                addBinding(newBinder(Litecoin.class).to(Litecoin.class).in(Singleton.class), configuration);
		addBinding(newBinder(YandexMoneyService.class).to(YandexMoneyService.class).in(Singleton.class), configuration);
		
		addBinding(newBinder(MoneyServiceProvider.class).to(MoneyServiceProvider.class).in(Singleton.class), configuration);

		addBinding(newBinder(OrderService.class).to(OrderService.class).in(Singleton.class), configuration);
		addBinding(newBinder(OrderFlowWorker.class).to(OrderFlowWorker.class).in(Singleton.class), configuration);

		addBinding(newBinder(BTCExchange.class).to(BTCExchange.class).in(Singleton.class), configuration);
		addBinding(newBinder(ExchangeFacade.class).to(Exchange.class).in(Singleton.class), configuration);

		configuration.commit();

		serviceLocator.createAndInitialize(OrderFlowScheduler.class);
		serviceLocator.createAndInitialize(YandexMoneyService.class);
	}
}
