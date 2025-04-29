package gov.nasa.pds.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class XssUtils {

	private XssUtils() {
	}

	// Patterns for Cross-Site Scripting filter.
	private static Pattern[] xssPatterns = new Pattern[] {
			// script fragments
			Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
			// src='...'
			Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// lonely script tags
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// eval(...)
			Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// expression(...)
			Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// javascript:...
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			// vbscript:...
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			// onload(...)=...
			Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
			// alert(...)
			Pattern.compile("alert\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL) };

	/**
     * This method makes up a simple anti cross-site scripting (XSS) filter written for Java web
     * applications. What it basically does is remove all suspicious strings from request parameters
     * before returning them to the application.
     * 
     * @throws UnsupportedEncodingException
     */
    public static String sanitize(String value) throws UnsupportedEncodingException {
		if (value != null) {
			// Avoid null characters
		    value = URLDecoder.decode(value, "UTF-8");

			// Remove all sections that match a pattern
			for (Pattern scriptPattern : xssPatterns) {
				value = scriptPattern.matcher(value).replaceAll("");
			}

			// After all of the above has been removed just blank out the value
			// if any of the offending characters are present that facilitate
			// Cross-Site Scripting and Blind SQL Injection.
			// We normally exclude () but they often show up in queries.
            char badChars[] = {'\0', '|', ';', '$', '@', '\'', '"', '<', '>', '\\', /* CR */ '\r',
                /* LF */ '\n',
					/* Backspace */ '\b' };
			try {
              for (char badChar : badChars) {
                value.replace(Character.toString(badChar), "");
			  }
			} catch (IllegalArgumentException e) {
				value = "";
			}
		}
		return value;
	}

}
