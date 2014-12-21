/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import java.util.regex.Pattern;

/**
 *
 * @author h_ayabe
 * @created 2014/12/21
 */
public class FormatterOffUtils {

    private static String formatterTokenMatchStr(boolean isOn) {
        String switchStr = (isOn) ? "[Oo]n" : "[Oo]ff";
        return "//[ \t]*@formatter[ \t]*:[ \t]*" + switchStr + "(\\s.*)*";
    }

    public static Pattern ON_PATTERN = Pattern.compile(formatterTokenMatchStr(true));

    public static Pattern OFF_PATTERN = Pattern.compile(formatterTokenMatchStr(false));

    public static boolean matchFormatterOff(String tokenStr) {
        return OFF_PATTERN.matcher(tokenStr).find();
    }

    public static boolean matchFormatterOn(String tokenStr) {
        return ON_PATTERN.matcher(tokenStr).find();
    }
}
