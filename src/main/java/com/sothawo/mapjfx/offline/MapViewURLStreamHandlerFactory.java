/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Custom URLStreamHandlerFactory that is used to implement caching. Singleton implementation.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public enum MapViewURLStreamHandlerFactory implements URLStreamHandlerFactory {

    /** the instance. */
    INSTANCE;

    public static final String PROTO_HTTP = "http";
    public static final String PROTO_HTTPS = "https";

    /** the map with the default handlers for different protocols. */
    private Map<String, URLStreamHandler> handlers = new ConcurrentHashMap<>();

// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(MapViewURLStreamHandlerFactory.class.getCanonicalName());

    {
        handlers.put(PROTO_HTTP, getURLStreamHandler(PROTO_HTTP));
        handlers.put(PROTO_HTTPS, getURLStreamHandler(PROTO_HTTPS));
    }

    /**
     * returns the URL class's stream handler for a protocol. WARNING: this relies on inspection! Must be tested with
     * Java 9 Jigsaw!
     *
     * @param protocol
     *         the protocol
     * @return the URLStreamHandler, null if it cannot be retrieved.
     */
    private URLStreamHandler getURLStreamHandler(String protocol) {
        try {
            Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
            method.setAccessible(true);
            return (URLStreamHandler) method.invoke(null, protocol);
        } catch (Exception e) {
            logger.warning("could not access URL.getUrlStreamHandler");
            return null;
        }
    }
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface URLStreamHandlerFactory ---------------------

    /** default Handler for http. */

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (null == protocol) {
            throw new IllegalArgumentException("null protocol not allowed");
        }
        logger.finer("need to create URLStreamHandler for protocol " + protocol);

        if (PROTO_HTTP.equals(protocol.toLowerCase()) || PROTO_HTTPS.equals(protocol.toLowerCase())) {
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    logger.info("should open connection to " + u.toExternalForm());

                    // TODO: check if the content of the URL is already in a cache file
                    // if so, return a URL to the file resource
                    // if the url is not cached read the content, store it and return a file url for the cached content.

                    // URLConnection only has a protected ctor, so we need to go through the URL ctor with the
                    // mathcing handler to get a default implementation of the needed URLSTreamHandler
                    final URLStreamHandler defaultHandler = handlers.get(protocol);
                    final URLConnection defaultUrlConnection =
                            new URL(protocol, u.getHost(), u.getPort(), u.getFile(), defaultHandler).openConnection();

                    // now wrap the default connection
                    switch (protocol) {
                        case PROTO_HTTP:
                            return new MyHttpURLConnection((HttpURLConnection) defaultUrlConnection);
                        case PROTO_HTTPS:
                            return new MyHttpsURLConnection((HttpsURLConnection) defaultUrlConnection);
                    }

                    throw new IOException("no matching handler");
                }
            };
        }
        // return null to use default ones
        return null;
    }
}
