/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.frameworkadapter.util;


public enum SpecifiedVersion { 
    _16(6, "16"), _17(7, "17"), _18(8, "18"), _19(9, "19"), _20(0, "20"), UNSPECIFIED(-1, "0"); 
    public final int minorVersion;
    public final String versionName;
    SpecifiedVersion(int minorVersion, String versionName) {
        this.minorVersion = minorVersion;
        this.versionName = versionName;
    }
    
    
}