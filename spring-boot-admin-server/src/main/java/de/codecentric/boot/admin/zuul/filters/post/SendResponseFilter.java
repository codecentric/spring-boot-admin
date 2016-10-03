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

package de.codecentric.boot.admin.zuul.filters.post;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicIntProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.util.Pair;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.constants.ZuulHeaders;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.util.HTTPRequestUtils;

/**
 * Copy from sprig cloud netflix which is part of a fix for
 * https://github.com/spring-cloud/spring-cloud-netflix/pull/1372
 *
 * @author Johannes Edmeier
 * @author Spencer Gibb
 */
public class SendResponseFilter
		extends org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter {
	private static final Logger log = LoggerFactory.getLogger(SendResponseFilter.class);

	private static DynamicBooleanProperty INCLUDE_DEBUG_HEADER = DynamicPropertyFactory
			.getInstance()
			.getBooleanProperty(ZuulConstants.ZUUL_INCLUDE_DEBUG_HEADER, false);

	private static DynamicIntProperty INITIAL_STREAM_BUFFER_SIZE = DynamicPropertyFactory
			.getInstance()
			.getIntProperty(ZuulConstants.ZUUL_INITIAL_STREAM_BUFFER_SIZE, 1024);

	private static DynamicBooleanProperty SET_CONTENT_LENGTH = DynamicPropertyFactory
			.getInstance()
			.getBooleanProperty(ZuulConstants.ZUUL_SET_CONTENT_LENGTH, false);

	@Override
	public String filterType() {
		return "post";
	}

	@Override
	public int filterOrder() {
		return 1000;
	}

	@Override
	public boolean shouldFilter() {
		return !RequestContext.getCurrentContext().getZuulResponseHeaders().isEmpty()
				|| RequestContext.getCurrentContext().getResponseDataStream() != null
				|| RequestContext.getCurrentContext().getResponseBody() != null;
	}

	@Override
	public Object run() {
		try {
			addResponseHeaders();
			writeResponse();
		}
		catch (Exception ex) {
			ReflectionUtils.rethrowRuntimeException(ex);
		}
		return null;
	}

	private void writeResponse() throws Exception {
		RequestContext context = RequestContext.getCurrentContext();
		// there is no body to send
		if (context.getResponseBody() == null
				&& context.getResponseDataStream() == null) {
			return;
		}
		HttpServletResponse servletResponse = context.getResponse();
		if (servletResponse.getCharacterEncoding() == null) { // only set if not set
			servletResponse.setCharacterEncoding("UTF-8");
		}
		OutputStream outStream = servletResponse.getOutputStream();
		InputStream is = null;
		try {
			if (RequestContext.getCurrentContext().getResponseBody() != null) {
				String body = RequestContext.getCurrentContext().getResponseBody();
				writeResponse(
						new ByteArrayInputStream(
								body.getBytes(servletResponse.getCharacterEncoding())),
						outStream);
				return;
			}
			boolean isGzipRequested = false;
			final String requestEncoding = context.getRequest()
					.getHeader(ZuulHeaders.ACCEPT_ENCODING);

			if (requestEncoding != null
					&& HTTPRequestUtils.getInstance().isGzipped(requestEncoding)) {
				isGzipRequested = true;
			}
			is = context.getResponseDataStream();
			InputStream inputStream = is;
			if (is != null) {
				if (context.sendZuulResponse()) {
					// if origin response is gzipped, and client has not requested gzip,
					// decompress stream
					// before sending to client
					// else, stream gzip directly to client
					if (context.getResponseGZipped() && !isGzipRequested) {
						// If origin tell it's GZipped but the content is ZERO bytes,
						// don't try to uncompress
						final Long len = context.getOriginContentLength();
						if (len == null || len > 0) {
							try {
								inputStream = new GZIPInputStream(is);
							}
							catch (java.util.zip.ZipException ex) {
								log.debug(
										"gzip expected but not "
												+ "received assuming unencoded response "
												+ RequestContext.getCurrentContext()
														.getRequest().getRequestURL()
														.toString());
								inputStream = is;
							}
						}
						else {
							// Already done : inputStream = is;
						}
					}
					else if (context.getResponseGZipped() && isGzipRequested) {
						servletResponse.setHeader(ZuulHeaders.CONTENT_ENCODING, "gzip");
					}
					writeResponse(inputStream, outStream);
				}
			}
		}
		finally {
			try {
				Object zuulResponse = RequestContext.getCurrentContext().get("zuulResponse");
				if (zuulResponse instanceof Closeable) {
					((Closeable) zuulResponse).close();
				}
				outStream.flush();
				// The container will close the stream for us
			}
			catch (IOException ex) {
			}
		}
	}

	private void writeResponse(InputStream zin, OutputStream out) throws Exception {
		byte[] bytes = new byte[INITIAL_STREAM_BUFFER_SIZE.get()];
		int bytesRead = -1;
		while ((bytesRead = zin.read(bytes)) != -1) {
			out.write(bytes, 0, bytesRead);
			out.flush();
			// doubles buffer size if previous read filled it
			if (bytesRead == bytes.length) {
				bytes = new byte[bytes.length * 2];
			}
		}
	}

	private void addResponseHeaders() {
		RequestContext context = RequestContext.getCurrentContext();
		HttpServletResponse servletResponse = context.getResponse();
		List<Pair<String, String>> zuulResponseHeaders = context.getZuulResponseHeaders();
		@SuppressWarnings("unchecked")
		List<String> rd = (List<String>) RequestContext.getCurrentContext()
				.get("routingDebug");
		if (rd != null) {
			StringBuilder debugHeader = new StringBuilder();
			for (String it : rd) {
				debugHeader.append("[[[" + it + "]]]");
			}
			if (INCLUDE_DEBUG_HEADER.get()) {
				servletResponse.addHeader("X-Zuul-Debug-Header", debugHeader.toString());
			}
		}
		if (zuulResponseHeaders != null) {
			for (Pair<String, String> it : zuulResponseHeaders) {
				servletResponse.addHeader(it.first(), it.second());
			}
		}
		RequestContext ctx = RequestContext.getCurrentContext();
		Long contentLength = ctx.getOriginContentLength();
		// Only inserts Content-Length if origin provides it and origin response is not
		// gzipped
		if (SET_CONTENT_LENGTH.get()) {
			if (contentLength != null && !ctx.getResponseGZipped()) {
				servletResponse.setContentLengthLong(contentLength);
			}
		}
	}

}
