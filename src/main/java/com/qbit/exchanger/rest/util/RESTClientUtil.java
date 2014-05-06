package com.qbit.exchanger.rest.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.glassfish.jersey.client.ClientConfig;

/**
 *
 * @author Александр
 */
public final class RESTClientUtil {

	private static final Map<String, Object> JAXB_PROPS = new HashMap<>(2);

	static {
		JAXB_PROPS.put(JAXBContextProperties.MEDIA_TYPE, MediaType.APPLICATION_JSON);
		JAXB_PROPS.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
	}

	private RESTClientUtil() {
	}
	
	public static <R> R get(String target, String path, Class<R> type) throws JAXBException {
		return get(target, path, null, null, type, false);
	}

	public static <R> R get(String target, String path, String param, String paramValue, Class<R> type) throws JAXBException {
		return get(target, path, param, paramValue, type, false);
	}
	
	public static <R> R get(String target, String path, Class<R> type, boolean forceUnmarshal) throws JAXBException {
		return get(target, path, null, null, type, forceUnmarshal);
	}

	public static <R> R get(String target, String path, String param, String paramValue, Class<R> type, boolean forceUnmarshal) throws JAXBException {
		Client client = ClientBuilder.newClient(new ClientConfig());
		Builder builder;
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

	public static <R> R unmarshal(String text, Class<R> type) throws JAXBException {
		System.out.println("!!! " + text);
		if(text.indexOf("[") == 0) {
			text = text.substring(1, text.length() - 1);
		}
		Source source = new StreamSource(new StringReader(text));
		return unmarshal(source, type);
	}

	public static <R> R unmarshal(Source source, Class<R> type) throws JAXBException {
		System.setProperty(JAXBContext.JAXB_CONTEXT_FACTORY, "org.eclipse.persistence.jaxb.JAXBContextFactory");
		JAXBContext context = JAXBContext.newInstance(new Class[] {type}, JAXB_PROPS);
		return context.createUnmarshaller().unmarshal(source, type).getValue();
	}
}
