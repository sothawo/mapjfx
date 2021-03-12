/*
 Copyright 2019-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
package com.sothawo.mapjfx;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.skyscreamer.jsonassert.JSONAssert.*;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
public class ConfigurationTest {

    @Test
    public void shouldHaveDefaultValues() {
        Configuration configuration = Configuration.builder().build();

        assertThat(configuration.getProjection()).isEqualTo(Projection.WEB_MERCATOR);
        assertThat(configuration.getInteractive()).isTrue();
        assertThat(configuration.showZoomControls()).isTrue();
    }

    @Test
    public void shouldDisableZoomControlsWhenNotInteractive() {
        Configuration configuration = Configuration.builder()
            .showZoomControls(true)
            .interactive(false)
            .build();

        assertThat(configuration.showZoomControls()).isFalse();
    }

    @Test
    public void shouldProduceJson() throws JSONException {

        String expected = "{\"projection\":\"EPSG:4326\", \"interactive\": false, \"showZoomControls\": false}";

        String json = Configuration.builder()
            .projection(Projection.WGS_84)
            .interactive(false)
            .showZoomControls(false)
            .build().toJson();

        assertEquals(expected, json, false);
    }
}
