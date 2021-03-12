/*
 Copyright 2018-2021 Peter-Josef Meisch (pj.meisch@sothawo.com)

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
package com.sothawo.mapjfx.browser;

/**
 * Helper class needed when launching the app from IntelliJ.
 * see https://stackoverflow.com/questions/52653836/maven-shade-javafx-runtime-components-are-missing
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com)
 */
public class BrowserLauncher {
    public static void main(String[] args) {
        Browser.main(args);
    }
}
