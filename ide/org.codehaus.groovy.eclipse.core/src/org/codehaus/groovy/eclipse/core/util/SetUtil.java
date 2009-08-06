 /*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class SetUtil {
    public static <T> Set<T> hashSet(final T... objects) {
        final Set<T> list = new HashSet<T>();
        add(list, objects);
        return list;
    }

    public static <T> Set<T> hashSet(final Collection<T> collection) {
        final Set<T> list = new HashSet<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    public static <T> Set<T> linkedSet(final T... objects) {
        final Set<T> list = new LinkedHashSet<T>();
        add(list, objects);
        return list;
    }

    public static <T> Set<T> linkedSet(final Collection<T> collection) {
        final Set<T> list = new LinkedHashSet<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    public static <T> Set<T> set(final Collection<T> collection) {
        final Set<T> list = new HashSet<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    public static <T> Set<T> set(final T... objects) {
        return hashSet(objects);
    }

    public static <T> Set<T> add(final Set<T> set, final T... objects) {
        if (objects == null)
            return set;
        for (final T object : objects)
            set.add(object);
        return set;
    }

    public static <T> Set<T> setAdd(final Set<T> set, final T... objects) {
        return add(set, objects);
    }

}
