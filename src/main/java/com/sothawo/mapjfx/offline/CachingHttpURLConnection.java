/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ContentHandlerFactory;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.security.Permission;
import java.util.List;
import java.util.Map;

/**
 * HttpURLConnection implementation that caches the data in a local file, if it is not already stored there.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachingHttpURLConnection extends HttpURLConnection {

    /** the delegate original connection. */
    private final HttpURLConnection delegate;

    /** the file to store the cache data in. */
    private final Path cacheFile;

    /** the input stream for this object, lazy created. */
    private InputStream inputStream;

    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
        URLConnection.setDefaultAllowUserInteraction(defaultallowuserinteraction);
    }

    public static FileNameMap getFileNameMap() {
        return URLConnection.getFileNameMap();
    }

    public static String guessContentTypeFromStream(InputStream is) throws IOException {
        return URLConnection.guessContentTypeFromStream(is);
    }

    public static boolean getDefaultAllowUserInteraction() {
        return URLConnection.getDefaultAllowUserInteraction();
    }

    @Deprecated
    public static void setDefaultRequestProperty(String key, String value) {
        URLConnection.setDefaultRequestProperty(key, value);
    }

    public static void setFollowRedirects(boolean set) {
        HttpURLConnection.setFollowRedirects(set);
    }

    public static boolean getFollowRedirects() {
        return HttpURLConnection.getFollowRedirects();
    }

    public static void setFileNameMap(FileNameMap map) {
        URLConnection.setFileNameMap(map);
    }

    @Deprecated
    public static String getDefaultRequestProperty(String key) {
        return URLConnection.getDefaultRequestProperty(key);
    }

    public static String guessContentTypeFromName(String fname) {
        return URLConnection.guessContentTypeFromName(fname);
    }

    public static void setContentHandlerFactory(ContentHandlerFactory fac) {
        URLConnection.setContentHandlerFactory(fac);
    }

    /**
     * inherited constructor for the HttpURLConnection, private.
     *
     * @param u
     *         the URL
     */
    private CachingHttpURLConnection(URL u) {
        super(u);
        this.delegate = null;
        this.cacheFile = null;
    }

    /**
     * creates a CachingHttpURlConnection.
     *
     * @param cacheFile
     *         the path to the file where the cached content ist stored
     * @param delegate
     *         the delegate that provides the content
     */
    public CachingHttpURLConnection(Path cacheFile, HttpURLConnection delegate) {
        super(delegate.getURL());
        this.delegate = delegate;
        this.cacheFile = cacheFile;
    }

    public void addRequestProperty(String key, String value) {
        delegate.addRequestProperty(key, value);
    }

    public void connect() throws IOException {
        delegate.connect();
    }

    public void disconnect() {
        delegate.disconnect();
    }

    public boolean getAllowUserInteraction() {
        return delegate.getAllowUserInteraction();
    }

    public int getConnectTimeout() {
        return delegate.getConnectTimeout();
    }

    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    public Object getContent(Class[] classes) throws IOException {
        return delegate.getContent(classes);
    }

    public String getContentEncoding() {
        return delegate.getContentEncoding();
    }

    public int getContentLength() {
        return delegate.getContentLength();
    }

    public long getContentLengthLong() {
        return delegate.getContentLengthLong();
    }

    public String getContentType() {
        return delegate.getContentType();
    }

    public long getDate() {
        return delegate.getDate();
    }

    public boolean getDefaultUseCaches() {
        return delegate.getDefaultUseCaches();
    }

    public boolean getDoInput() {
        return delegate.getDoInput();
    }

    public boolean getDoOutput() {
        return delegate.getDoOutput();
    }

    public InputStream getErrorStream() {
        return delegate.getErrorStream();
    }

    public long getExpiration() {
        return delegate.getExpiration();
    }

    public String getHeaderField(int n) {
        return delegate.getHeaderField(n);
    }

    public String getHeaderField(String name) {
        return delegate.getHeaderField(name);
    }

    public long getHeaderFieldDate(String name, long Default) {
        return delegate.getHeaderFieldDate(name, Default);
    }

    public int getHeaderFieldInt(String name, int Default) {
        return delegate.getHeaderFieldInt(name, Default);
    }

    public String getHeaderFieldKey(int n) {
        return delegate.getHeaderFieldKey(n);
    }

    public long getHeaderFieldLong(String name, long Default) {
        return delegate.getHeaderFieldLong(name, Default);
    }

    public Map<String, List<String>> getHeaderFields() {
        return delegate.getHeaderFields();
    }

    public long getIfModifiedSince() {
        return delegate.getIfModifiedSince();
    }

    /**
     * return the delegate's InputStream wrapped in a {@link WriteCacheFileInputStream} or a FileInputStream in case
     * when the data is already cached.
     *
     * @return wrapping InputStream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (null == inputStream) {
            inputStream = new WriteCacheFileInputStream(delegate.getInputStream(), new FileOutputStream(cacheFile
                    .toFile()));
        }
        return inputStream;
    }

    public boolean getInstanceFollowRedirects() {
        return delegate.getInstanceFollowRedirects();
    }

    public long getLastModified() {
        return delegate.getLastModified();
    }

    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    public Permission getPermission() throws IOException {
        return delegate.getPermission();
    }

    public int getReadTimeout() {
        return delegate.getReadTimeout();
    }

    public String getRequestMethod() {
        return delegate.getRequestMethod();
    }

    public Map<String, List<String>> getRequestProperties() {
        return delegate.getRequestProperties();
    }

    public String getRequestProperty(String key) {
        return delegate.getRequestProperty(key);
    }

    public int getResponseCode() throws IOException {
        return delegate.getResponseCode();
    }

    public String getResponseMessage() throws IOException {
        return delegate.getResponseMessage();
    }

    public URL getURL() {
        return delegate.getURL();
    }

    public boolean getUseCaches() {
        return delegate.getUseCaches();
    }

    public void setAllowUserInteraction(boolean allowuserinteraction) {
        delegate.setAllowUserInteraction(allowuserinteraction);
    }

    public void setChunkedStreamingMode(int chunklen) {
        delegate.setChunkedStreamingMode(chunklen);
    }

    public void setConnectTimeout(int timeout) {
        delegate.setConnectTimeout(timeout);
    }

    public void setDefaultUseCaches(boolean defaultusecaches) {
        delegate.setDefaultUseCaches(defaultusecaches);
    }

    public void setDoInput(boolean doinput) {
        delegate.setDoInput(doinput);
    }

    public void setDoOutput(boolean dooutput) {
        delegate.setDoOutput(dooutput);
    }

    public void setFixedLengthStreamingMode(long contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }

    public void setFixedLengthStreamingMode(int contentLength) {
        delegate.setFixedLengthStreamingMode(contentLength);
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        delegate.setIfModifiedSince(ifmodifiedsince);
    }

    public void setInstanceFollowRedirects(boolean followRedirects) {
        delegate.setInstanceFollowRedirects(followRedirects);
    }

    public void setReadTimeout(int timeout) {
        delegate.setReadTimeout(timeout);
    }

    public void setRequestMethod(String method) throws ProtocolException {
        delegate.setRequestMethod(method);
    }

    public void setRequestProperty(String key, String value) {
        delegate.setRequestProperty(key, value);
    }

    public void setUseCaches(boolean usecaches) {
        delegate.setUseCaches(usecaches);
    }

    public boolean usingProxy() {
        return delegate.usingProxy();
    }
}
