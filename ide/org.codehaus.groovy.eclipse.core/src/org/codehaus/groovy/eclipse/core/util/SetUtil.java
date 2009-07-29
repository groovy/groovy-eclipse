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
