package com.qbit.exchanger.auth;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.Random;

/**
 *
 * @author Alexander_Alexandrov
 */
public final class AuthKey implements Serializable {
	
	private static final BigInteger CHIPHER_KEY = new BigInteger("f37uq9ybyk1a", Character.MAX_RADIX);
	
	private static final Random rnd = new Random();
	
	public static String encode(AuthKey key) {
		BigInteger pin = new BigInteger(key.pin).setBit(15);
		BigInteger timestamp = BigInteger.valueOf(key.timestamp);
		BigInteger result = BigInteger.ZERO.add(pin)
				.shiftLeft(timestamp.bitLength()).add(timestamp);
		return result.shiftLeft(4).add(BigInteger.valueOf(rnd.nextInt(0x10)))
				.xor(CHIPHER_KEY).toString(Character.MAX_RADIX);
	}
	
	public static AuthKey decode(String encodedKey) {
		BigInteger decryptedKey = new BigInteger(encodedKey, Character.MAX_RADIX).xor(CHIPHER_KEY).shiftRight(4);
		BigInteger pin = decryptedKey.shiftRight(decryptedKey.bitLength() - 16).clearBit(15);
		BigInteger timestamp = decryptedKey.subtract(pin.setBit(15).shiftLeft(decryptedKey.bitLength() - 16));
		return new AuthKey(pin.toString(), timestamp.longValue());
	}
	
	private final String pin;
	private final long timestamp;
	
	protected AuthKey() {
		this("0000", 0L);
	}

	public AuthKey(String pin, long timestamp) {
		if ((pin == null) || (pin.length() != 4)) {
			throw new IllegalArgumentException();
		}
		this.pin = pin;
		this.timestamp = timestamp;
	}

	public String getPin() {
		return pin;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.pin);
		hash = 97 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AuthKey other = (AuthKey) obj;
		if (!Objects.equals(this.pin, other.pin)) {
			return false;
		}
		if (this.timestamp != other.timestamp) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "AuthKey{" + "pin=" + pin + ", timestamp=" + timestamp + '}';
	}
	
	/**
	 * Unit test
	 */
	public static void main(String[] args) {
		AuthKey authKey = new AuthKey("1234", System.currentTimeMillis());
		System.out.println(authKey);
		String encodedKey = AuthKey.encode(authKey);
		System.out.println(encodedKey);
		AuthKey decodedKey = AuthKey.decode(encodedKey);
		System.out.println(decodedKey);
		assert authKey.equals(decodedKey);
	}
}
