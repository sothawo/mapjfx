/*
 Copyright 2018-2021 Erik Jähne

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
package com.sothawo.mapjfx;
 */
package com.sothawo.mapjfx;

import java.util.StringJoiner;

/**
 * parameters for the XYZ map source.
 * See https://openlayers.org/en/latest/apidoc/module-ol_source_XYZ.html
 * @author Erik Jähne
 */
public class XYZParam {
    /** Attributions */
    private String attributions;
    /** Cache size. */
    private Integer cacheSize;
    /**
     * The crossOrigin attribute for loaded images.
     * Note that you must provide a crossOrigin value if you are using the WebGL renderer or if you want to access pixel data with the Canvas renderer.
     * See https://developer.mozilla.org/en-US/docs/Web/HTML/CORS_enabled_image for more detail.
     */
    private String crossOrigin;
    /** Whether the layer is opaque. */
    private Boolean opaque;
    /** Maximum allowed reprojection error (in pixels). Higher values can increase reprojection performance, but decrease precision. */
    private Double reprojectionErrorThreshold;
    /** Optional max zoom level. */
    private Integer maxZoom;
    /** Optional min zoom level. */
    private Integer minZoom;
    /**
     * The pixel ratio used by the tile service. For example, if the tile service advertizes 256px by 256px tiles but actually sends 512px by 512px images (for retina/hidpi devices) then tilePixelRatio should be with to 2.
     */
    private Double tilePixelRatio;
    /** The tile size used by the tile service. */
    private Integer tileSize;
    /**
     * URL template. Must include {x}, {y} or {-y}, and {z} placeholders.
     * A {?-?} template pattern, for example subdomain{a-f}.domain.com, may be used instead of defining each one separately in the urls option.
     */
    private String url;
    /** Whether to wrap the world horizontally. */
    private Boolean wrapX;
    /** Duration of the opacity transition for rendering. To disable the opacity transition, pass transition: 0. */
    private Integer transition;

    public XYZParam withAttributions(String attributions) {
        setAttributions(attributions);
        return this;
    }

    public XYZParam withCacheSize(Integer cacheSize) {
        setCacheSize(cacheSize);
        return this;
    }

    public XYZParam withCrossOrigin(String crossOrigin) {
        setCrossOrigin(crossOrigin);
        return this;
    }

    public XYZParam withOpaque(Boolean opaque) {
        setOpaque(opaque);
        return this;
    }

    public XYZParam withReprojectionErrorThreshold(Double reprojectionErrorThreshold) {
        setReprojectionErrorThreshold(reprojectionErrorThreshold);
        return this;
    }

    public XYZParam withMaxZoom(Integer maxZoom) {
        setMaxZoom(maxZoom);
        return this;
    }

    public XYZParam withMinZoom(Integer minZoom) {
        setMinZoom(minZoom);
        return this;
    }

    public XYZParam withTilePixelRatio(Double tilePixelRatio) {
        setTilePixelRatio(tilePixelRatio);
        return this;
    }

    public XYZParam withTileSize(Integer tileSize) {
        setTileSize(tileSize);
        return this;
    }

    public XYZParam withUrl(String url) {
        setUrl(url);
        return this;
    }

    public XYZParam withWrapX(Boolean wrapX) {
        setWrapX(wrapX);
        return this;
    }

    public XYZParam withTransition(Integer transition) {
        setTransition(transition);
        return this;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = escape(attributions);
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Integer cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String getCrossOrigin() {
        return crossOrigin;
    }

    public void setCrossOrigin(String crossOrigin) {
        this.crossOrigin = escape(crossOrigin);
    }

    public Boolean getOpaque() {
        return opaque;
    }

    public void setOpaque(Boolean opaque) {
        this.opaque = opaque;
    }

    public Double getReprojectionErrorThreshold() {
        return reprojectionErrorThreshold;
    }

    public void setReprojectionErrorThreshold(Double reprojectionErrorThreshold) {
        this.reprojectionErrorThreshold = reprojectionErrorThreshold;
    }

    public Integer getMaxZoom() {
        return maxZoom;
    }

    public void setMaxZoom(Integer maxZoom) {
        this.maxZoom = maxZoom;
    }

    public Integer getMinZoom() {
        return minZoom;
    }

    public void setMinZoom(Integer minZoom) {
        this.minZoom = minZoom;
    }

    public Double getTilePixelRatio() {
        return tilePixelRatio;
    }

    public void setTilePixelRatio(Double tilePixelRatio) {
        this.tilePixelRatio = tilePixelRatio;
    }

    public Integer getTileSize() {
        return tileSize;
    }

    public void setTileSize(Integer tileSize) {
        this.tileSize = tileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = escape(url);
    }

    public Boolean getWrapX() {
        return wrapX;
    }

    public void setWrapX(Boolean wrapX) {
        this.wrapX = wrapX;
    }

    public Integer getTransition() {
        return transition;
    }

    public void setTransition(Integer transition) {
        this.transition = transition;
    }

    public String toJSON() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        if (attributions != null)   joiner.add("\"attributions\":\"" +  attributions + "\"");
        if (cacheSize != null)      joiner.add("\"cacheSize\":" +       cacheSize);
        if (crossOrigin != null)    joiner.add("\"crossOrigin\":\"" +   crossOrigin + "\"");
        if (opaque != null)         joiner.add("\"opaque\":" +          opaque);
        if (maxZoom != null)        joiner.add("\"maxZoom\":" +         maxZoom);
        if (minZoom != null)        joiner.add("\"minZoom\":" +         minZoom);
        if (tilePixelRatio != null) joiner.add("\"tilePixelRatio\":" +  tilePixelRatio);
        if (tileSize != null)       joiner.add("\"tileSize\":" +        tileSize);
        if (url != null)            joiner.add("\"url\":\"" +           url + "\"");
        if (wrapX != null)          joiner.add("\"wrapX\":" +           wrapX);
        if (transition != null)     joiner.add("\"transition\":" +      transition);
        if (reprojectionErrorThreshold != null)
            joiner.add("\"reprojectionErrorThreshold\":" + reprojectionErrorThreshold);
        return joiner.toString();
    }

    private String escape(String other){
        return other.replace("\"","\\\"");
    }
}
