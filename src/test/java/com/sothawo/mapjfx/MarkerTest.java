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
package com.sothawo.mapjfx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.*;

public class MarkerTest {

    private static final String TEST_IMG = "/markers/blue_map_marker.png";
    URL imageURL;

    @BeforeEach
    public void setUp() throws Exception {
        imageURL = getClass().getResource(TEST_IMG);
        assertThat(imageURL).isNotNull();
    }

    @Test
    public void ctorWithNullURL() throws Exception {
        assertThatThrownBy(() -> {
            new Marker(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void ctorArgsAreSet() throws Exception {
        Marker marker = new Marker(imageURL, 47, 11);
        assertThat(imageURL).isEqualTo(marker.getImageURL());
        assertThat(marker.getOffsetX()).isEqualTo(47);
        assertThat(marker.getOffsetY()).isEqualTo(11);
    }

    @Test
    public void noPositionInNewObject() throws Exception {
        assertThat(new Marker(imageURL).getPosition()).isNull();
    }

    @Test
    public void positionIsSet() throws Exception {
        Coordinate position = new Coordinate(48.3, 8.2);
        Marker marker = new Marker(imageURL);
        marker.setPosition(position);
        assertThat(marker.getPosition()).isEqualTo(position);
    }
}
