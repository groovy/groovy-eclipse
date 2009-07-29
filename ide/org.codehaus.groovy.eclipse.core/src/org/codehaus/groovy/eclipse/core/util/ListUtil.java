/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

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
