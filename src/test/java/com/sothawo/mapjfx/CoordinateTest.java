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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.data.Offset.offset;

public class CoordinateTest {

    @Test
    public void equals() throws Exception {
        final Coordinate coordinate1 = new Coordinate(12.345, 67.89);
        final Coordinate coordinate2 = new Coordinate(12.345, 67.89);
        assertThat(coordinate2).isEqualTo(coordinate1);
    }

    @Test
    public void getLatitude() throws Exception {
        final Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertThat(coordinate.getLatitude()).isEqualTo((Double) 12.345);
    }

    @Test
    public void getLongitude() throws Exception {
        final Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertThat(coordinate.getLongitude()).isEqualTo((Double) 67.89);
    }

    @Test
    public void nullLatitude() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            new Coordinate(null, 12.345);
        });
    }

    @Test
    public void nullLongitude() throws Exception {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> {
            new Coordinate(12.345, null);
        });
    }

    @Test
    public void normalize() {
        assertThat(177.5).isCloseTo(new Coordinate(10.0, 177.5).normalize().getLongitude(), offset(0.01));
        assertThat(-137.5).isCloseTo(new Coordinate(10.0, 222.5).normalize().getLongitude(), offset(0.01));
        assertThat(177.5).isCloseTo(new Coordinate(10.0, 537.5).normalize().getLongitude(), offset(0.01));
        assertThat(-137.5).isCloseTo(new Coordinate(10.0, 582.5).normalize().getLongitude(), offset(0.01));
        assertThat(177.5).isCloseTo(new Coordinate(10.0, 897.5).normalize().getLongitude(), offset(0.01));

        assertThat(176.5).isCloseTo(new Coordinate(10.0, -183.5).normalize().getLongitude(), offset(0.01));
        assertThat(137.5).isCloseTo(new Coordinate(10.0, -222.5).normalize().getLongitude(), offset(0.01));
    }
}
