package com.qbit.exchanger.auth;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Alexander_Sergeev
 */
public final class EncryptionUtil {
	
	public final static String MD5 = "md5";
	
	private EncryptionUtil() {
	}
	
	public static String getMD5(String source) {
		if (source == null) {
			return null;
		}
		String digest = null;
		try {
			byte[] hash = MessageDigest.getInstance(MD5).digest(source.getBytes("UTF-8"));
			StringBuilder buf = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				buf.append(String.format("%02x", b & 0xFF));
			}
			digest = buf.toString();
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		return digest;
	}
}
