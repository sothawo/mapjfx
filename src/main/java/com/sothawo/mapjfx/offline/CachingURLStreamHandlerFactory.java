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
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Custom URLStreamHandlerFactory that is used to implement caching. sThe factory creates CachingHttpUrlConnection (and
 * https) instances that wrap the original URLConnection elements.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachingURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static final String PROTO_HTTP = "http";
    public static final String PROTO_HTTPS = "https";

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(CachingURLStreamHandlerFactory.class.getCanonicalName());

    /** the cache this instance blegons to. */
    private final OfflineCache cache;

    /** the map with the default handlers for different protocols. */
    private Map<String, URLStreamHandler> handlers = new ConcurrentHashMap<>();


    /**
     * initializes the {@link #handlers} map with the current handlers for the relevant protocols.
     *
     * @param cache
     *         the cache this instance belongs to
     */
    CachingURLStreamHandlerFactory(OfflineCache cache) {
        this.cache = cache;
        handlers.put(PROTO_HTTP, getURLStreamHandler(PROTO_HTTP));
        handlers.put(PROTO_HTTPS, getURLStreamHandler(PROTO_HTTPS));
    }

    /**
     * returns the URL class's stream handler for a protocol. this uses inspection.
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


    /** default Handler for http. */
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (null == protocol) {
            throw new IllegalArgumentException("null protocol not allowed");
        }
        logger.finer("need to create URLStreamHandler for protocol " + protocol);

        final String proto = protocol.toLowerCase();
        if (PROTO_HTTP.equals(proto) || PROTO_HTTPS.equals(proto)) {
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL u) throws IOException {
                    logger.finer("should open connection to " + u.toExternalForm());

                    // URLConnection only has a protected ctor, so we need to go through the URL ctor with the
                    // matching handler to get a default implementation of the needed URLStreamHandler
                    final URLConnection defaultUrlConnection =
                            new URL(protocol, u.getHost(), u.getPort(), u.getFile(), handlers.get(protocol))
                                    .openConnection();

                    if (urlShouldBeCached(u)) {

                        // now wrap the default connection
                        final Path cacheFile = cache.filenameForURL(u);
                        switch (proto) {
                            case PROTO_HTTP:
                                return new CachingHttpURLConnection(cacheFile,
                                        (HttpURLConnection) defaultUrlConnection);
                            case PROTO_HTTPS:
                                return new CachingHttpURLConnection(cacheFile,
                                        (HttpURLConnection) defaultUrlConnection);
                        }
                        throw new IOException("no matching handler");
                    }

                    return defaultUrlConnection;
                }

                @Override
                protected URLConnection openConnection(URL u, Proxy p) throws IOException {
                    logger.finer("should open connection to " + u.toExternalForm() + " via " + p.toString());
                    // URLConnection only has a protected ctor, so we need to go through the URL ctor with the
                    // matching handler to get a default implementation of the needed URLStreamHandler
                    final URLConnection defaultUrlConnection =
                            new URL(protocol, u.getHost(), u.getPort(), u.getFile(), handlers.get(protocol))
                                    .openConnection(p);

                    return defaultUrlConnection;
                }
            };
        }
        // return null to use default ones
        return null;
    }

    /**
     * checks wether a URL should be cached at all.
     *
     * @param u
     *         the ULR to check
     * @return true if the URL should be cached.
     */
    private boolean urlShouldBeCached(URL u) {
        return true;
    }
}
