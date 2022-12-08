package com.example.trying.utils;

public class GFG {

	// Function to convert camel case
	// string to snake case string
	public static String camelToSnake(String str)
	{

		// Empty String
		String result = "";

		// Append first character(in lower case)
		// to result string
		char c = str.charAt(0);
		result = result + Character.toLowerCase(c);

		// Traverse the string from
		// ist index to last index
		for (int i = 1; i < str.length(); i++) {

			char ch = str.charAt(i);

			// Check if the character is upper case
			// then append '_' and such character
			// (in lower case) to result string
			if (Character.isUpperCase(ch)) {
				result = result + '_';
				result
					= result
					+ Character.toLowerCase(ch);
			}

			// If the character is lower case then
			// add such character into result string
			else {
				result = result + ch;
			}
		}

		// return the result
		return convert(result);
	}
	
	public static String convert(String str) {
		int n= str.length();
		String str1= "";
		for(int i= 0; i < n ; i++) {
			if(str.charAt(i) == ' ') {
				str1= str1+ '_';
			}else {
				str1 = str1 + Character.toLowerCase(str.charAt(i));
			}
			
			
		}
		return str1;
	}
	
	

	
}

