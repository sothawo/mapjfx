/*
 Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import static org.junit.Assert.assertEquals;

public class CoordinateTest {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void equals() throws Exception {
        Coordinate coordinate1 = new Coordinate(12.345, 67.89);
        Coordinate coordinate2 = new Coordinate(12.345, 67.89);
        assertEquals(coordinate1, coordinate2);
    }

    @Test
    public void getLatitude() throws Exception {
        Coordinate coordinate = new Coordinate(12.345, 67.89);
        assertEquals((Double) 12.345, coordinate.getLatitude());
    }

    @Test
    public void getLongitude() throws Exception {
        Coordinate coordinate = new Coordinate(12.345, 67.89);
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

}
