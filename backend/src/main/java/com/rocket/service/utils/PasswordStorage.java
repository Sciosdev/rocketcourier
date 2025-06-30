package com.rocket.service.utils;

import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author Raul Eduardo Martinez Chavez
 * 
 */
public class PasswordStorage {

	static public class InvalidHashException extends Exception {
		public InvalidHashException(String message) {
			super(message);
		}

		public InvalidHashException(String message, Throwable source) {
			super(message, source);
		}
	}

	static public class CannotPerformOperationException extends Exception {
		public CannotPerformOperationException(String message) {
			super(message);
		}

		public CannotPerformOperationException(String message, Throwable source) {
			super(message, source);
		}
	}

	public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA512";

	public static final int SALT_BYTE_SIZE = 128;
	public static final int HASH_BYTE_SIZE = 128;
	public static final int PBKDF2_ITERATIONS = 64000;

	public static final int HASH_SECTIONS = 2;
	public static final int HASH_ALGORITHM_INDEX = 0;
	public static final int ITERATION_INDEX = 1;
	public static final int HASH_SIZE_INDEX = 3;
	public static final int SALT_INDEX = 0;
	public static final int PBKDF2_INDEX = 1;

	public static String createHash(String password) throws CannotPerformOperationException {
		return createHash(password.toCharArray());
	}

	public static String createHash(char[] password) throws CannotPerformOperationException {
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[SALT_BYTE_SIZE];
		random.nextBytes(salt);

		byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTE_SIZE);

		String other = toBase64(salt) + ":" + toBase64(hash);
		return other;
	}

	public static boolean verifyPassword(String password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException {
		return verifyPassword(password.toCharArray(), correctHash);
	}

	public static boolean verifyPassword(char[] password, String correctHash)
			throws CannotPerformOperationException, InvalidHashException {
		String[] params = correctHash.split(":");
		if (params.length != HASH_SECTIONS) {
			throw new InvalidHashException("Fields are missing from the password hash.");
		}

		int iterations = 0;
		try {
			iterations = PBKDF2_ITERATIONS;
		} catch (NumberFormatException ex) {
			throw new InvalidHashException("Could not parse the iteration count as an integer.", ex);
		}

		if (iterations < 1) {
			throw new InvalidHashException("Invalid number of iterations. Must be >= 1.");
		}

		byte[] salt = null;
		try {
			salt = fromBase64(params[SALT_INDEX]);
		} catch (IllegalArgumentException ex) {
			throw new InvalidHashException("Base64 decoding of salt failed.", ex);
		}

		byte[] hash = null;
		try {
			hash = fromBase64(params[PBKDF2_INDEX]);
		} catch (IllegalArgumentException ex) {
			throw new InvalidHashException("Base64 decoding of pbkdf2 output failed.", ex);
		}

		byte[] testHash = pbkdf2(password, salt, iterations, hash.length);

		return slowEquals(hash, testHash);
	}

	private static boolean slowEquals(byte[] a, byte[] b) {
		int diff = a.length ^ b.length;
		for (int i = 0; i < a.length && i < b.length; i++)
			diff |= a[i] ^ b[i];
		return diff == 0;
	}

	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
			throws CannotPerformOperationException {
		try {
			PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
			return skf.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException ex) {
			throw new CannotPerformOperationException("Hash algorithm not supported.", ex);
		} catch (InvalidKeySpecException ex) {
			throw new CannotPerformOperationException("Invalid key spec.", ex);
		}
	}

	private static byte[] fromBase64(String hex) throws IllegalArgumentException {
		return DatatypeConverter.parseBase64Binary(hex);
	}

	private static String toBase64(byte[] array) {
		return DatatypeConverter.printBase64Binary(array);
	}

}