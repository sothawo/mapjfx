/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
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
