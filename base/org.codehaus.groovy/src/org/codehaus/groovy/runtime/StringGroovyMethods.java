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
package org.codehaus.groovy.runtime;

import java.util.List;

import groovy.lang.IntRange;

//GRECLIPSE
/**
* In Groovy 1.7, StringGroovyMethods does not exist.  Add required methods here and
* point them to {@link DefaultGroovyMethods}
 * @author Andrew Eisenberg
 * @created Jun 18, 2013
 */
public class StringGroovyMethods {

    public static String getAt(String line, IntRange range) {
        return DefaultGroovyMethods.getAt(line, range);
    }

    public static List<String> tokenize(String text) {
        return DefaultGroovyMethods.tokenize(text);
    }

}
