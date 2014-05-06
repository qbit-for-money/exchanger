package com.qbit.exchanger.external.exchange.cryptsy;

import com.qbit.exchanger.rest.util.*;
import java.io.StringReader;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.glassfish.jersey.client.ClientConfig;

/**
 * @author Alexander_Sergeev
 */
public class CryptsyExchangeRestClient {
	
	private static final String LTC = "{\"LTC\"";
	private static final String DOGE = "{\"DOGE\"";
	private static final String REPLACEMENT = "{\"currency\"";
	
	public static CryptsyRateResponse get(String target, String path, Class<CryptsyRateResponse> type, boolean forceUnmarshal) throws JAXBException {
		return get(target, path, null, null, type, forceUnmarshal);
	}
	
	public static CryptsyRateResponse get(String target, String path, String param, String paramValue, Class<CryptsyRateResponse> type, boolean forceUnmarshal) throws JAXBException {
		Client client = ClientBuilder.newClient(new ClientConfig());
		Invocation.Builder builder;
		if(param != null && paramValue != null) {
			builder = client.target(target).path(path).queryParam(param, paramValue).request(MediaType.APPLICATION_JSON_TYPE);
		} else {
			builder = client.target(target).path(path).request(MediaType.APPLICATION_JSON_TYPE);
		}
		if (forceUnmarshal) {
			return unmarshal(builder.get(String.class), type);
		} else {
			return builder.get(type);
		}
	}
	
	public static CryptsyRateResponse unmarshal(String text, Class<CryptsyRateResponse> type) throws JAXBException {
		if(text.indexOf("[") == 0) {
			text = text.substring(1, text.length() - 1);
		}	
		if(text.contains(LTC)) {
			System.out.println("!! contains");
			text = text.replace(LTC, REPLACEMENT);
		} else if (text.contains(DOGE)) {
			text = text.replace(DOGE, REPLACEMENT);
		}
		
		Source source = new StreamSource(new StringReader(text));
		return RESTClientUtil.unmarshal(source, type);
	}
}
