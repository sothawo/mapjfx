/*
 Copyright 2016-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class OfflineCacheTest {

    private static final Path cacheDirectory = FileSystems.getDefault().getPath("./target/cache");

    private final OfflineCache cache = OfflineCache.INSTANCE;

    @BeforeEach
    public void setUp() throws Exception {
        Files.createDirectories(cacheDirectory);
        cache.setCacheDirectory(cacheDirectory);
        cache.clearAllCacheFilters();
    }

    @AfterEach
    public void tearDown() throws Exception {
        OfflineCache.clearDirectory(cacheDirectory);
    }

    @Test
    public void cacheIsInactive() throws Exception {
        assertThat(cache.isNotActive()).isTrue();
    }

    @Test
    public void noCacheFiltersCannotBeSetWhenCacheFiltersAreSet() {
        cache.setCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));

        assertThatThrownBy(() -> {
            cache.setNoCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void cacheFiltersCannotBeSetWhenNoCacheFiltersAreSet() {
        cache.setNoCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));

        assertThatThrownBy(() -> {
            cache.setCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void cacheFilters() throws Exception {
        cache.setCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));
        assertThat(cache.urlShouldBeCached(new URL("http://www.sothawo.com/"))).isTrue();
        assertThat(cache.urlShouldBeCached(new URL("https://www.sothawo.com/"))).isTrue();
        assertThat(cache.urlShouldBeCached(new URL("http://www.github.com/"))).isFalse();
    }

    @Test
    public void noCacheFilters() throws Exception {
        cache.setNoCacheFilters(Arrays.asList("https?://www\\.sothawo\\.com.*"));
        assertThat(cache.urlShouldBeCached(new URL("http://www.sothawo.com/"))).isFalse();
        assertThat(cache.urlShouldBeCached(new URL("https://www.sothawo.com/"))).isFalse();
        assertThat(cache.urlShouldBeCached(new URL("http://www.github.com/"))).isTrue();
    }

    @Test
    public void filenameForUrl() throws Exception {
        URL url = new URL("https://avatars3.githubusercontent.com/u/8456476?v=3&s=460");
        final String encoded = URLEncoder.encode(url.toExternalForm(), "UTF-8");

        Path cacheDirectory = cache.getCacheDirectory();
        final Path filenamePath = cacheDirectory.resolve(encoded);

        assertThat(filenamePath).isEqualTo(cache.filenameForURL(url));
    }

    @Test
    public void clearCache() throws Exception {
        for (int i = 1; i < 3; i++) {
            Path p = cacheDirectory.resolve("file_" + i);
            try (PrintWriter w = new PrintWriter(p.toFile())) {
                w.println("42");
                w.flush();
            }
        }

        cache.clear();

        boolean directoryIsEmpty = false;
        try (DirectoryStream<Path> s = Files.newDirectoryStream(cacheDirectory)) {
            directoryIsEmpty = !s.iterator().hasNext();
        }

        assertThat(directoryIsEmpty).isTrue();
    }
}
