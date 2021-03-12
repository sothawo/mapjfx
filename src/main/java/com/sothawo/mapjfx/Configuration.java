/*
 Copyright 2019-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

/**
 * Configuration class. Instances must be created by using the builder, obtained with {@link Configuration#builder()}.
 */
public class Configuration {

    private final Projection projection;

    private final boolean interactive;
    private final boolean showZoomControls;

    private Configuration(final Projection projection, final boolean interactive, final boolean showZoomControls) {
        this.projection = projection;
        this.interactive = interactive;
        this.showZoomControls = showZoomControls && interactive;
    }

    /**
     * @return a new Configuration builder.
     */
    public static ConfigurationBuilder builder() {
        return new ConfigurationBuilder();
    }

    @Override
    public String toString() {
        return "Configuration{" +
            "projection=" + projection +
            ", interactive=" + interactive +
            ", showZoomControls=" + showZoomControls +
            '}';
    }

    /**
     * @return the {@link Projection} to use for the Map.
     */
    public Projection getProjection() {
        return projection;
    }

    /**
     * if set to false, the user cannot change the zoom by controls in the map, scrolling the wheel or doubleclicking and cannot pan around by dragging the mouse
     */
    public boolean getInteractive() {
        return interactive;
    }

    /**
     * @return a JSON representation of this configuration.
     */
    public String toJson() {
        return '{' +
            "\"projection\":" +
            '"' + projection.getOlName() + "\"," +
            "\"interactive\":" + getInteractive() + "," +
            "\"showZoomControls\":" + showZoomControls() +
            '}';
    }

    /**
     * @return false if the zoom controls in the map should not be shown.
     */
    public boolean showZoomControls() {
        return showZoomControls;
    }

    public static final class ConfigurationBuilder {
        private Projection projection = Projection.WEB_MERCATOR;
        private boolean interactive = true;
        private boolean showZoomControls = true;

        private ConfigurationBuilder() {
        }


        public ConfigurationBuilder projection(final Projection projection) {
            this.projection = projection;
            return this;
        }

        public ConfigurationBuilder interactive(boolean interactive) {
            this.interactive = interactive;
            return this;
        }

        public ConfigurationBuilder showZoomControls(final boolean showZoomControls) {
            this.showZoomControls = showZoomControls;
            return this;
        }

        public Configuration build() {
            return new Configuration(projection, interactive, showZoomControls);
        }
    }
}
