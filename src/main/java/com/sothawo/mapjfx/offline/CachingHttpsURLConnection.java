/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ContentHandlerFactory;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Permission;
import java.security.Principal;
import java.security.cert.Certificate;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * HttpsURLConnection implementation that caches the data in a local file, if it is not already stored there.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachingHttpsURLConnection extends HttpsURLConnection {

    /** Logger for the class */
    private static final Logger logger = Logger.getLogger(CachingHttpsURLConnection.class.getCanonicalName());

    /** the delegate original connection. */
    private final HttpsURLConnection delegate;

    /** the file to store the cache data in. */
    private final Path cacheFile;
    /** the file where the meta info is stored. */
    private final Path cacheDataFile;

    /** flag wether to read from the cache file or from the delegate. */
    private boolean readFromCache = false;

    /** the input stream for this object, lazy created. */
    private InputStream inputStream;

    /** info about the cached data. */
    private CachedDataInfo cachedDataInfo;

    public static void setDefaultAllowUserInteraction(boolean defaultallowuserinteraction) {
        URLConnection.setDefaultAllowUserInteraction(defaultallowuserinteraction);
    }

    public static FileNameMap getFileNameMap() {
        return URLConnection.getFileNameMap();
    }

    public static void setDefaultSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
    }

    public static String guessContentTypeFromStream(InputStream is) throws IOException {
        return URLConnection.guessContentTypeFromStream(is);
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        return HttpsURLConnection.getDefaultSSLSocketFactory();
    }

    public static boolean getDefaultAllowUserInteraction() {
        return URLConnection.getDefaultAllowUserInteraction();
    }

    @Deprecated
    public static void setDefaultRequestProperty(String key, String value) {
        URLConnection.setDefaultRequestProperty(key, value);
    }

    public static HostnameVerifier getDefaultHostnameVerifier() {
        return HttpsURLConnection.getDefaultHostnameVerifier();
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

    public static void setDefaultHostnameVerifier(HostnameVerifier hostnameVerifier) {
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    }

    public static void setContentHandlerFactory(ContentHandlerFactory fac) {
        URLConnection.setContentHandlerFactory(fac);
    }

    /**
     * inherited constructor for the HttpURLConnection, private, not to be used.
     *
     * @param u
     *         the URL
     */
    private CachingHttpsURLConnection(URL url) {
        super(url);
        this.delegate = null;
        this.cacheFile = null;
        this.cacheDataFile = null;
    }

    /**
     * creates a CachingHttpsURlConnection.
     *
     * @param cacheFile
     *         the path to the file where the cached content ist stored
     * @param delegate
     *         the delegate that provides the content
     * @throws IOException
     *         if the output file cannot be created, or the input stream from the delegate cannot be retrieved
     */
    public CachingHttpsURLConnection(Path cacheFile, HttpsURLConnection delegate) throws IOException {
        super(delegate.getURL());
        this.delegate = delegate;
        this.cacheFile = cacheFile;
        this.cacheDataFile = Paths.get(cacheFile.toString() + ".dataInfo");

        if (Files.exists(cacheFile) && Files.isReadable(cacheFile) && Files.size(cacheFile) > 0) {
            readFromCache = true;
        }

        if (readFromCache) {
            // get the cached data info
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cacheDataFile.toFile()))) {
                cachedDataInfo = (CachedDataInfo) ois.readObject();
            } catch (Exception e) {
                logger.severe("could not read dataInfo from " + cacheDataFile);
                cachedDataInfo = new CachedDataInfo();
                readFromCache = false;
            }
        } else {
            cachedDataInfo = new CachedDataInfo();
        }

        logger.finer(MessageFormat.format("URL: {0}, cache file: {1}, read from cache: {2}", delegate.getURL()
                .toExternalForm(), cacheFile, readFromCache));
    }

    public void addRequestProperty(String key, String value) {
        delegate.addRequestProperty(key, value);
    }

    public void connect() throws IOException {
        if (!readFromCache) {
            logger.finer("connect to " + delegate.getURL().toExternalForm());
            delegate.connect();
        }
    }

    public void disconnect() {
        if (!readFromCache) {
            delegate.disconnect();
        }
    }

    public boolean getAllowUserInteraction() {
        return delegate.getAllowUserInteraction();
    }

    public String getCipherSuite() {
        return delegate.getCipherSuite();
    }

    public int getConnectTimeout() {
        return readFromCache ? 10 : delegate.getConnectTimeout();
    }

    public Object getContent() throws IOException {
        return delegate.getContent();
    }

    public Object getContent(Class[] classes) throws IOException {
        return delegate.getContent(classes);
    }

    public String getContentEncoding() {
        if (!readFromCache) {
            cachedDataInfo.setContentEncoding(delegate.getContentEncoding());
        }
        return cachedDataInfo.getContentEncoding();
    }

    public int getContentLength() {
        return readFromCache ? -1 : delegate.getContentLength();
    }

    public long getContentLengthLong() {
        return readFromCache ? -1 : delegate.getContentLengthLong();
    }

    public String getContentType() {
        if (!readFromCache) {
            cachedDataInfo.setContentType(delegate.getContentType());
        }
        return cachedDataInfo.getContentType();
    }

    public long getDate() {
        return readFromCache ? 0 : delegate.getDate();
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
        return readFromCache ? 0 : delegate.getExpiration();
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

    public HostnameVerifier getHostnameVerifier() {
        return delegate.getHostnameVerifier();
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
            if (readFromCache) {
                inputStream = new FileInputStream(cacheFile.toFile());
            } else {
                inputStream = new WriteCacheFileInputStream(delegate.getInputStream(),
                        new FileOutputStream(cacheFile.toFile()));
                ((WriteCacheFileInputStream) inputStream).onInputStreamClose(this::saveCacheDataInfo);
            }
        }
        return inputStream;
    }

    /**
     * called when the inputstrem is closed.
     */
    private void saveCacheDataInfo() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cacheDataFile.toFile()))) {
            oos.writeObject(cachedDataInfo);
            oos.flush();
            logger.finer("save dataInfo " + cacheDataFile);
        } catch (Exception e) {
            logger.severe("could not save dataInfo " + cacheDataFile);
        }
    }

    public boolean getInstanceFollowRedirects() {
        return delegate.getInstanceFollowRedirects();
    }

    public long getLastModified() {
        return readFromCache ? 0 : delegate.getLastModified();
    }

    public Certificate[] getLocalCertificates() {
        return delegate.getLocalCertificates();
    }

    public Principal getLocalPrincipal() {
        return delegate.getLocalPrincipal();
    }

    public OutputStream getOutputStream() throws IOException {
        return delegate.getOutputStream();
    }

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        return delegate.getPeerPrincipal();
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
        return readFromCache ? HTTP_OK : delegate.getResponseCode();
    }

    public String getResponseMessage() throws IOException {
        return readFromCache ? "OK" : delegate.getResponseMessage();
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return delegate.getSSLSocketFactory();
    }

    public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
        return delegate.getServerCertificates();
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

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        delegate.setHostnameVerifier(hostnameVerifier);
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

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        delegate.setSSLSocketFactory(sslSocketFactory);
    }

    public void setUseCaches(boolean usecaches) {
        delegate.setUseCaches(usecaches);
    }

    public boolean usingProxy() {
        return delegate.usingProxy();
    }
}
