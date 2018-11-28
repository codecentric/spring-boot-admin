package de.codecentric.boot.admin.zuul.filters.route;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.AntPathMatcher;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ROUTE_TYPE;

/**
 * Fix zuul incapability to handle the infinite chunked http-stream
 *
 * @author wanghongen
 * 2018/10/27
 */
public class HystrixProxyStreamFilter extends ZuulFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(HystrixProxyStreamFilter.class);

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    public String filterType() {
        return ROUTE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return MATCHER.match("/**/hystrix.stream", RequestContext.getCurrentContext().getRequest().getRequestURI());
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletResponse response = RequestContext.getCurrentContext().getResponse();
        context.setSendZuulResponse(false);


        HttpGet httpget = null;
        InputStream is = null;
        String url = context.getRouteHost().toString() + context.getRequest().getRequestURI();
        LOGGER.info("\n\nProxy opening connection to: " + url + "\n\n");
        try {
            httpget = new HttpGet(url);
            HttpResponse httpResponse = ProxyConnectionManager.httpClient.execute(httpget);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);
            copyHeadersToServletResponse(httpResponse.getAllHeaders(), response);
            if (statusCode == HttpStatus.SC_OK) {
                // writeTo swallows exceptions and never quits even if outputstream is
                // throwing IOExceptions (such as broken pipe) ... since the
                // inputstream is infinite
                // httpResponse.getEntity().writeTo(new
                // OutputStreamWrapper(response.getOutputStream()));
                // so I copy it manually ...
                is = httpResponse.getEntity().getContent();

                OutputStream os = response.getOutputStream();
                int b;
                while ((b = is.read()) != -1) {
                    try {
                        os.write(b);
                        /*  flush buffer on line feed */
                        if (b == 10) {
                            os.flush();
                        }
                    } catch (Exception ex) {
                        if (ex.getClass().getSimpleName()
                                .equalsIgnoreCase("ClientAbortException")) {
                            // don't throw an exception as this means the user closed
                            // the connection
                            LOGGER.debug("Connection closed by client. Will stop proxying ...");
                            // break out of the while loop
                            break;
                        } else {
                            // received unknown error while writing so throw an
                            // exception
                            throw new RuntimeException(ex);
                        }
                    }
                }
            } else {
                String body = EntityUtils.toString(httpResponse.getEntity());
                response.getWriter().append(body).flush();
                LOGGER.warn("Failed opening connection to " + url + " : "
                        + statusCode + " : " + httpResponse.getStatusLine() + " body : " + body);
            }
        } catch (Exception ex) {
            LOGGER.error("Error proxying request: " + url, ex);
        } finally {
            if (httpget != null) {
                try {
                    httpget.abort();
                } catch (Exception ex) {
                    LOGGER.error("failed aborting proxy connection.", ex);
                }
            }

            // httpget.abort() MUST be called first otherwise is.close() hangs
            // (because data is still streaming?)
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignore) {
                }
            }
        }

        LOGGER.info("Hystrix Proxy end. poll stats " + ProxyConnectionManager.connectionManager.getTotalStats());
        return null;
    }

    private void copyHeadersToServletResponse(Header[] headers,
                                              HttpServletResponse response) {
        for (Header header : headers) {
            if (!HttpHeaders.TRANSFER_ENCODING.equalsIgnoreCase(header.getName())) {
                response.addHeader(header.getName(), header.getValue());
            }
        }
    }


    private static class ProxyConnectionManager {

        private final static PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        final static RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(20000)
                .setConnectTimeout(3000)
                .build();
        private final static HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionReuseStrategy(NoConnectionReuseStrategy.INSTANCE)
                .setDefaultRequestConfig(requestConfig)
                .build();

        static {
            LOGGER.debug("Initialize ProxyConnectionManager");
            // 将最大连接数增加到200
            connectionManager.setMaxTotal(200);
            // 将每个路由基础的连接增加到50
            connectionManager.setDefaultMaxPerRoute(100);
        }

    }

}
