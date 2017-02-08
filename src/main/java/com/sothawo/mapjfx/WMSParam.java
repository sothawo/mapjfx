/*
 Copyright 2017 Peter-Josef Meisch (pj.meisch@sothawo.com)

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

import java.util.HashMap;
import java.util.Map;

/**
 * parameters for the WMS map type.
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
public class WMSParam {
    /** the parameters for the server. */
    private final Map<String, String> params = new HashMap<>();
    /** the url of the server. */
    private String url;

    public String getUrl() {
        return url;
    }

    public WMSParam setUrl(String url) {
        this.url = url;
        return this;
    }

    public WMSParam addParam(String key, String value) {
        if (null != key) {
            params.put(key, value);
        }
        return this;
    }

    public WMSParam clearParams() {
        params.clear();
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "WMSParam{" +
                "params=" + params +
                ", url='" + url + '\'' +
                '}';
    }
}
