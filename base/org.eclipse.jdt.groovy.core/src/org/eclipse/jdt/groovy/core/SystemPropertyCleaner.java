/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.groovy.core;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Workaround for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=445122
 */
public class SystemPropertyCleaner {

    public static void clean() {
        Properties props = System.getProperties();
        Iterator<Entry<Object, Object>> iter = props.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Object, Object> e = iter.next();
            Object k = e.getKey();
            Object v = e.getValue();
            if (k instanceof String && v instanceof String) {
                //ok
            } else {
                //System.out.println("deleting "+k+" = ("+className(v)+") "+v);
                iter.remove();
            }
        }
    }
}
