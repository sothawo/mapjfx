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

import javafx.beans.property.SimpleBooleanProperty;

/**
 * Common base class for elements that can exist on the map. The common functionality is the visibility on the map.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public abstract class MapElement {
// ------------------------------ FIELDS ------------------------------

    /** visible property */
    protected final SimpleBooleanProperty visible = new SimpleBooleanProperty(false);

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public String toString() {
        return "MapElement{" +
                "visible=" + visible +
                '}';
    }

// -------------------------- OTHER METHODS --------------------------

    public boolean getVisible() {
        return visible.get();
    }

    /**
     * sets the visibilty of the marker
     *
     * @param visible
     *         visibilty
     * @return this object
     */
    public MapElement setVisible(boolean visible) {
        this.visible.set(visible);
        return this;
    }

    public SimpleBooleanProperty visibleProperty() {
        return visible;
    }
}
