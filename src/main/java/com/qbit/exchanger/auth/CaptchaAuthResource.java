package com.qbit.exchanger.auth;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.FunkyBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.GradientBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.MultipleShapeBackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.fontgenerator.FontGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.RandomTextPaster;
import com.octo.captcha.component.image.textpaster.TextPaster;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.image.wordtoimage.WordToImage;
import com.qbit.exchanger.user.UserDAO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander_Sergeev
 */
@Path("captcha-auth")
@Singleton
public class CaptchaAuthResource {

	@XmlRootElement
	public static class AuthRequest implements Serializable {

		private String encodedKey;
		private String pin;
		private long timestamp;

		public AuthRequest() {
		}

		public String getEncodedKey() {
			return encodedKey;
		}

		public void setEncodedKey(String encodedKey) {
			this.encodedKey = encodedKey;
		}

		public String getPin() {
			return pin;
		}

		public void setPin(String pin) {
			this.pin = pin;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public AuthKey toAuthKey() {
			return new AuthKey(pin, timestamp);
		}

		public boolean isValid() {
			return (encodedKey != null) && (pin != null) && (pin.length() == 4);
		}
	}

	private final Logger logger = LoggerFactory.getLogger(CaptchaAuthResource.class);

	private static final int MIN_WORD_LENGTH = 6;
	private static final int MAX_WORD_LENGTH = 15;

	private static final int IMAGE_WIDTH = 360;
	private static final int IMAGE_HEIGHT = 50;

	private static final Random rnd = new Random();

	private static final List<BackgroundGenerator> BACKGROUND_GENERATORS_LIST;
	private static final List<Color> COLORS_LIST;

	static {
		List<BackgroundGenerator> backgroundGeneratorsList = new ArrayList<>();
		List<Color> colorsList = new ArrayList<>();

		backgroundGeneratorsList.add(new UniColorBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT, Color.ORANGE));
		backgroundGeneratorsList.add(new MultipleShapeBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT, Color.MAGENTA, Color.GREEN, 20, 10, 15, 20, Color.ORANGE, Color.cyan, 15));
		backgroundGeneratorsList.add(new FunkyBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT));
		backgroundGeneratorsList.add(new GradientBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT, Color.yellow, Color.green));
		backgroundGeneratorsList.add(new GradientBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT, Color.LIGHT_GRAY, Color.magenta));
		backgroundGeneratorsList.add(new GradientBackgroundGenerator(IMAGE_WIDTH, IMAGE_HEIGHT, Color.orange, Color.magenta));

		colorsList.add(Color.red);
		colorsList.add(Color.green);
		colorsList.add(Color.gray);
		colorsList.add(Color.pink);
		
		BACKGROUND_GENERATORS_LIST = Collections.unmodifiableList(backgroundGeneratorsList);
		COLORS_LIST = Collections.unmodifiableList(colorsList);
	}

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("image")
	@Produces("image/jpeg")
	public byte[] getImage(@QueryParam("pin") String pin, @QueryParam("timestamp") long timestamp, @QueryParam("rand") long rand) throws Exception {
		AuthKey authKey = new AuthKey(pin, timestamp);
		String result = AuthKey.encode(authKey);
		FontGenerator font = new RandomFontGenerator(18, 34);
		BackgroundGenerator background = BACKGROUND_GENERATORS_LIST.get(rnd.nextInt(BACKGROUND_GENERATORS_LIST.size()));

		TextPaster paster = new RandomTextPaster(MIN_WORD_LENGTH, MAX_WORD_LENGTH, COLORS_LIST.get(rnd.nextInt(COLORS_LIST.size())));

		WordToImage wordToImage = new ComposedWordToImage(font, background, paster);
		BufferedImage buffer = wordToImage.getImage(result);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(2048);
		try {
			ImageIO.write(buffer, "jpeg", outputStream);
		} catch (IOException ex) {
			return null;
		}
		return outputStream.toByteArray();
	}

	@POST
	@Path("auth")
	@Consumes(MediaType.APPLICATION_JSON)
	public void auth(AuthRequest authRequest) throws Exception {
		if (!authRequest.isValid()) {
			throw new IllegalArgumentException();
		}
		String encodedKey = authRequest.getEncodedKey();
		AuthKey authKeyFromCaptcha = AuthKey.decode(encodedKey);

		if (!authKeyFromCaptcha.equals(authRequest.toAuthKey())) {
			throw new CaptchaAuthException();
		}
		httpServletRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, encodedKey);
		try {
			if (userDAO.find(encodedKey) == null) {
				userDAO.create(encodedKey);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
	}
	
	@POST
	@Path("logout")
	public void logout() {
		if(httpServletRequest.getSession().getAttribute(AuthFilter.USER_ID_KEY) != null) {
			httpServletRequest.getSession().removeAttribute(AuthFilter.USER_ID_KEY);
		}
	}
}
