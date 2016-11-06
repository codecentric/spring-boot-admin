/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.boot.admin.zuul.filters.route;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.Host;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;

/**
 * Copy from sprig cloud netflix which is part of a fix for
 * https://github.com/spring-cloud/spring-cloud-netflix/pull/1372
 *
 * @author Johannes Edmeier
 * @author Spencer Gibb
 */
public class SimpleHostRoutingFilter extends ZuulFilter {
	private static final Logger log = LoggerFactory.getLogger(SimpleHostRoutingFilter.class);

	private static final DynamicIntProperty SOCKET_TIMEOUT = DynamicPropertyFactory
			.getInstance()
			.getIntProperty(ZuulConstants.ZUUL_HOST_SOCKET_TIMEOUT_MILLIS, 10000);

	private static final DynamicIntProperty CONNECTION_TIMEOUT = DynamicPropertyFactory
			.getInstance()
			.getIntProperty(ZuulConstants.ZUUL_HOST_CONNECT_TIMEOUT_MILLIS, 2000);
	private static final String ERROR_STATUS_CODE = "error.status_code";

	private final Timer connectionManagerTimer = new Timer(
			"SimpleHostRoutingFilter.connectionManagerTimer", true);

	private boolean sslHostnameValidationEnabled;

	private ProxyRequestHelper helper;
	private Host hostProperties;
	private PoolingHttpClientConnectionManager connectionManager;
	private CloseableHttpClient httpClient;

	private final Runnable clientloader = new Runnable() {
		@Override
		public void run() {
			try {
				SimpleHostRoutingFilter.this.httpClient.close();
			}
			catch (IOException ex) {
				log.error("error closing client", ex);
			}
			SimpleHostRoutingFilter.this.httpClient = newClient();
		}
	};

	public SimpleHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties properties) {
		this.helper = helper;
		this.hostProperties = properties.getHost();
		this.sslHostnameValidationEnabled = properties.isSslHostnameValidationEnabled();
	}

	@PostConstruct
	private void initialize() {
		this.httpClient = newClient();
		SOCKET_TIMEOUT.addCallback(this.clientloader);
		CONNECTION_TIMEOUT.addCallback(this.clientloader);
		this.connectionManagerTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (SimpleHostRoutingFilter.this.connectionManager == null) {
					return;
				}
				SimpleHostRoutingFilter.this.connectionManager.closeExpiredConnections();
			}
		}, 30000, 5000);
	}

	@PreDestroy
	public void stop() {
		this.connectionManagerTimer.cancel();
	}

	@Override
	public String filterType() {
		return "route";
	}

	@Override
	public int filterOrder() {
		return 100;
	}

	@Override
	public boolean shouldFilter() {
		return RequestContext.getCurrentContext().getRouteHost() != null
				&& RequestContext.getCurrentContext().sendZuulResponse();
	}

	@Override
	public Object run() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletRequest request = context.getRequest();
		MultiValueMap<String, String> headers = this.helper
				.buildZuulRequestHeaders(request);
		MultiValueMap<String, String> params = this.helper
				.buildZuulRequestQueryParams(request);
		String verb = getVerb(request);
		InputStream requestEntity = getRequestBody(request);
		if (request.getContentLength() < 0) {
			context.setChunkedRequestBody();
		}

		String uri = this.helper.buildZuulRequestURI(request);
		this.helper.addIgnoredHeaders();

		try {
			CloseableHttpResponse response = forward(this.httpClient, verb, uri, request, headers,
					params, requestEntity);
			setResponse(response);
		}
		catch (Exception ex) {
			context.set(ERROR_STATUS_CODE, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			context.set("error.exception", ex);
		}
		return null;
	}

	protected PoolingHttpClientConnectionManager newConnectionManager() {
		try {
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates,
						String s) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates,
						String s) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} }, new SecureRandom());

			RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
					.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE);
			if (this.sslHostnameValidationEnabled) {
				registryBuilder.register("https",
						new SSLConnectionSocketFactory(sslContext));
			}
			else {
				registryBuilder.register("https", new SSLConnectionSocketFactory(
						sslContext, NoopHostnameVerifier.INSTANCE));
			}
			final Registry<ConnectionSocketFactory> registry = registryBuilder.build();

			this.connectionManager = new PoolingHttpClientConnectionManager(registry);
			this.connectionManager
					.setMaxTotal(this.hostProperties.getMaxTotalConnections());
			this.connectionManager.setDefaultMaxPerRoute(
					this.hostProperties.getMaxPerRouteConnections());
			return this.connectionManager;
		}
		catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	protected CloseableHttpClient newClient() {
		final RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(SOCKET_TIMEOUT.get())
				.setConnectTimeout(CONNECTION_TIMEOUT.get())
				.setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();

		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		if (!this.sslHostnameValidationEnabled) {
			httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
		}
		return httpClientBuilder.setConnectionManager(newConnectionManager())
				.useSystemProperties().setDefaultRequestConfig(requestConfig)
				.setRetryHandler(new DefaultHttpRequestRetryHandler(0, false))
				.setRedirectStrategy(new RedirectStrategy() {
					@Override
					public boolean isRedirected(HttpRequest request,
							HttpResponse response, HttpContext context)
							throws ProtocolException {
						return false;
					}

					@Override
					public HttpUriRequest getRedirect(HttpRequest request,
							HttpResponse response, HttpContext context)
							throws ProtocolException {
						return null;
					}
				}).build();
	}

	private CloseableHttpResponse forward(CloseableHttpClient httpclient, String verb, String uri,
			HttpServletRequest request, MultiValueMap<String, String> headers,
			MultiValueMap<String, String> params, InputStream requestEntity)
			throws Exception {
		Map<String, Object> info = this.helper.debug(verb, uri, headers, params,
				requestEntity);
		URL host = RequestContext.getCurrentContext().getRouteHost();
		HttpHost httpHost = getHttpHost(host);
		uri = StringUtils.cleanPath((host.getPath() + uri).replaceAll("/{2,}", "/"));
		HttpRequest httpRequest;
		int contentLength = request.getContentLength();
		InputStreamEntity entity = new InputStreamEntity(requestEntity, contentLength,
				request.getContentType() != null
						? ContentType.create(request.getContentType()) : null);
		switch (verb.toUpperCase()) {
		case "POST":
			HttpPost httpPost = new HttpPost(uri + this.helper.getQueryString(params));
			httpRequest = httpPost;
			httpPost.setEntity(entity);
			break;
		case "PUT":
			HttpPut httpPut = new HttpPut(uri + this.helper.getQueryString(params));
			httpRequest = httpPut;
			httpPut.setEntity(entity);
			break;
		case "PATCH":
			HttpPatch httpPatch = new HttpPatch(uri + this.helper.getQueryString(params));
			httpRequest = httpPatch;
			httpPatch.setEntity(entity);
			break;
		default:
			httpRequest = new BasicHttpRequest(verb,
					uri + this.helper.getQueryString(params));
			log.debug(uri + this.helper.getQueryString(params));
		}
		try {
			httpRequest.setHeaders(convertHeaders(headers));
			log.debug(httpHost.getHostName() + " " + httpHost.getPort() + " "
					+ httpHost.getSchemeName());
			CloseableHttpResponse zuulResponse = forwardRequest(httpclient, httpHost, httpRequest);
			RequestContext.getCurrentContext().set("zuulResponse", zuulResponse);
			this.helper.appendDebug(info, zuulResponse.getStatusLine().getStatusCode(),
					revertHeaders(zuulResponse.getAllHeaders()));
			return zuulResponse;
		}
		finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			// httpclient.getConnectionManager().shutdown();
		}
	}

	private MultiValueMap<String, String> revertHeaders(Header[] headers) {
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		for (Header header : headers) {
			String name = header.getName();
			if (!map.containsKey(name)) {
				map.put(name, new ArrayList<String>());
			}
			map.get(name).add(header.getValue());
		}
		return map;
	}

	private Header[] convertHeaders(MultiValueMap<String, String> headers) {
		List<Header> list = new ArrayList<>();
		for (String name : headers.keySet()) {
			for (String value : headers.get(name)) {
				list.add(new BasicHeader(name, value));
			}
		}
		return list.toArray(new BasicHeader[0]);
	}

	private CloseableHttpResponse forwardRequest(CloseableHttpClient httpclient, HttpHost httpHost,
			HttpRequest httpRequest) throws IOException {
		return httpclient.execute(httpHost, httpRequest);
	}

	private HttpHost getHttpHost(URL host) {
		HttpHost httpHost = new HttpHost(host.getHost(), host.getPort(),
				host.getProtocol());
		return httpHost;
	}

	private InputStream getRequestBody(HttpServletRequest request) {
		InputStream requestEntity = null;
		try {
			requestEntity = request.getInputStream();
		}
		catch (IOException ex) {
			// no requestBody is ok.
		}
		return requestEntity;
	}

	private String getVerb(HttpServletRequest request) {
		String sMethod = request.getMethod();
		return sMethod.toUpperCase();
	}

	private void setResponse(HttpResponse response) throws IOException {
		this.helper.setResponse(response.getStatusLine().getStatusCode(),
				response.getEntity() == null ? null : response.getEntity().getContent(),
				revertHeaders(response.getAllHeaders()));
	}

	/**
	 * Add header names to exclude from proxied response in the current request.
	 * @param names
	 */
	protected void addIgnoredHeaders(String... names) {
		this.helper.addIgnoredHeaders(names);
	}

	/**
	 * Determines whether the filter enables the validation for ssl hostnames.
	 * @return true if enabled
	 */
	boolean isSslHostnameValidationEnabled() {
		return this.sslHostnameValidationEnabled;
	}
}
