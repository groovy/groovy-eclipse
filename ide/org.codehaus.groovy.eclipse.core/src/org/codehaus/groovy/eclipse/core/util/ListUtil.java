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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Andrew Eisenberg
 * @created Jul 20, 2009
 *
 */
public class ListUtil {
    public static <T> List<T> array(final T... objects) {
        final List<T> list = new ArrayList<T>();
        add(list, objects);
        return list;
    }

    public static <T> List<T> array(final Collection<T> collection) {
        final List<T> list = new ArrayList<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    public static <T> List<T> linked(final T... objects) {
        final List<T> list = new LinkedList<T>();
        add(list, objects);
        return list;
    }

    public static <T> List<T> linked(final Collection<T> collection) {
        final List<T> list = new LinkedList<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> newEmptyList() {
        return newList();
    }

    public static <T> List<T> newList(final T... objects) {
        return array(objects);
    }

    public static <T> List<T> newList(final Collection<T> objects) {
        return array(objects);
    }

    public static <T> List<T> list(final T... objects) {
        return array(objects);
    }

    public static <T> List<T> list(final Collection<T> collection) {
        final List<T> list = new ArrayList<T>();
        if (collection != null && collection.size() > 0)
            list.addAll(collection);
        return list;
    }

    public static <T> List<T> add(final List<T> list, final T... objects) {
        if (objects == null)
            return list;
        for (final T object : objects)
            list.add(object);
        return list;
    }


}
