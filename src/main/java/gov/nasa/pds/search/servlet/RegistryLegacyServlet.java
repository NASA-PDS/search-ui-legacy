package gov.nasa.pds.search.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import gov.nasa.pds.search.util.XssUtils;

public class RegistryLegacyServlet extends HttpServlet {

  private static final long serialVersionUID = 6640363130974190821L;

  /** Setup the logger. */
  private static Logger LOG = Logger.getLogger(RegistryLegacyServlet.class.getName());

  /** URL for accessing Solr. */
  private String solrServerUrl;

  /** Solr Collection name */
  private String solrCollection;

  /** Solr Request Handler */
  private String solrRequestHandler;

  private static List<String> SOLR_QUERY_PARAMS =
      new ArrayList<String>(
          List.of("q", "sort", "start", "rows", "fq", "fl", "wt", "json.wrf", "_"));
  private static List<String> SOLR_FACET_FIELDS =
      new ArrayList<String>(List.of("facet_agency", "facet_instrument", "facet_investigation",
          "facet_target", "facet_type", "facet_pds_model_version", "facet_primary_result_purpose",
          "facet_primary_result_processing_level"));
  private static List<String> REQUEST_HANDLERS =
      new ArrayList<String>(List.of("search", "archive-filter", "select"));
  private static String REQUEST_HANDLER_PARAM = "qt";
  private static String SOLR_BASE_URL = "http://localhost:8983/solr";
  private static String SOLR_COLLECTION = "data";
  private static String DEFAULT_REQUEST_HANDLER = "archive-filter";

  /**
   * Constructor for the search servlet.
   */
  public RegistryLegacyServlet() {}

  /**
   * Initialize the servlet.
   *
   * It turns out that we don't need the solrServerUrl property for this implementation, but I left
   * the code in as an example.
   *
   * @param servletConfig The servlet configuration.
   * @throws ServletException
   */
  public void init(ServletConfig servletConfig) throws ServletException {

    // Grab the solrServerUrl parameter from the servlet config.
    this.solrServerUrl = servletConfig.getInitParameter("solrServerUrl");
    if (solrServerUrl == null) {
      this.solrServerUrl = SOLR_BASE_URL;
    }

    this.solrCollection = servletConfig.getInitParameter("solrCollection");
    if (this.solrCollection == null) {
      this.solrCollection = SOLR_COLLECTION;
    }

    this.solrRequestHandler = servletConfig.getInitParameter("solrRequestHandler");
    if (this.solrRequestHandler == null) {
      this.solrRequestHandler = DEFAULT_REQUEST_HANDLER;
    }

    LOG.info("Solr Server URL: " + this.solrServerUrl);

    super.init(servletConfig);
  }

  /**
   * Handle a GET request.
   *
   * @param req The servlet request.
   * @param res The servlet response.
   * @throws ServletException
   * @throws IOException
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    try {

      String queryString = getQueryString(request);
      String url = String.format("%s/%s/%s?%s", this.solrServerUrl, this.solrCollection,
              this.solrRequestHandler, queryString);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest solrRequest = HttpRequest.newBuilder().uri(URI.create(url)).build();


      HttpResponse<String> solrResponse = client.send(solrRequest, BodyHandlers.ofString());

      response.setStatus(solrResponse.statusCode());

      setResponseHeader(request.getParameter("wt"), response);
      response.getOutputStream().write(solrResponse.body().getBytes());
    } catch (Exception e) {
      LOG.severe(e.getMessage());
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal system failure. Contact pds-operator@jpl.nasa.gov for additional assistance.");
    }
  }

  /**
   * Handle a POST request.
   *
   * @param req The servlet request.
   * @param res The servlet response.
   * @throws ServletException
   * @throws IOException
   */
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.sendError(HttpServletResponse.SC_BAD_REQUEST,
        "POST requests are not supported by this API");
  }

  /**
   * Generate query string from subset of allowable query parameters
   * 
   * 
   * @param request
   * @return
   * @throws UnsupportedEncodingException
   */
  private String getQueryString(HttpServletRequest request) throws UnsupportedEncodingException {
    String queryString = "";

    Enumeration<?> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramKey = String.valueOf(parameterNames.nextElement());
      String value = "";

      if (paramKey.equals(REQUEST_HANDLER_PARAM)) {
        value = request.getParameter(REQUEST_HANDLER_PARAM);
        if (REQUEST_HANDLERS.contains(value)) {
          this.solrRequestHandler = value;
        }
      } else if (SOLR_QUERY_PARAMS.contains(paramKey)) {
        queryString += appendQueryParameters(paramKey, request.getParameterValues(paramKey));
      } else if (paramKey.endsWith(".facet.prefix")
          && SOLR_FACET_FIELDS.contains(paramKey.split("\\.")[1])) {
        queryString += appendQueryParameters(paramKey, request.getParameterValues(paramKey));
      } else {
        LOG.warning("Unknown parameter: " + URLEncoder.encode(XssUtils.clean(paramKey), "UTF-8"));
      }
    }

    if (queryString.equals("")) {
      return "q=*:*";
    }

    LOG.info("Solr query: " + queryString);

    return queryString;
  }

  private String appendQueryParameters(String key, String[] parameterValues)
      throws UnsupportedEncodingException {
    String value = "";
    String queryString = "";
    for (String v : Arrays.asList(parameterValues)) {
      value = XssUtils.clean(v);
      LOG.info("key: " + key + " value: " + value);
      queryString +=
          String.format("%s=%s&", key, URLEncoder.encode(value, "UTF-8"));
    }
    return queryString;
  }

  private void setResponseHeader(String wt, HttpServletResponse response) {
    String contentType = "text/html; charset=UTF-8";
    if (wt != null) {
      if (wt.equals("json")) {
        contentType = "application/json; charset=UTF-8";
      } else if (wt.equals("xml")) {
        contentType = "application/xml; charset=UTF-8";
      }
    }
    response.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
  }
}
