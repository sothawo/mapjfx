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


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class MapLabelTest {

    private static final String TEXT = "text";

    @Test
    public void ctorArgsAreSet() throws Exception {
        MapLabel mapLabel = new MapLabel(TEXT, 47, 11);
        assertThat(TEXT).isEqualTo(mapLabel.getText());
        assertThat(mapLabel.getOffsetX()).isEqualTo(47);
        assertThat(mapLabel.getOffsetY()).isEqualTo(11);
    }

    @Test
    public void ctorWithEmptyLabel() throws Exception {
        assertThatThrownBy(() -> {
            new MapLabel("");
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void ctorWithNullLabel() throws Exception {
        assertThatThrownBy(() -> {
            new MapLabel(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void noPositionInNewObject() throws Exception {
        assertThat(new MapLabel(TEXT).getPosition()).isNull();
    }

    @Test
    public void positionIsSet() throws Exception {
        Coordinate position = new Coordinate(48.3, 8.2);
        MapLabel mapLabel = new MapLabel(TEXT);
        mapLabel.setPosition(position);
        assertThat(mapLabel.getPosition()).isEqualTo(position);
    }
}
