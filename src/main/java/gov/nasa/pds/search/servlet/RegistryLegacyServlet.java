package gov.nasa.pds.search.servlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;

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
      new ArrayList<String>(List.of("q", "sort", "start", "rows", "fq", "fl", "wt"));
  private static List<String> REQUEST_HANDLERS =
      new ArrayList<String>(List.of("search", "archive-filter", "select"));
  private static String REQUEST_HANDLER_PARAM = "qt";
  private static String SOLR_BASE_URL = "http://localhost:8983/solr";
  private static String SOLR_COLLECTION = "data";
  private static String DEFAULT_REQUEST_HANDLER = "archive-filter";

  // private static final List<String> SOLR_QUERY_PARAMS = Arrays.asList("defType", "sort", "start",
  // )

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
      // Forward the query to the results page.
      LOG.info("Query: " + request.getQueryString());

      String queryString = getQueryString(request);
      String url = String.format("%s/%s/%s?%s", this.solrServerUrl, this.solrCollection,
              this.solrRequestHandler, queryString);


      HttpClient client = HttpClient.newHttpClient();
      HttpRequest solrRequest = HttpRequest.newBuilder().uri(URI.create(url)).build();


      HttpResponse<String> solrResponse = client.send(solrRequest, BodyHandlers.ofString());

      response.setStatus(solrResponse.statusCode());
      response.addHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");
      response.getOutputStream().write(solrResponse.body().getBytes());
    } catch (Exception e) {
      LOG.severe(e.getMessage());
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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

  private String getQueryString(HttpServletRequest request) throws UnsupportedEncodingException {
    String queryString = "";

    Enumeration<?> parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramKey = String.valueOf(parameterNames.nextElement());
      LOG.info("paramKey: " + paramKey);

      String value = "";
      if (paramKey.equals(REQUEST_HANDLER_PARAM)) {
        value = request.getParameter(REQUEST_HANDLER_PARAM);
        if (REQUEST_HANDLERS.contains(value)) {
          this.solrRequestHandler = value;
        }
      } else if (SOLR_QUERY_PARAMS.contains(paramKey)) {
        value = URLDecoder.decode(request.getParameter(paramKey), "UTF-8");
        queryString +=
            String.format("%s=%s&", paramKey, URLEncoder.encode(value, "UTF-8"));
        LOG.info("QueryString: " + queryString);
      }
    }
    return queryString;
  }
}
