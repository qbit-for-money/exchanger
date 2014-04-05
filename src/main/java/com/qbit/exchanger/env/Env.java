package com.qbit.exchanger.env;

import java.io.IOException;
import java.util.Properties;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Exchanger properties
 *
 * @author Alexander_Alexandrov
 */
@Singleton
@XmlRootElement
public class Env {

	private final Properties properties;

	public Env() {
		properties = new Properties();
		try {
			properties.load(Env.class.getResourceAsStream("/com/qbit/exchanger/exchanger.properties"));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	@XmlElement
	public boolean isDemoEnabled() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("demo.enabled"));
	}

	@XmlElement
	public String getMailBotAddress() {
		return properties.getProperty("mail.bot.address");
	}

	@XmlTransient
	public String getMailBotPersonal() {
		return properties.getProperty("mail.bot.personal");
	}

	@XmlTransient
	public String getMailHost() {
		return properties.getProperty("mail.host");
	}

	@XmlTransient
	public int getOrderWorkerPeriodSecs() {
		return Integer.parseInt(properties.getProperty("order.worker.period.secs"));
	}
	
	@XmlTransient
	public int getOrderCleanupPeriodHours() {
		return Integer.parseInt(properties.getProperty("order.cleanup.period.hours"));
	}

	@XmlTransient
	public String getYandexRedirectUrl() {
		return properties.getProperty("money.yandex.yandexRedirectUrl");
	}

	@XmlTransient
	public String getYandexResourceRedirectUrl() {
		return properties.getProperty("money.yandex.resourceRedirectUrl");
	}

	@XmlTransient
	public String getYandexResourceRedirectRoute() {
		return properties.getProperty("money.yandex.resourceRedirectRoute");
	}

	@XmlTransient
	public String getYandexWallet() {
		return properties.getProperty("money.yandex.wallet");
	}

	@XmlTransient
	public String getYandexToken() {
		return properties.getProperty("money.yandex.token");
	}

	@XmlTransient
	public String getYandexClientId() {
		return properties.getProperty("money.yandex.clientId");
	}

	@XmlTransient
	public String getYandexOperationDescription() {
		return properties.getProperty("money.yandex.operationDescription");
	}

	@XmlTransient
	public String getBitcoinWalletPath() {
		return properties.getProperty("money.bitcoin.wallet.path");
	}

	@XmlTransient
	public boolean isBitcoinTestnet() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("money.bitcoin.testnet"));
	}
	
	@XmlTransient
	public String getLitecoinWalletPath() {
		return properties.getProperty("money.litecoin.wallet.path");
	}

	@XmlTransient
	public boolean isLitecoinTestnet() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("money.litecoin.testnet"));
	}
	
	@XmlTransient
	public boolean isFullChain() {
		return Boolean.TRUE.toString().equalsIgnoreCase(properties.getProperty("money.crypto.fullChain"));
	}
	
	@XmlTransient
	public String getLitecoinDBName() {
		return properties.getProperty("money.litecoin.dbName");
	}
	
	@XmlTransient
	public String getLitecoinTestDBName() {
		return properties.getProperty("money.litecoin.test.dbName");
	}
	
	@XmlTransient
	public String getBitcoinDBName() {
		return properties.getProperty("money.bitcoin.dbName");
	}
	
	@XmlTransient
	public String getBitcoinTestDBName() {
		return properties.getProperty("money.bitcoin.test.dbName");
	}
	
	@XmlTransient
	public String getCryptoDBUsername() {
		return properties.getProperty("money.crypto.username");
	}
	
	@XmlTransient
	public String getCryptoDBPassword() {
		return properties.getProperty("money.crypto.password");
	}
	
	@XmlTransient
	public String getCryptoDBHostname() {
		return properties.getProperty("money.crypto.hostname");
	}
	
	@XmlTransient
	public String getGoogleClientId() {
		return properties.getProperty("auth.google.clientId");
	}
	
	@XmlTransient
	public String getGoogleClientSecret() {
		return properties.getProperty("auth.google.clientSecret");
	}
	
	@XmlTransient
	public String getGoogleScope() {
		return properties.getProperty("auth.google.scope");
	}
	
	@XmlTransient
	public String getGoogleUserInfoUrl() {
		return properties.getProperty("auth.google.userInfoUrl");
	}
	
	@XmlTransient
	public String getGoogleAuthorizeRoute() {
		return properties.getProperty("auth.google.authorizeRoute");
	}
	
	@XmlTransient
	public String getAdminMail() {
		return properties.getProperty("admin.mail");
	}
}
