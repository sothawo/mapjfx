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

import org.junit.Before;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.*;

public class MarkerTest {

    private static final String TEST_IMG = "/markers/blue_map_marker.png";
    URL imageURL;

    @Before
    public void setUp() throws Exception {
        imageURL = getClass().getResource(TEST_IMG);
        assertNotNull("Testimage not found: " + TEST_IMG, imageURL);
    }

    @Test(expected = NullPointerException.class)
    public void ctorWithNullURL() throws Exception {
        new Marker(null);
    }

    @Test
    public void ctorArgsAreSet() throws Exception {
        Marker marker = new Marker(imageURL, 47, 11);
        assertEquals(marker.getImageURL(), imageURL);
        assertEquals(47, marker.getOffsetX());
        assertEquals(11, marker.getOffsetY());
    }

    @Test
    public void noPositionInNewObject() throws Exception {
        assertNull(new Marker(imageURL).getPosition());
    }

    @Test
    public void positionIsSet() throws Exception {
        Coordinate position = new Coordinate(48.3, 8.2);
        Marker marker = new Marker(imageURL);
        marker.setPosition(position);
        assertEquals(position, marker.getPosition());
    }
}
