package com.sothawo.mapjfx.offline;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class OfflineCacheTest {

    private OfflineCache cache;

    @Before
    public void setUp() throws Exception {
        cache = new OfflineCache();
        cache.setCacheDirectory(FileSystems.getDefault().getPath("tmpdata/cache"));
    }

    @Test
    public void filenameForUrl() throws Exception {
        URL url = new URL("https://avatars3.githubusercontent.com/u/8456476?v=3&s=460");
        final String encoded = URLEncoder.encode(url.toExternalForm(), "UTF-8");

        Path cacheDirectory = cache.getCacheDirectory();
        final Path filenamePath = cacheDirectory.resolve(encoded);

        assertThat(filenamePath, is(equalTo(cache.filenameForURL(url))));
    }
}
