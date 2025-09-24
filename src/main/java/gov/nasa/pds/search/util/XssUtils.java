package gov.nasa.pds.search.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public class XssUtils {

	private XssUtils() {
	}

	// Patterns for Cross-Site Scripting filter.
	private static Pattern[] xssPatterns = new Pattern[] {
			// script fragments - complete tags
			Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<script[^>]*>", Pattern.CASE_INSENSITIVE),
			// src='...' and src="..."
			Pattern.compile("src\\s*=\\s*['\"][^'\"]*['\"]", Pattern.CASE_INSENSITIVE),
			// eval(...) and expression(...) - more aggressive patterns
			Pattern.compile("eval\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("expression\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
			// javascript: and vbscript:
			Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
			Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
			// Event handlers - comprehensive pattern
			Pattern.compile("on[a-z]+\\s*=\\s*['\"][^'\"]*['\"]", Pattern.CASE_INSENSITIVE),
			Pattern.compile("on[a-z]+\\s*=\\s*[^\\s>]+", Pattern.CASE_INSENSITIVE),
			// Combined event handlers (like onerroronload)
			Pattern.compile("on[a-z]+on[a-z]+\\s*=\\s*['\"][^'\"]*['\"]", Pattern.CASE_INSENSITIVE),
			Pattern.compile("on[a-z]+on[a-z]+\\s*=\\s*[^\\s>]+", Pattern.CASE_INSENSITIVE),
			// JavaScript functions
			Pattern.compile("alert\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("confirm\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
			Pattern.compile("prompt\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
			// HTML tags - complete tags including closing tags
			Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<iframe[^>]*>.*?</iframe>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			Pattern.compile("<iframe[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("</iframe>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<object[^>]*>.*?</object>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			Pattern.compile("<object[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("</object>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<embed[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<link[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<meta[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<style[^>]*>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			Pattern.compile("<style[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("</style>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("<form[^>]*>.*?</form>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			Pattern.compile("<form[^>]*>", Pattern.CASE_INSENSITIVE),
			Pattern.compile("</form>", Pattern.CASE_INSENSITIVE) };

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
                value = value.replace(Character.toString(badChar), "");
			  }
			} catch (IllegalArgumentException e) {
				value = "";
			}
		}
		return value;
	}

}
