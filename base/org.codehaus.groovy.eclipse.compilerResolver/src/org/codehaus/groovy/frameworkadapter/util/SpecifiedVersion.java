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
    _16(1, 6, "16"), _17(1, 7, "17"), _18(1, 8, "18"), 
    _19(1, 9, "19"), _20(2, 0, "20"), _21(2,1,"21"),
    _22(2,2,"22"),_23(2,3,"23"),DONT_CARE(0, 0, "-1"), UNSPECIFIED(0, 0, "0");
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
    
    public String toReadableVersionString() {
        if (this == UNSPECIFIED) {
            return "unspecified";
        } else if (this == DONT_CARE) {
            return "I don't care";
        } else {
            return majorVersion + "." + minorVersion;
        }
    }
    
    
    /**
     * Generates a {@link SpecifiedVersion} from a name of a groovy jar
     *
     * @param jarName the name of a jar
     * @return the {@link SpecifiedVersion} if known. Will return
     *         {@link UNSPECIFIED} if not known
     */
    public static SpecifiedVersion parseVersion(String jarName) {
        boolean groovyStart = jarName.startsWith("groovy-");
        if (groovyStart && jarName.endsWith(".jar")) {
            boolean groovyAllStart = jarName.startsWith("groovy-all-");
            int verstionStart;
            if (groovyAllStart) {
                verstionStart = "groovy-all-".length();
            } else {
                verstionStart = "groovy-".length();
            }
            String[] splits = jarName.substring(verstionStart).split("\\.");
            if (splits.length > 1) {
                try {
                    int major = Integer.valueOf(splits[0]);
                    int minor = Integer.valueOf(splits[1]);
                    switch (major) {
                        case 1:
                            switch (minor) {
                                case 6:
                                    return _16;
                                case 7:
                                    return _17;
                                case 8:
                                    return _18;
                            }
                            break;
                        case 2:
                            switch (minor) {
                                case 0:
                                    return _20;
                                case 1:
                                    return _21;
                                case 2:
                                    return _22;
                            }
                            break;
    
                    }
                } catch (NumberFormatException e) {
                    // can ignore just return unspecified
                }
            }
        }
        return UNSPECIFIED;
    }

    public static SpecifiedVersion findVersionFromString(String compilerLevel) {
        if (compilerLevel == null) {
            return UNSPECIFIED;
        }
        
        if ("16".equals(compilerLevel) || "1.6".equals(compilerLevel)) {
            return _16;
        }
        if ("17".equals(compilerLevel) || "1.7".equals(compilerLevel)) {
            return _17;
        }
        if ("18".equals(compilerLevel) || "1.8".equals(compilerLevel)) {
            return _18;
        }
        if ("19".equals(compilerLevel) || "1.9".equals(compilerLevel)) {
            return _19;
        }
        if ("20".equals(compilerLevel) || "2.0".equals(compilerLevel)) {
            return _20;
        }
        if ("21".equals(compilerLevel) || "2.1".equals(compilerLevel)) {
            return _21;
        }
        if ("22".equals(compilerLevel) || "2.2".equals(compilerLevel)) {
            return _22;
        }
        if ("0".equals(compilerLevel)) {
            return UNSPECIFIED;
        }
        if ("-1".equals(compilerLevel)) {
            return DONT_CARE;
        }
        
        System.out.println("Invalid Groovy compiler level specified: " + compilerLevel + 
                        "\nMust be one of 16, 1.6, 17, 1.7, 18, 1.8, 19, 1.9, 20, 2.0, 21, 2.1, 22, or 2.2");
        return UNSPECIFIED;
    }

    public static SpecifiedVersion findVersion(Version ver) {
        switch (ver.getMajor()) {
            case 1:
                switch (ver.getMinor()) {
                    case 6:
                        return _16;
                    case 7:
                        return _17;
                    case 8:
                        return _18;
                }
                break;
            case 2:
                switch (ver.getMinor()) {
                    case 0:
                        return _20; 
                    case 1:
                        return _21; 
                    case 2:
                        return _22; 
                }                
        }
        return UNSPECIFIED;
    }
}