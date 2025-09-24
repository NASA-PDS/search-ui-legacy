package gov.nasa.pds.search.util;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit tests for XSS protection in XssUtils.
 */
public class XssUtilsTest {

    @Test
    public void testBasicXssProtection() throws Exception {
        // Test basic script tag removal
        String malicious = "<script>alert('xss')</script>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testSpecificAttackFromUrl() throws Exception {
        // Test the specific attack from the URL: html<img src onerroronload=confirm(1)>
        String malicious = "html<img src onerroronload=confirm(1)>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("html", result); // Should remove the img tag and event handlers
    }

    @Test
    public void testOnErrorOnLoadAttack() throws Exception {
        // Test onerroronload specifically
        String malicious = "onerroronload=confirm(1)";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testConfirmFunction() throws Exception {
        // Test confirm function removal
        String malicious = "confirm(1)";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testImgTagWithEventHandlers() throws Exception {
        // Test img tag with various event handlers
        String malicious = "<img src='x' onerror='alert(1)'>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testMultipleEventHandlers() throws Exception {
        // Test multiple event handlers
        String malicious = "onclick=alert(1) onmouseover=confirm(1)";
        String result = XssUtils.sanitize(malicious);
        // Event handlers should be removed, leaving only harmless fragments
        assertTrue("Result should not contain 'onclick'", !result.contains("onclick"));
        assertTrue("Result should not contain 'onmouseover'", !result.contains("onmouseover"));
        assertTrue("Result should not contain 'alert'", !result.contains("alert"));
        assertTrue("Result should not contain 'confirm'", !result.contains("confirm"));
    }

    @Test
    public void testJavaScriptProtocol() throws Exception {
        // Test javascript: protocol
        String malicious = "javascript:alert(1)";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testEvalFunction() throws Exception {
        // Test eval function
        String malicious = "eval('alert(1)')";
        String result = XssUtils.sanitize(malicious);
        // The eval function should be removed, leaving only harmless fragments
        assertTrue("Result should not contain 'eval'", !result.contains("eval"));
        assertTrue("Result should not contain 'alert'", !result.contains("alert"));
    }

    @Test
    public void testExpressionFunction() throws Exception {
        // Test expression function
        String malicious = "expression(alert(1))";
        String result = XssUtils.sanitize(malicious);
        // The expression function should be removed, leaving only harmless fragments
        assertTrue("Result should not contain 'expression'", !result.contains("expression"));
        assertTrue("Result should not contain 'alert'", !result.contains("alert"));
    }

    @Test
    public void testIframeTag() throws Exception {
        // Test iframe tag removal
        String malicious = "<iframe src='javascript:alert(1)'></iframe>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testStyleTag() throws Exception {
        // Test style tag removal
        String malicious = "<style>body{background:url('javascript:alert(1)')}</style>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testFormTag() throws Exception {
        // Test form tag removal
        String malicious = "<form onsubmit='alert(1)'></form>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testSafeContent() throws Exception {
        // Test that safe content passes through
        String safe = "normal search query";
        String result = XssUtils.sanitize(safe);
        assertEquals("normal search query", result);
    }

    @Test
    public void testSafeContentWithSpecialChars() throws Exception {
        // Test safe content with special characters that might be in queries
        String safe = "mission:cassini target:saturn";
        String result = XssUtils.sanitize(safe);
        assertEquals("mission:cassini target:saturn", result);
    }

    @Test
    public void testNullInput() throws Exception {
        // Test null input
        String result = XssUtils.sanitize(null);
        assertNull(result);
    }

    @Test
    public void testEmptyInput() throws Exception {
        // Test empty input
        String result = XssUtils.sanitize("");
        assertEquals("", result);
    }

    @Test
    public void testUrlEncodedAttack() throws Exception {
        // Test URL-encoded attack (like in the original URL)
        String malicious = "html%3Cimg%20src%20onerroronload%3Dconfirm(1)%3E";
        String result = XssUtils.sanitize(malicious);
        assertEquals("html", result); // Should decode and clean
    }

    @Test
    public void testCaseInsensitiveAttack() throws Exception {
        // Test case insensitive attacks
        String malicious = "<SCRIPT>ALERT('XSS')</SCRIPT>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }

    @Test
    public void testMixedCaseEventHandlers() throws Exception {
        // Test mixed case event handlers
        String malicious = "OnErRoR=alert(1) OnClIcK=confirm(1)";
        String result = XssUtils.sanitize(malicious);
        // Event handlers should be removed, leaving only harmless fragments
        assertTrue("Result should not contain 'onerror'", !result.toLowerCase().contains("onerror"));
        assertTrue("Result should not contain 'onclick'", !result.toLowerCase().contains("onclick"));
        assertTrue("Result should not contain 'alert'", !result.contains("alert"));
        assertTrue("Result should not contain 'confirm'", !result.contains("confirm"));
    }

    @Test
    public void testBadCharactersRemoval() throws Exception {
        // Test that bad characters are removed
        String malicious = "test<script>alert(1)</script>test";
        String result = XssUtils.sanitize(malicious);
        assertEquals("testtest", result);
    }

    @Test
    public void testComplexAttack() throws Exception {
        // Test a complex multi-vector attack
        String malicious = "<img src='x' onerror='javascript:eval(confirm(1))' onclick='alert(1)'>";
        String result = XssUtils.sanitize(malicious);
        assertEquals("", result);
    }
}
