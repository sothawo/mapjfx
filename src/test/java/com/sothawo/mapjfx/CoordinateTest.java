/*
 Copyright 2015-2019 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CoordinateTest {

    @Test
    public void equals() throws Exception {
        final Coordinate coordinate1 = new Coordinate(12.345, 67.89);
        final Coordinate coordinate2 = new Coordinate(12.345, 67.89);
        assertEquals(coordinate1, coordinate2);
    }

    @Test
    public void getLatitude() throws Exception {
        final Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertEquals((Double) 12.345, coordinate.getLatitude());
    }

    @Test
    public void getLongitude() throws Exception {
        final Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertEquals((Double) 67.89, coordinate.getLongitude());
    }

    @Test(expected = NullPointerException.class)
    public void nullLatitude() throws Exception {
        new Coordinate(null, 12.345);
    }

    @Test(expected = NullPointerException.class)
    public void nullLongitude() throws Exception {
        new Coordinate(12.345, null);
    }

    @Test
    public void normalize() {
        assertEquals(new Coordinate(10.0, 177.5).normalize().getLongitude(), 177.5,0.01);
        assertEquals(new Coordinate(10.0, 222.5).normalize().getLongitude(), -137.5,0.01);
        assertEquals(new Coordinate(10.0, 537.5).normalize().getLongitude(), 177.5,0.01);
        assertEquals(new Coordinate(10.0, 582.5).normalize().getLongitude(), -137.5,0.01);
        assertEquals(new Coordinate(10.0, 897.5).normalize().getLongitude(), 177.5,0.01);

        assertEquals(new Coordinate(10.0, -183.5).normalize().getLongitude(), 176.5,0.01);
        assertEquals(new Coordinate(10.0, -222.5).normalize().getLongitude(), 137.5,0.01);
    }
}
