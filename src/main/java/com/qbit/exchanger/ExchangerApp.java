package com.qbit.exchanger;

import com.qbit.exchanger.env.Env;
import com.qbit.exchanger.services.yandex.YandexMoneyService;
import javax.inject.Inject;
import javax.inject.Singleton;
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

		// singleton binding
		addBinding(newBinder(Env.class).to(Env.class).in(Singleton.class), configuration);
		addBinding(newBinder(YandexMoneyService.class).to(YandexMoneyService.class), configuration);

		// commits changes
		configuration.commit();
	}
}
