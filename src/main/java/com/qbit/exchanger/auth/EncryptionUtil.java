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
			MessageDigest md = MessageDigest.getInstance(MD5);
			byte[] hash = md.digest(source.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			digest = sb.toString();

		} catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
		return digest;
	}
}
