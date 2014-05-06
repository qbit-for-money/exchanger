package com.qbit.exchanger.external.exchange.resource;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qbit.exchanger.common.model.Tuple2;
import com.qbit.exchanger.external.exchange.core.Exchange;
import com.qbit.exchanger.money.model.Currency;
import com.qbit.exchanger.money.model.Rate;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Александр
 */
@Path("exchanges")
@Singleton
public class ExchangesResource {

	@Inject
	private Exchange exchange;

	private Cache<Tuple2<Currency, Currency>, Rate> cache;

	@PostConstruct
	public void init() {
		cache = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.maximumSize(Currency.values().length * (Currency.values().length - 1))
				.build();
	}

	@GET
	@Path("rate")
	@Produces(MediaType.APPLICATION_JSON)
	public Rate getRate(@QueryParam("from") final Currency from, @QueryParam("to") final Currency to) throws Exception {
		return cache.get(new Tuple2(from, to), new Callable<Rate>() {

			@Override
			public Rate call() throws Exception {
				return exchange.getRate(from, to);
			}
		});
//		return new Rate(BigDecimal.ONE, from, to);
	}
}
