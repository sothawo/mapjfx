/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Offline Cache functionality. The cache must be explicitly set to be active. If it is active, a call to a http or
 * https resource is intercepted. If the reuslt of the same call is already stored in the local cache directory, it is
 * returned without a further connect to the network. If it is not in the cache directory, a network request is made and
 * the returned data is stored in the local cache directory.
 *
 * todo: honor the active flag
 *
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class OfflineCache {

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(OfflineCache.class.getCanonicalName());

    /** flag if the cache is active. */
    private boolean active = false;

    /** flag if the URLStreamHandlerfactory is initialized. */
    private boolean urlStreamHandlerFactoryIsInitialized = false;

    /** the cache directory. */
    private Path cacheDirectory;


    public Path getCacheDirectory() {
        return cacheDirectory;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * sets the active state of the cache
     *
     * @param active
     *         new state
     * @throws IllegalArgumentException
     *         if active is true and no cacheDirectory has been set.
     * @throws IllegalStateException
     *         if the factory cannot be initialized.
     */
    public void setActive(boolean active) {
        if (active && null == cacheDirectory) {
            throw new IllegalArgumentException("cannot setactive when no cacheDirectory is set");
        }
        if (active) {
            setupURLStreamHandlerFactory();
        }
        this.active = active;
    }

    /**
     * sets the cacheDirectory.
     *
     * @param cacheDirectory
     *         the new directory
     * @throws NullPointerException
     *         if cacheDirectory is null
     * @throws IllegalArgumentException
     *         if cacheDirectory does not exist or is not writeable
     */
    public void setCacheDirectory(Path cacheDirectory) {
        Path dir = Objects.requireNonNull(cacheDirectory);
        if (!Files.isDirectory(dir) || !Files.isWritable(dir)) {
            throw new IllegalArgumentException("cacheDirectory");
        }
        this.cacheDirectory = dir;
    }

    /**
     * sets up the URLStreamHandlerFactory.
     *
     * @throws IllegalStateException
     *         if the factory cannot be initialized.
     */
    private void setupURLStreamHandlerFactory() {
        if (!urlStreamHandlerFactoryIsInitialized) {
            String msg;
            try {
                URL.setURLStreamHandlerFactory(new CachingURLStreamHandlerFactory(this));
                urlStreamHandlerFactoryIsInitialized = true;
                return;
            } catch (Error e) {
                msg = "cannot setup URLStreamFactoryHandler, it is already set in this application. " + e.getMessage();
                logger.warning(msg);
            } catch (SecurityException e) {
                msg = "cannot setup URLStreamFactoryHandler. " + e.getMessage();
                logger.severe(msg);
            }
            throw new IllegalStateException(msg);
        }
    }

    /**
     * returns the filename path for a cachefile
     *
     * @param url
     *         the url to store in the cache
     * @return the filename path for the url
     * @throws IllegalStateException
     *         if no cacheDirectory is set.
     * @throws UnsupportedEncodingException
     *         if the url cannot be UTF-8 encoded
     */
    public Path filenameForURL(URL url) throws UnsupportedEncodingException {
        if (null == cacheDirectory) {
            throw new IllegalStateException("cannot resolve filename for url");
        }
        return cacheDirectory.resolve(URLEncoder.encode(Objects.requireNonNull(url.toExternalForm()), "UTF-8"));
    }

}
