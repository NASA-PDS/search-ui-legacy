package gov.nasa.pds.search.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import gov.nasa.pds.search.util.XssUtils;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.client5.http.config.RequestConfig;
import java.io.PrintWriter;


public class RegistryLegacyServlet extends HttpServlet {

  private static final long serialVersionUID = 6640363130974190821L;

  /** Setup the logger. */
  private static final Logger LOG = LoggerFactory.getLogger(RegistryLegacyServlet.class);

  /** URL for accessing Solr. */
  private String solrServerUrl;

  /** Solr Collection name */
  private String solrCollection;

  /** Solr Request Handler */
  private String solrRequestHandler;

  private static List<String> SOLR_QUERY_PARAMS =
      new ArrayList<String>(
    		  Arrays.asList("q", "sort", "start", "rows", "fq", "fl", "wt", "json.wrf", "_", "facet.field",
              "facet", "facet.sort", "facet.mincount", "facet.method", "facet.excludeTerms",
              "facet.pivot", "facet.contains", "facet.limit", "bq", "qf", "mm", "pf", "ps", "tie"));
  private static List<String> SOLR_FACET_FIELDS =
      new ArrayList<String>(Arrays.asList("facet_agency", "facet_instrument", "facet_investigation",
          "facet_target", "facet_type", "facet_pds_model_version", "facet_primary_result_purpose",
          "facet_primary_result_processing_level"));
  private static List<String> REQUEST_HANDLERS =
      new ArrayList<String>(Arrays.asList("search", "archive-filter", "select", "keyword", "all"));
  private static String REQUEST_HANDLER_PARAM = "qt";
  private static String SOLR_BASE_URL = "http://localhost:8983/solr";
  private static String SOLR_COLLECTION = "data";
  private static String DEFAULT_REQUEST_HANDLER = "archive-filter";

  private static final int MAX_TOTAL_CONNECTIONS = 100;
  private static final int MAX_PER_ROUTE = 20;
  private static final int VALIDATE_AFTER_INACTIVITY_MS = 2000;
  private static final int CONNECTION_TIMEOUT_MS = 5000;
  private static final int SOCKET_TIMEOUT_MS = 10000;

  private PoolingHttpClientConnectionManager connectionManager;
  private CloseableHttpClient httpClient;

  /**
   * Initialize the servlet.
   *
   * It turns out that we don't need the solrServerUrl property for this implementation, but I left
   * the code in as an example.
   *
   * @param servletConfig The servlet configuration.
   * @throws ServletException
   */
  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);

    // Initialize connection manager with eviction policy
    connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
    connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);
    connectionManager.setValidateAfterInactivity(TimeValue.ofMilliseconds(VALIDATE_AFTER_INACTIVITY_MS));

    // Create HTTP client with connection pool
    httpClient = HttpClients.custom()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(CONNECTION_TIMEOUT_MS))
            .setResponseTimeout(Timeout.ofMilliseconds(SOCKET_TIMEOUT_MS))
            .build())
        .build();

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

    LOG.debug("Solr Server URL: {}", this.solrServerUrl);
  }

  @Override
  public void destroy() {
    try {
      if (httpClient != null) {
        httpClient.close();
      }
      if (connectionManager != null) {
        connectionManager.close();
      }
    } catch (IOException e) {
      LOG.error("Error closing HTTP client resources", e);
    }
    super.destroy();
  }

  /**
   * Handle a GET request.
   *
   * @param req The servlet request.
   * @param res The servlet response.
   * @throws ServletException
   * @throws IOException
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      String queryString = getQueryString(request);
      String url = String.format("%s/%s/%s?%s", this.solrServerUrl, this.solrCollection,
              this.solrRequestHandler, queryString);

      ClassicHttpRequest httpGet = ClassicRequestBuilder.get(url).build();
      String resultContent = null;
      try (CloseableHttpResponse solrResponse = httpClient.execute(httpGet)) {
        response.setStatus(solrResponse.getCode());
        setResponseHeader(request.getParameter("wt"), response);
        HttpEntity entity = solrResponse.getEntity();
        if (entity != null) {
          try {
            resultContent = EntityUtils.toString(entity);
          } finally {
            EntityUtils.consume(entity);
          }
        }
      }
      if (resultContent != null) {
        try (PrintWriter writer = response.getWriter()) {
          writer.write(resultContent);
        }
      }
    } catch (IOException e) {
      LOG.error("Error processing request", e);
    }
    } catch (Exception e) {
      LOG.error("Error processing request", e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Internal system failure. Contact pds-operator@jpl.nasa.gov for additional assistance.");
      } catch (IOException ioe) {
        LOG.error("Failed to send error response", ioe);
      }
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
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    try {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST,
          "POST requests are not supported by this API");
    } catch (IOException e) {
      LOG.error("Failed to send error response for POST request", e);
    }
  }

  /**
   * Generate query string from subset of allowable query parameters
   * 
   * 
   * @param request
   * @return
   * @throws UnsupportedEncodingException
   */
  private String getQueryString(HttpServletRequest request) {
    String queryString = "";

    try {
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
          if (LOG.isWarnEnabled()) {
            LOG.warn("Unknown parameter: {}", URLEncoder.encode(XssUtils.clean(paramKey), "UTF-8"));
          }
        }
      }

      if (queryString.equals("")) {
        queryString = "q=*:*";
      }

      LOG.info("Solr query: {}", queryString);

      return queryString;
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error encoding query parameters", e);
      return "q=*:*";
    }
  }

  private String appendQueryParameters(String key, String[] parameterValues) {
    String value = "";
    String queryString = "";
    try {
      for (String v : Arrays.asList(parameterValues)) {
        LOG.info("{} : {}", key, v);
        value = XssUtils.sanitize(v);
        queryString += String.format("%s=%s&", key, URLEncoder.encode(value, "UTF-8"));
      }
      return queryString;
    } catch (UnsupportedEncodingException e) {
      LOG.error("Error encoding parameter value", e);
      return "";
    }
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

