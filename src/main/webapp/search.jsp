<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<!-- InstanceBegin template="/Templates/jsp.dwt" codeOutsideHTMLIsLocked="false" -->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page language="java" session="true" isThreadSafe="true"
	info="PDS Search" isErrorPage="false"
	contentType="text/html; charset=ISO-8859-1"
	import="java.net.*,java.util.*,java.io.*,java.lang.*"%>

<%!
// Blank out the parameter name if any of the bad characters are present
// that facilitate Cross-Site Scripting and Blind SQL Injection.
public String clean(String str) {
  char badChars [] = {'|', ';', '$', '@', '\'', '"', '<', '>', '(', ')', ',', '\\', /* CR */ '\r' , /* LF */ '\n' , /* Backspace */ '\b'};
  String decodedStr = "";

  try {
    if (str != null) {
      decodedStr = URLDecoder.decode(str);
      for(int i = 0; i < badChars.length; i++) {
        if (decodedStr.indexOf(badChars[i]) >= 0) {
          return "";
        }
      }
    }
  } catch (IllegalArgumentException e) {
    return "";
  }
  return decodedStr;
}
%>

<%
String pdshome = application.getInitParameter("pdshome.url");
String contextPath = request.getContextPath() + "/";

String qString = request.getParameter("q");
String query = "", newQString = "";
if (qString == null || qString == "") {
  response.sendRedirect("index.jsp");
  return;
}

// Remove specific parameter included from PDS Header Search
List<String> ignoredParams = new ArrayList<String>();
ignoredParams.add("in");
ignoredParams.add("words");
ignoredParams.add("search_scope");


// Loop through request params to ignore params that come from search
// Allows for parameters to be appended to search-service query
query = "";
Map<String, String[]> params = request.getParameterMap();
for (String name : params.keySet()) {
  name = clean(name);
  if (name != "") {
    if (!ignoredParams.contains(name)) {
      for (String value : Arrays.asList(params.get(name))) {
        if (value != null) {
          query += name + "=" + URLEncoder.encode(value, "UTF-8")+ "&";
        }
      }
    }
  }
}

%>

<head>
<!-- InstanceBeginEditable name="doctitle" -->
<title>PDS: Search Results</title>
<!-- InstanceEndEditable -->
<c:import url="/includes.html" context="/include" />
<!-- InstanceBeginEditable name="head" -->
<!-- InstanceEndEditable -->
<!-- InstanceParam name="menu_section" type="text" value="data" -->
<!-- InstanceParam name="menu_item" type="text" value="data_keyword_search" -->
<!-- InstanceParam name="left_sidebar" type="boolean" value="false" -->
<!-- InstanceParam name="standard_sidebar" type="boolean" value="false" -->
<!-- InstanceParam name="standard_page_content" type="boolean" value="false" -->
<!-- InstanceParam name="custom_page_class" type="text" value="sidebar" -->

<script type="text/javascript"
	src="https://code.jquery.com/jquery.min.js"></script>

<script type="text/javascript">
$(function() {
    //var query = window.location.href.slice(window.location.href.indexOf('?') + 1).split('@@');
    var query = '<%= query %>';
    $.get('/services/search/search?qt=archive-filter&' + query, function(data) {
		$('.output').html(data);
	});
});
</script>

</head>

<body class="fullWidth  menu_data menu_item_data_keyword_search sidebar">
	<!--[if IE]>
<div id="IE">
<![endif]-->

<div id="header-container">
  <c:import url="/header_logo.html" context="/include" />
  <div id="menu-container">
    <c:import url="/main_menu.html" context="/include" />
    <c:import url="/datasearch_menu.html" context="/include" />
  </div>
  <c:import url="/header_links.html" context="/include" />
</div>

	<!-- InstanceBeginEditable name="custom_content" -->

	<!-- Added to contain search service  output -->
	<div class="output"></div>

	<!-- InstanceEndEditable -->

	<c:import url="/footer.html" context="/include" />

	<!--[if IE]>
    </div>
    <![endif]-->
</body>
<!-- InstanceEnd -->
</html>
