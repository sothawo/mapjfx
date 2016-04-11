/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import java.io.Serializable;

/**
 * A class that keeps information about a cached object. When.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class CachedDataInfo implements Serializable {
    /** the content-type of the data. */
    private String contentType;

    /** the content-encoding. */
    private String contentEncoding;

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
