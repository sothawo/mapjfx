/*
 Copyright 2016 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/package com.sothawo.mapjfx.event;

import com.sothawo.mapjfx.MapLabel;
import javafx.event.Event;
import javafx.event.EventType;

import static java.util.Objects.requireNonNull;

/**
 * Event class for events concerning markers.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class MapLabelEvent extends Event {

    /** base event type */
    public static final EventType<MapLabelEvent> ANY = new EventType<>("MAPLABEL_EVENT_ANY");

    /** label clicked in map */
    public static final EventType<MapLabelEvent> MAPLABEL_CLICKED = new EventType<>(ANY, "MAPLABEL_CLICKED");

    /** label double clicked in map */
    public static final EventType<MapLabelEvent> MAPLABEL_DOUBLECLICKED = new EventType<>(ANY,
            "MAPLABEL_DOUBLECLICKED");

    /** the MapLabel for this event. */
    private final MapLabel mapLabel;

    public MapLabelEvent(EventType<? extends MapLabelEvent> eventType, MapLabel mapLabel) {
        super(eventType);
        this.mapLabel = requireNonNull(mapLabel);
    }

    public MapLabel getMapLabel() {
        return mapLabel;
    }

}
