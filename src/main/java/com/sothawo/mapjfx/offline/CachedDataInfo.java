/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * A class that keeps information about a cached object.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachedDataInfo implements Serializable {

    /** the content-type of the data. */
    private String contentType;

    /** the content-encoding. */
    private String contentEncoding;

    /** the response headers. */
    private Map<String,List<String>> headerFields;

    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
