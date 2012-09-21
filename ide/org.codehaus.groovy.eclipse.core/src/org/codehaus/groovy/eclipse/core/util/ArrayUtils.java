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
package org.codehaus.groovy.eclipse.core.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;

/**
 * Use this class so we don't have a dependency on apache commons-lang.
 * No code was copied from that jar
 *
 * @author andrew
 * @created Sep 20, 2012
 */
public class ArrayUtils {

    public static Object[] remove(Object[] arr, int i) {
        Assert.isNotNull(arr);
        Assert.isTrue(arr.length > i);


        List<Object> l = new ArrayList<Object>();
        for (int j = 0; j < arr.length; j++) {
            if (j != i) {
                l.add(arr[j]);
            }
        }
        Object[] newArr = (Object[]) Array.newInstance(arr.getClass().getComponentType(), arr.length - 1);
        return l.toArray(newArr);
    }

    public static Object[] add(Object[] arr, Object val) {
        return add(arr, arr.length, val);
    }
    public static Object[] add(Object[] arr, int index, Object val) {
        Assert.isNotNull(arr);
        Assert.isTrue(index >= 0 && index <= arr.length);

        Object[] newArr = (Object[]) Array.newInstance(arr.getClass().getComponentType(), arr.length + 1);
        System.arraycopy(arr, 0, newArr, 0, index);
        newArr[index] = val;
        if (arr.length > index) {
            System.arraycopy(arr, index, newArr, index + 1, arr.length - index);
        }
        return newArr;
    }

    public static Object[] removeElement(Object[] arr, Object toRemove) {
        Assert.isNotNull(arr);
        int index = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(toRemove)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            Object[] newArr = (Object[]) Array.newInstance(arr.getClass().getComponentType(), arr.length - 1);
            if (index > 0) {
                System.arraycopy(arr, 0, newArr, 0, index);
            }
            System.arraycopy(arr, index + 1, newArr, index, arr.length - index - 1);
            return newArr;
        } else {
            Object[] newArr = (Object[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
            System.arraycopy(arr, 0, newArr, 0, arr.length);
            return newArr;
        }
    }

}
