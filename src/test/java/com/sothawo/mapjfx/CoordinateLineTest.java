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

import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Tests for CoordinateLine.
 *
 * @author P.J.Meisch (pj.meisch@jaroso.de)
 */
public class CoordinateLineTest implements TestBase {
// -------------------------- OTHER METHODS --------------------------

    @Test
    public void colorCanBeSet() throws Exception {
        CoordinateLine coordinateLine = new CoordinateLine();
        coordinateLine.setColor(Color.BISQUE);
        assertEquals(Color.BISQUE, coordinateLine.getColor());
    }

    @Test
    public void defaultColorIsSet() throws Exception {
        assertEquals(CoordinateLine.DEFAULT_COLOR, new CoordinateLine().getColor());
    }

    @Test
    public void defaultVisibilityIsFalse() throws Exception {
        assertFalse(new CoordinateLine().getVisible());
    }

    @Test
    public void defaultWidthIsSet() throws Exception {
        assertEquals(CoordinateLine.DEFAULT_WIDTH, new CoordinateLine().getWidth());
    }

    @Test
    public void noCoordinatesInCtorYieldsEmptyStream() throws Exception {
        CoordinateLine coordinateLine = new CoordinateLine();
        assertEquals(0, coordinateLine.getCoordinateStream().count());
    }

    @Test
    public void passedCoordinatesAreInStream() throws Exception {
        CoordinateLine coordinateLine = new CoordinateLine(coordKarlsruheHarbour, coordKarlsruheStation);
        Set<Coordinate> coordinates = coordinateLine.getCoordinateStream().collect(Collectors.toSet());
        assertTrue(coordinates.contains(coordKarlsruheHarbour));
        assertTrue(coordinates.contains(coordKarlsruheStation));
        assertEquals(2, coordinates.size());
    }

    @Test
    public void setVisibility() throws Exception {
        CoordinateLine coordinateLine = new CoordinateLine();
        coordinateLine.setVisible(true);
        assertTrue(coordinateLine.getVisible());
    }

    @Test
    public void widthCanBeSet() throws Exception {
        CoordinateLine coordinateLine = new CoordinateLine();
        coordinateLine.setWidth(5);
        assertEquals(5, coordinateLine.getWidth());
    }
}
