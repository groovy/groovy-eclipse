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

import org.osgi.framework.Version;

public enum SpecifiedVersion {
    _16(1, 6, "16"), _17(1, 7, "17"), _18(1, 8, "18"), _19(1, 9, "19"), _20(2, 0, "20"), UNSPECIFIED(-1, -1, "0");
    public final int majorVersion;
    public final int minorVersion;
    public final String versionName;

    SpecifiedVersion(int majorVersion, int minorVersion, String versionName) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.versionName = versionName;
    }
    
    public String toVersionString() {
        return "[" + majorVersion + "." + minorVersion + "."  + 0 + "," + majorVersion + "." + minorVersion + "."  + 99 + ")";
    }
    
    public static SpecifiedVersion findVersion(Version ver) {
        if (ver.getMajor() == 2) {
            if (ver.getMinor() == 0) {
                return _20;
            }
        }
        if (ver.getMajor() == 1) {
            if (ver.getMinor() == 6) {
                return _16;
            }
            if (ver.getMinor() == 7) {
                return _17;
            }
            if (ver.getMinor() == 8) {
                return _18;
            }
            if (ver.getMinor() == 9) {
                return _19;
            }
        }
        return UNSPECIFIED;
    }
}