package utils;

public class StringUtils {

	public static String truncate(String message) {
		if (message.length() > 10)
			return message.substring(0, 11).concat("...");
		else
			return message;
	}

}
