package com.network.util;

public class MessageDecoder {

	// "id:type.value0,value1,...,valueN;"
	
	public static String getSenderId(String message) {
		return message.substring(0, message.indexOf(":"));
	}

	public static String getType(String message) {
		return message.substring(message.indexOf(":") + 1, message.indexOf("."));
	}

	public static int getIntValue(String message, int index) {
		return Integer.parseInt(getStrValue(message, index));
	}

	public static String getStrValue(String message, int index) {
		if (index < 0) {
			throw new ValueIndexOutOfBoundsException("Value index must not be less than 0, " + index + " is.");

		}
		String value = message.substring(message.indexOf(".") + 1, message.indexOf(";"));

		int counter = 0;
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == ',')
				counter++;
		}
		if (index > counter) {
			throw new ValueIndexOutOfBoundsException("Maximum value index of the message \"" + message + "\" is "
					+ counter + ", " + index + " is too big.");
		}

		if (value.contains(",")) {
			for (int i = 0; i < index; i++) {
				value = value.substring(value.indexOf(",") + 1, value.length());
			}
			if (value.contains(","))
				value = value.substring(0, value.indexOf(","));
		}
		return value;
	}
}
