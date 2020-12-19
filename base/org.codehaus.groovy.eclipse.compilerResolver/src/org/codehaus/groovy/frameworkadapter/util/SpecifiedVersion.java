/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.frameworkadapter.util;

import java.util.Objects;

import org.osgi.framework.Version;

public enum SpecifiedVersion {
    _16(1, 6, "16"),
    _17(1, 7, "17"),
    _18(1, 8, "18"),
    _19(1, 9, "19"),
    _20(2, 0, "20"),
    _21(2, 1, "21"),
    _22(2, 2, "22"),
    _23(2, 3, "23"),
    _24(2, 4, "24"),
    _25(2, 5, "25"),
    _26(2, 6, "26"),
    _30(3, 0, "30"),
    _40(4, 0, "40"),
    DONT_CARE(0, 0, "-1"),
    UNSPECIFIED(0, 0, "0");

    public final int majorVersion;
    public final int minorVersion;
    public final String versionName;

    SpecifiedVersion(int majorVersion, int minorVersion, String versionName) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.versionName = Objects.requireNonNull(versionName);
    }

    public String toVersionString() {
        return "[" + majorVersion + "." + minorVersion + "."  + 0 + "," + majorVersion + "." + minorVersion + "."  + 99 + ")";
    }

    public String toReadableVersionString() {
        switch (this) {
        case UNSPECIFIED:
            return "unspecified";
        case DONT_CARE:
            return "I don't care";
        default:
            return majorVersion + "." + minorVersion;
        }
    }

    /**
     * Generates a {@link SpecifiedVersion} from a name of a groovy jar.
     *
     * @return the {@link SpecifiedVersion} if known or {@link UNSPECIFIED}.
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
                    int major = Integer.parseInt(splits[0], 10);
                    int minor = Integer.parseInt(splits[1], 10);
                    switch (major) {
                    case 1:
                        switch (minor) {
                        case 6:
                            return _16;
                        case 7:
                            return _17;
                        case 8:
                            return _18;
                        case 9:
                            return _19;
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
                        case 3:
                            return _23;
                        case 4:
                            return _24;
                        case 5:
                            return _25;
                        case 6:
                            return _26;
                        }
                        break;
                    case 3:
                        switch (minor) {
                        case 0:
                            return _30;
                        }
                        break;
                    case 4:
                        switch (minor) {
                        case 0:
                            return _40;
                        }
                    }
                } catch (NumberFormatException ignore) {
                }
            }
        }
        return UNSPECIFIED;
    }

    public static SpecifiedVersion findVersionFromString(String compilerLevel) {
        if (compilerLevel == null) {
            return UNSPECIFIED;
        }
        switch (compilerLevel) {
        case "-1":
            return DONT_CARE;

        case "16":
        case "1.6":
            return _16;

        case "17":
        case "1.7":
            return _17;

        case "18":
        case "1.8":
            return _18;

        case "19":
        case "1.9":
            return _19;

        case "20":
        case "2.0":
            return _20;

        case "21":
        case "2.1":
            return _21;

        case "22":
        case "2.2":
            return _22;

        case "23":
        case "2.3":
            return _23;

        case "24":
        case "2.4":
            return _24;

        case "25":
        case "2.5":
            return _25;

        case "30":
        case "3.0":
            return _30;

        case "40":
        case "4.0":
            return _40;
        }

        System.out.println("Invalid Groovy compiler level: " + compilerLevel +
            "\nMust be one of 16, 1.6, 17, 1.7, 18, 1.8, 19, 1.9, 20, 2.0, 21, 2.1, 22, 2.2, 23, 2.3, 24, 2.4, 25, 2.5, 26, 2.6, 30, 3.0, 40 or 4.0");

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
            case 9:
                return _19;
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
            case 3:
                return _23;
            case 4:
                return _24;
            case 5:
                return _25;
            case 6:
                return _26;
            }
        case 3:
            switch (ver.getMinor()) {
            case 0:
                return _30;
            }
        case 4:
            switch (ver.getMinor()) {
            case 0:
                return _40;
            }
        }
        return UNSPECIFIED;
    }
}
