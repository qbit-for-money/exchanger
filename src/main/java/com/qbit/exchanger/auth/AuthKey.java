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

	private static final int PIN_BIT_LEN = 16;
	private static final BigInteger PIN_MASK = BigInteger.valueOf(2).pow(PIN_BIT_LEN).subtract(BigInteger.ONE);
	private static final int PIN_MAX_BIT_INDEX = PIN_BIT_LEN - 1;

	private static final int MAX_SHIFT = 32;
	private static final BigInteger MAX_SHIFT_MASK = BigInteger.valueOf(MAX_SHIFT - 1);
	private static final int MAX_SHIFT_BIT_LEN = BigInteger.valueOf(MAX_SHIFT).bitLength();

	private static final Random rnd = new Random();

	public static String encode(AuthKey key) {
		BigInteger pin = new BigInteger(key.pin).setBit(PIN_MAX_BIT_INDEX);
		BigInteger timestamp = BigInteger.valueOf(key.timestamp);
		BigInteger pinAndTimestamp = timestamp.shiftLeft(PIN_BIT_LEN).or(pin);
		int shift = rnd.nextInt(MAX_SHIFT);
		BigInteger rotatedKey = rotateLeft(pinAndTimestamp, pinAndTimestamp.bitLength(), shift)
				.setBit(pinAndTimestamp.bitLength());
		BigInteger rotatedKeyAndShift = rotatedKey.shiftLeft(MAX_SHIFT_BIT_LEN).or(BigInteger.valueOf(shift));
		return rotatedKeyAndShift.toString(Character.MAX_RADIX);
	}

	public static AuthKey decode(String encriptedKey) {
		BigInteger rotatedKeyAndShift = new BigInteger(encriptedKey, Character.MAX_RADIX);
		int shift = rotatedKeyAndShift.and(MAX_SHIFT_MASK).intValue();
		BigInteger rotatedKey = rotatedKeyAndShift.shiftRight(MAX_SHIFT_BIT_LEN);
		int pinAndTimestampBitLen = (rotatedKey.bitLength() - 1);
		BigInteger pinAndTimestamp = rotateRight(rotatedKey.clearBit(pinAndTimestampBitLen),
				pinAndTimestampBitLen, shift);
		BigInteger pin = pinAndTimestamp.and(PIN_MASK).clearBit(PIN_MAX_BIT_INDEX);
		BigInteger timestamp = pinAndTimestamp.shiftRight(PIN_BIT_LEN);
		return new AuthKey(pin.toString(), timestamp.longValue());
	}

	private static BigInteger rotateLeft(BigInteger num, int bitLength, int shift) {
		return num.shiftLeft(shift).or(num.shiftRight(bitLength - shift)).and(ones(bitLength));
	}

	private static BigInteger rotateRight(BigInteger num, int bitLength, int shift) {
		return num.shiftRight(shift).or(num.shiftLeft(bitLength - shift)).and(ones(bitLength));
	}

	private static BigInteger ones(int bitLength) {
		return BigInteger.valueOf(2).pow(bitLength).subtract(BigInteger.ONE);
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
	 * @param args
	 */
	public static void main(String[] args) {
		AuthKey authKey = new AuthKey("1234", 123456789);
		System.out.println(authKey);
		for (int i = 0; i < 100; i++) {
			String encodedKey = AuthKey.encode(authKey);
			System.out.println(encodedKey);
			AuthKey decodedKey = AuthKey.decode(encodedKey);
			if (!authKey.equals(decodedKey)) {
				System.out.println("Error: " + decodedKey);
				break;
			}
		}
	}
}
