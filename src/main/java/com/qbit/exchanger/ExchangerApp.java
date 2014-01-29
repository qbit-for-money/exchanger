package com.qbit.exchanger;

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

		// commits changes
		configuration.commit();
	}
}
