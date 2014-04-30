package com.qbit.exchanger.external.exchange.btce;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Александр
 */
public class Ticker implements Serializable {

	private BigDecimal buy, sell;

	private long updated, server_time;

	public BigDecimal getBuy() {
		return buy;
	}

	public void setBuy(BigDecimal buy) {
		this.buy = buy;
	}

	public BigDecimal getSell() {
		return sell;
	}

	public void setSell(BigDecimal sell) {
		this.sell = sell;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}

	public long getServer_time() {
		return server_time;
	}

	public void setServer_time(long server_time) {
		this.server_time = server_time;
	}
}
