package com.youfuns.ecommerce.frontend.utils;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.RequestContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * Bridge between Apache Commons FileUpload and Java's HttpExchange.
 */
public class ApacheHttpExchangeContext implements RequestContext {
    private final HttpExchange exchange;

    public ApacheHttpExchangeContext(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public String getCharacterEncoding() {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null) {
            return "UTF-8";
        }
        // Extract charset from Content-Type header
        String[] parts = contentType.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.toLowerCase().startsWith("charset=")) {
                return trimmed.substring(8);
            }
        }
        return "UTF-8";
    }

    @Override
    public String getContentType() {
        return exchange.getRequestHeaders().getFirst("Content-Type");
    }

    @Override
    public int getContentLength() {
        String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
        if (contentLength == null) {
            return -1;
        }
        try {
            return Integer.parseInt(contentLength);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return exchange.getRequestBody();
    }
}