/*
 Copyright 2015-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.mapjfx.offline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Custom URLStreamHandlerFactory that is used to implement caching. The factory creates CachingHttpUrlConnection (and
 * https) instances that wrap the original URLConnection elements.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
class CachingURLStreamHandlerFactory implements URLStreamHandlerFactory {

    public static final String PROTO_HTTP = "http";
    public static final String PROTO_HTTPS = "https";

    /** Logger for the class */
    private static final Logger logger = LoggerFactory.getLogger(CachingURLStreamHandlerFactory.class);

    /** the cache this instance belongs to. */
    private final OfflineCache cache;

    /** the map with the default handlers for different protocols. */
    private final Map<String, URLStreamHandler> handlers = new ConcurrentHashMap<>();


    /**
     * initializes the {@link #handlers} map with the current handlers for the relevant protocols.
     *
     * @param cache
     *         the cache this instance belongs to
     */
    CachingURLStreamHandlerFactory(final OfflineCache cache) {
        this.cache = cache;

        URLStreamHandler urlStreamHandler = getURLStreamHandler(PROTO_HTTP);
        if (urlStreamHandler != null) {
            handlers.put(PROTO_HTTP, urlStreamHandler);
        }

        urlStreamHandler = getURLStreamHandler(PROTO_HTTPS);
        if (urlStreamHandler != null) {
            handlers.put(PROTO_HTTPS, getURLStreamHandler(PROTO_HTTPS));
        }
    }

    /**
     * returns the URL class's stream handler for a protocol. this uses inspection.
     *
     * @param protocol
     *         the protocol
     * @return the URLStreamHandler, null if it cannot be retrieved.
     */
    private URLStreamHandler getURLStreamHandler(final String protocol) {
        try {
            final Method method = URL.class.getDeclaredMethod("getURLStreamHandler", String.class);
            method.setAccessible(true);
            return (URLStreamHandler) method.invoke(null, protocol);
        } catch (final Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("could not access URL.getUrlStreamHandler for protocol {}", protocol);
            }
            return null;
        }
    }


    /** default Handler for http and https. */
    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
        if (null == protocol) {
            throw new IllegalArgumentException("null protocol not allowed");
        }
        if (logger.isTraceEnabled()) {
            logger.trace("need to create URLStreamHandler for protocol {}", protocol);
        }

        final String proto = protocol.toLowerCase();
        if (PROTO_HTTP.equals(proto) || PROTO_HTTPS.equals(proto)) {
            if (handlers.get(protocol) == null) {
                logger.warn("default protocol handler for protocol {} not available", protocol);
                return null;
            }
            return new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL url) throws IOException {
                    if (logger.isTraceEnabled()) {
                        logger.trace("should open connection to {}", url.toExternalForm());
                    }

                    // URLConnection only has a protected ctor, so we need to go through the URL ctor with the matching handler
                    final URLConnection defaultUrlConnection =
                            new URL(protocol, url.getHost(), url.getPort(), url.getFile(), handlers.get(protocol))
                                    .openConnection();

                    if (!cache.urlShouldBeCached(url)) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("not using cache for {}", url);
                        }
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
                    if (logger.isTraceEnabled()) {
                        logger.trace("should open connection to {} via {}", u.toExternalForm(), p);
                    }
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
