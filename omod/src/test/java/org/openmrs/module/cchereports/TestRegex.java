package org.openmrs.module.cchereports;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {

		String input = "tthen tan";

		Pattern pattern = Pattern.compile("t.*n");
		Matcher matcher = pattern.matcher(String.valueOf(input));
		System.out.println(matcher.find());
		System.out.println(matcher.group());

		/*
		 * while (matcher.find()) { System.out.println(matcher.group() + "Starts at " +
		 * matcher.start() + " and ends at " + matcher.end());
		 * 
		 * }
		 */

	}

	/*
	 * public static void main(String[] args) {
	 * 
	 * String input = "10";
	 * 
	 * Pattern pattern = Pattern.compile("[0-2[6-9]]"); Matcher matcher =
	 * pattern.matcher(String.valueOf(input));
	 * 
	 * System.out.println(matcher.find());
	 * 
	 * }
	 */

}
