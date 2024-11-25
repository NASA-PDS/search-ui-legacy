<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en"><!-- InstanceBegin template="/Templates/jsp.dwt" codeOutsideHTMLIsLocked="false" -->

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!-- %@ page language="java" session="true" isThreadSafe="true" 
info="PDS Search" isErrorPage="false"
contentType="text/html; charset=ISO-8859-1" 
import="javax.servlet.http.*, jpl.pds.util.*, jpl.pds.beans.*, java.sql.*, java.util.*, java.io.*" %-->
<%
String pdshome = application.getInitParameter("pdshome.url");
String contextPath = request.getContextPath() + "/";
%>

<head>
<!-- InstanceBeginEditable name="doctitle" -->
<title>PDS: Keyword Search</title>
<!-- InstanceEndEditable -->
<c:import url="/includes.html" context="/include" />
<!-- InstanceBeginEditable name="head" --><!-- InstanceEndEditable -->
<!-- InstanceParam name="menu_section" type="text" value="data" -->
<!-- InstanceParam name="menu_item" type="text" value="data_keyword_search" -->
<!-- InstanceParam name="left_sidebar" type="boolean" value="true" -->
<!-- InstanceParam name="standard_sidebar" type="boolean" value="true" -->
<!-- InstanceParam name="standard_page_content" type="boolean" value="true" -->
<!-- InstanceParam name="custom_page_class" type="text" value="" -->
</head>


<body class="sidebar  menu_data menu_item_data_keyword_search ">
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

<!-- Sidebar -->
<div id="sidebar">
<c:import url="/standard_sidebar.html" context="/include" />
  <!-- InstanceBeginEditable name="leftSidebar" -->
  <!-- InstanceEndEditable -->
</div>

<!-- Main content -->
<div id="content">
  <h1><a name="mainContent"></a><!-- InstanceBeginEditable name="pageTitle" -->Keyword Search<!-- InstanceEndEditable --></h1>
  <div id="annual-survey" class="banner">
    <p class="header">New Beta Site is Live!</p>
    <p> See our new search interface here: <a href="https://pds.mcp.nasa.gov/portal/search">https://pds.mcp.nasa.gov/portal/search</a></p>
  </div>
  <div>
    <!-- InstanceBeginEditable name="content" -->
    <p>This interface allows the user to search the holdings of the PDS archive. Results from a search include links to data sets available from the PDS archive. In addition to PDS data, the results also include links to data sets curated by other international space agencies.</p>

    <p>You may also visit the <a href="/datasearch/data-search">Data Search</a> application if you are looking for search tools or other resources.</p>

    <form action="search.jsp" method="get">
      <input type="text" name="q" size="60" />&nbsp;&nbsp;<input type="submit" value="Search" />
    </form>
  
  <div id="IEBug">    
    <p style="margin-top: 2em; margin-bottom: .5em;">
    Begin your search by entering a keyword or keywords in the text box above. 
    The following are some examples of supported keywords and search examples:
    </p>
    <ul>
        <li>A target name like <strong>mars</strong> or <strong>eros</strong> or a target body type like <strong>asteroid</strong></li>
        <li>An instrument name or type like <strong>spectrometer</strong> or <strong>laser altimeter</strong> or <strong>MOLA</strong></li>
        <li>A word or phrase to find in the description of a data set or search tool</li>
        <li>The use of quotation marks to bind words that occur together, e.g. <strong>&quot;mars express&quot;</strong></li>
        <li>The use of logical operator <strong>OR</strong> or <strong>AND</strong>, e.g. <strong>uranus OR neptune</strong></li>
    </ul>

    <p style="margin-top: 2em; margin-bottom: 1em;">
    You may further refine your query by entering a searchable attribute along with a keyword. 
    See the following examples:</p>
    <table id="searchable_attributes">
	<thead>
	    <tr>
		<th>Attribute</th>
		<th>Description</th>
		<th>Example</th>
	    </tr>
	</thead>
	<tbody>
	    <tr>
		<td>identifier</td>
		<td>Search for products with a specific logical identifier (LID).</td>
		<td>identifier:urn:nasa:pds:maven.iuvs.raw:echelle</td>
	    </tr>
	    <tr>
		<td>instrument</td>
		<td>Search for products with a specified instrument name.</td>
		<td>instrument:instrument_name:IMAGING ULTRAVIOLET SPECTROGRAPH</td>
	    </tr>
	    <tr>
		<td>instrument-host</td>
		<td>Search for products with a specified instrument host.</td>
		<td>instrument-host:MAVEN</td>
	    </tr>
	    <tr>
		<td>instrument-host-type</td>
		<td>Search for products with a specified instrument host type.</td>
		<td>instrument-host-type:Spacecraft</td>
	    </tr>
	    <tr>
		<td>instrument-type</td>
		<td>Search for products with a specified instrument type.</td>
		<td>instrument-type:Imager</td>
	    </tr>
	    <tr>
		<td>investigation</td>
		<td>Search for products with a specified investigation (mission) name.</td>
		<td>investigation:Galileo</td>
	    </tr>
	    <tr>
		<td>observing-system</td>
		<td>Search for products with a specified observing system name.</td>
		<td>observing-system:Pathfinder-IMP</td>
	    </tr>
	    <tr>
		<td>product-class</td>
		<td>Search for products with a specific PDS4 Product Class name (Product_Bundle, Product_Collection, etc).</td>
		<td>product-class:Product_Collection</td>
	    </tr>
	    <tr>
		<td>target</td>
		<td>Search for products with a specified target name.</td>
		<td>target:MARS</td>
	    </tr>
	    <tr>
		<td>target-type</td>
		<td>Search for products with a specified target type.</td>
		<td>target-type:Planet</td>
	    </tr>
	    <tr>
		<td>title</td>
		<td>Search for products with a specified value within the title field of a product.</td>
		<td>title:Bundle</td>
	    </tr>
	    <tr>
		<td>start-time</td>
		<td>Search for products within a specified start datetime range. Datetime format should be in ISO-8601 format.</td>
		<td>start-time:[2008-05-01T00:00:00.000z to 2008-06-01T00:00:00.000Z]</td>
	    </tr>
	    <tr>
		<td>stop-time</td>
		<td>Search for products within a specified stop datetime range. Datetime format should be in ISO-8601 format.</td>
		<td>stop-time:[2008-05-01T00:00:00.000z to 2008-06-01T00:00:00.000Z]</td>
	    </tr>
	</tbody>
    </table>

    </div>
    <!-- InstanceEndEditable -->
  </div>
</div>

<c:import url="/footer.html" context="/include" />

    <!--[if IE]>
    </div>
    <![endif]--> 
</body>
<!-- InstanceEnd -->
</html>
