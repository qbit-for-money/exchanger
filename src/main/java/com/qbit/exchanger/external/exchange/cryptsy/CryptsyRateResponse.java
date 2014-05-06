package com.qbit.exchanger.external.exchange.cryptsy;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Alexander_Sergeev
 */
@XmlRootElement
public class CryptsyRateResponse implements Serializable {
	public static class Return {
		public static class Markets {
			public static class CurrencyRate {
				private String label;
				@XmlElement(name="marketid")
				private long marketId;
				@XmlElement(name="lasttradeprice")
				private BigDecimal lastTradePrice;

				public String getLabel() {
					return label;
				}

				public void setLabel(String label) {
					this.label = label;
				}

				public long getMarketId() {
					return marketId;
				}

				public void setMarketId(long marketId) {
					this.marketId = marketId;
				}

				public BigDecimal getLastTradePrice() {
					return lastTradePrice;
				}

				public void setLastTradePrice(BigDecimal lastTradePrice) {
					this.lastTradePrice = lastTradePrice;
				}

				@Override
				public String toString() {
					return "CurrencyRate{" + "label=" + label + ", marketId=" + marketId + ", lastTradePrice=" + lastTradePrice + '}';
				}
			}
			
			@XmlElement(name="currency")
			private CurrencyRate currency;

			public CurrencyRate getCurrency() {
				return currency;
			}

			public void setCurrency(CurrencyRate currency) {
				this.currency = currency;
			}

			@Override
			public String toString() {
				return "Markets{" + "currencyRate=" + currency + '}';
			}
		}
		
		private Markets markets;

		public Markets getMarkets() {
			return markets;
		}

		public void setMarkets(Markets markets) {
			this.markets = markets;
		}

		@Override
		public String toString() {
			return "Return{" + "markets=" + markets + '}';
		}
	}
	@XmlElement(name="return")
	private Return returnMarket;

	public Return getReturnMarket() {
		return returnMarket;
	}

	public void setReturnMarket(Return returnMarket) {
		this.returnMarket = returnMarket;
	}

	@Override
	public String toString() {
		return "RateResponse{" + "returnMarket=" + returnMarket + '}';
	}
}
