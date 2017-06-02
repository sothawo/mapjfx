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
 * Custom URLStreamHandlerFactory that is used to implement caching. The factory creates CachingHttpUrlConnection (and
 * https) instances that wrap the original URLConnection elements.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachingURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static final String PROTO_HTTP = "http";
    public static final String PROTO_HTTPS = "https";

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(CachingURLStreamHandlerFactory.class.getCanonicalName());

    /** the cache this instance belongs to. */
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


    /** default Handler for http and https. */
    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        if (null == protocol) {
            throw new IllegalArgumentException("null protocol not allowed");
        }
        logger.finer("need to create URLStreamHandler for protocol " + protocol);

        final String proto = protocol.toLowerCase();
        if (PROTO_HTTP.equals(proto) || PROTO_HTTPS.equals(proto)) {
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL url) throws IOException {
                    logger.finer("should open connection to " + url.toExternalForm());

                    // URLConnection only has a protected ctor, so we need to go through the URL ctor with the matching handler
                    final URLConnection defaultUrlConnection =
                            new URL(protocol, url.getHost(), url.getPort(), url.getFile(), handlers.get(protocol))
                                    .openConnection();


                    if (!cache.urlShouldBeCached(url)) {
                        logger.finer("not using cache for " + url);
                        return defaultUrlConnection;
                    }

                    final Path cacheFile = cache.filenameForURL(url);
                    // now wrap the defaultUrlConnection
                    if (cache.isCached(url)) {
                        // if cached, always use http connection to prevent ssl handshake. As we are reading from the
                        // cache, this is enough
                        return new CachingHttpURLConnection(cache, (HttpURLConnection) defaultUrlConnection);
                    } else {
                        switch (proto) {
                            case PROTO_HTTP:
                                return new CachingHttpURLConnection(cache, (HttpURLConnection) defaultUrlConnection);
                            case PROTO_HTTPS:
                                return new CachingHttpsURLConnection(cache, (HttpsURLConnection) defaultUrlConnection);
                        }
                    }
                    throw new IOException("no matching handler");
                }

                @Override
                protected URLConnection openConnection(final URL u, final Proxy p) throws IOException {
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

}
