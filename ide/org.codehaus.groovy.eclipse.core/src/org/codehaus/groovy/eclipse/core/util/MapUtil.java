/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtil {
    public static <K, V> Map<K, V> newLinkedMap() {
        return linkedMap();
    }

    public static <K, V> Map<K, V> linkedMap(final Map.Entry<K, V>... objects) {
        final Map<K, V> list = new LinkedHashMap<K, V>();
        add(list, objects);
        return list;
    }
    public static <K, V> Map<K, V> newMap() {
        return new HashMap<K, V>();
    }
    public static <K, V> void add(final Map<K, V> map,
            final Map.Entry<K, V>... objects) {
        if (objects == null)
            return;
        for (final Map.Entry<K, V> object : objects)
            map.put(object.getKey(), object.getValue());
    }
}
