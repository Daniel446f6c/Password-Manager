package com.danield.passwordgenerator;

import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.lang.Character;

/**
 * The {@code PwGen} class represents a easy-to-use Password Generator.
 * <p>
 * All generated passwords will contain uppercase and lowercase letters, as well as numbers.
 * You can define the length and whether or not to use symbols.
 * @author Daniel D
 * @version 0.3
 */
public class PwGen {
	
	private final String ERRMSG_PWTOSHORT = "the password length must not be less than 4.";
	private final String SYMBOLS = ".,:;-_+*~#'^!\"§$/%&?\\=^<>|[](){}";
	private final String LOWER_LETTERS = "abcdefghijklmnopqrstuvwxyz";
	private final String UPPER_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private final String NUMBERS = "0123456789";
	private final int shuffTimes = 200; // used by method ShuffleStrings()
	private final Random rnd = new Random();
	private boolean bSymbols = true;
	private int length = 12;
	
	/**
	 * The default configuration.
	 * <p>
	 * password length is 12 characters and symbols will be used.
	 */
	public PwGen() {}
	
	/**
	 * Custom configuration.
	 * @param length : the password length.
	 * @param useSymbols : whether or not to use symbols
	 */
	public PwGen(int length, boolean useSymbols) {

		this.length = length;
		bSymbols = useSymbols;

	}
	
	/**
	 * Set password length.
	 * @param length
	 */
	public void setLength(int length) {
		this.length = length;
	}
	
	/**
	 * Whether or not to use symbols.
	 * @param symbols
	 */
	public void useSymbols(boolean symbols) {
		bSymbols = symbols;
	}

	/**
	 * Generate one password.
	 * @return a new password
	 * @throws IllegalArgumentException if the password length is less than 4.
	 */
	public String generate() throws IllegalArgumentException {

		if (this.length < 4) {
			throw new IllegalArgumentException(ERRMSG_PWTOSHORT);
		}
		return calcPw();

	}
	
	/**
	 * Generate one password.
	 * @param length : the password length.
	 * @return a new password
	 * @throws IllegalArgumentException if the password length is less than 4.
	 */
	public String generate(int length) throws IllegalArgumentException {

		if (length < 4) {
			throw new IllegalArgumentException(ERRMSG_PWTOSHORT);
		}
		this.length = length;
		return calcPw();

	}
	
	/**
	 * Generate one password
	 * @param length : the password length.
	 * @param symbols : whether or not to use symbols.
	 * @return a new password
	 * @throws IllegalArgumentException if the password length is less than 4
	 */
	public String generate(int length, boolean symbols) {

		if (length < 4) {
			throw new IllegalArgumentException(ERRMSG_PWTOSHORT);
		}
		this.length = length;
		bSymbols = symbols;
		return calcPw();

	}
	
	
	//########################################PRIVATE METHODS########################################
	
	/**
	 * Swaps two chars at specified indices in a string.
	 * @param str : the string.
	 * @param i0 : index 1.
	 * @param i1 : index 2.
	 * @return a new string
	 */
	private String swap(String str, int i0, int i1) {

		char[] ca = str.toCharArray();
		char tmp = ca[i0];
		ca[i0] = ca[i1];
		ca[i1] = tmp;
		return new String(ca);

	}
	
	/**
	 * Make sure that the password contains at least 1 lower 1 upper 1 number (1 symbol)
	 * @param pw : the password to validate.
	 * @return {@code true} if the password meets the requirements, {@code false} otherwise.
	 */
	private boolean validatePassword(String pw) {

		boolean lower = false;
		boolean upper = false;
		boolean digit = false;
		boolean symbol = false;
		char[] tmp = pw.toCharArray();
		
		// check
		for (int i = 0; i < tmp.length; i++) {
			if (!digit && Character.isDigit(tmp[i])) { digit = true; continue; }
			if (!lower || !upper && Character.isLetter(tmp[i])) {
				if (!lower && Character.isLowerCase(tmp[i])) { lower = true; continue; }
				if (!upper && Character.isUpperCase(tmp[i])) { upper = true; continue; }
			}
			if (bSymbols && !symbol && !Character.isLetter(tmp[i]) && !Character.isDigit(tmp[i])) { symbol = true; }
		}
		
		// return
		if (lower && upper && digit) {
			if (bSymbols && symbol) { return true; }
			else if (bSymbols && !symbol) { return false; }
			else { return true; }
		}
		return false;

	}
	
	/**
	 * Password Generator
	 * @return the new password
	 */
	private String calcPw() {

		char[] characters = shuffleStrings().toCharArray();
		String password = "";
		
		// Generate random numbers
		List<Integer> rndNums = new ArrayList<Integer>();
		while (rndNums.size() != length)
			rndNums.add(rnd.nextInt(characters.length));
		
		// Generate one password
		while (password.length() != length) {
			password += characters[rndNums.get(0)];
			rndNums.remove(0);
		}
		
		// Make sure to return a valid password
		if (!validatePassword(password)) { return calcPw(); }
		return password;

	}
	
	/**
	 * Combine multiple strings into one large string,
	 * then shuffle positions "shuffTimes" times.
	 * This method is supposed to add extra randomness to the generator.
	 * @return the shuffled string
	 */
	private String shuffleStrings() {

		String shuffStr = LOWER_LETTERS + UPPER_LETTERS + NUMBERS;
		if (bSymbols) {
			shuffStr += SYMBOLS;
		}
		
		// Generate random numbers
		List<Integer> rndNums = new ArrayList<Integer>();
		while (rndNums.size() < shuffTimes) {
			rndNums.add(rnd.nextInt(shuffStr.length()-1));
		}

		// Swap 2 chars
		while (rndNums.size() >= 2) {
			shuffStr = swap(shuffStr, rndNums.get(0), rndNums.get(1));
			rndNums.remove(0);
			rndNums.remove(0);
		}
		
		return shuffStr;

	}

}
