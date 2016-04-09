/**
 * Copyright (c) 2016 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.mapjfx.offline;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * FilterInputStream that dumps all data passed through it to a cache file before passing the data on.
 *
 * todo: implement cache writing.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class WriteCacheFileInputStream extends FilterInputStream {
// ------------------------------ FIELDS ------------------------------

    private static final Logger logger = Logger.getLogger(WriteCacheFileInputStream.class.getCanonicalName());

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Creates a <code>FilterInputStream</code> by assigning the  argument <code>in</code> to the field
     * <code>this.in</code> so as to remember it for later use.
     *
     * @param in
     *         the underlying input stream, or <code>null</code> if this instance is to be created without an underlying
     *         stream.
     */
    protected WriteCacheFileInputStream(InputStream in) {
        super(in);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AutoCloseable ---------------------

    @Override
    public void close() throws IOException {
        logger.finer("closing stream");
        super.close();
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }
}
