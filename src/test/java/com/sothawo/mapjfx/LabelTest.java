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
import static org.junit.Assert.assertNull;

public class LabelTest {
// ------------------------------ FIELDS ------------------------------

    private static final String TEXT = "text";

// -------------------------- OTHER METHODS --------------------------

    @Test
    public void ctorArgsAreSet() throws Exception {
        Label label = new Label(TEXT, 47, 11);
        assertEquals(label.getText(), TEXT);
        assertEquals(47, label.getOffsetX());
        assertEquals(11, label.getOffsetY());
    }

    @Test(expected = IllegalArgumentException.class)
    public void ctorWithEmptyLabel() throws Exception {
        new Label("");
    }

    @Test(expected = NullPointerException.class)
    public void ctorWithNullLabel() throws Exception {
        new Label(null);
    }

    @Test
    public void noPositionInNewObject() throws Exception {
        assertNull(new Label(TEXT).getPosition());
    }

    @Test
    public void positionIsSet() throws Exception {
        Coordinate position = new Coordinate(48.3, 8.2);
        Label label = new Label(TEXT);
        label.setPosition(position);
        assertEquals(position, label.getPosition());
    }
}
