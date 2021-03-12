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
module com.sothawo.mapjfx {
    requires java.desktop;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.web;
    requires jdk.jsobject;
    requires org.slf4j;

    exports com.sothawo.mapjfx;
    exports com.sothawo.mapjfx.event;
    exports com.sothawo.mapjfx.offline;
    exports com.sothawo.mapjfx.app to javafx.graphics; // for the test application
}
