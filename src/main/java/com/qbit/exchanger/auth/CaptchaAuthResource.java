package com.qbit.exchanger.auth;

import com.octo.captcha.component.image.backgroundgenerator.BackgroundGenerator;
import com.octo.captcha.component.image.backgroundgenerator.GradientBackgroundGenerator;
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
@Path("captcha")
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

	@Context
	private HttpServletRequest httpServletRequest;

	@Inject
	private UserDAO userDAO;

	@GET
	@Path("image")
	@Produces("image/jpeg")
	public byte[] getImage(@QueryParam("pin") String pin, @QueryParam("timestamp") long timestamp) throws Exception {
		AuthKey authKey = new AuthKey(pin, timestamp);
		String result = AuthKey.encode(authKey);
		FontGenerator font = new RandomFontGenerator(18, 34);
		BackgroundGenerator background = new GradientBackgroundGenerator(360, 50, Color.lightGray, Color.yellow); //EllipseBackgroundGenerator(360, 50);
		TextPaster paster = new RandomTextPaster(4, 15, Color.RED);
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
	@Consumes(MediaType.APPLICATION_JSON)
	public boolean auth(AuthRequest authRequest) throws Exception {
		if (!authRequest.isValid()) {
			throw new IllegalArgumentException();
		}
		String encodedKey = authRequest.getEncodedKey();
		AuthKey authKeyFromCaptcha = AuthKey.decode(encodedKey);

		if (authKeyFromCaptcha.equals(authRequest.toAuthKey())) {
			httpServletRequest.getSession().setAttribute(AuthFilter.USER_ID_KEY, encodedKey);
			try {
				if (userDAO.find(encodedKey) == null) {
					userDAO.create(encodedKey);
					return true;
				}
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}
		return false;
	}
}
