/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.tests

import static org.junit.Assert.*

import org.codehaus.groovy.eclipse.dsl.pointcuts.StringObjectVector
import org.junit.Test

final class StringObjectVectorTests {

    private final Object obj1 = new Object()
    private final Object obj2 = new Object()
    private final Object obj3 = new Object()
    private final Object obj4 = new Object()
    private final Object obj5 = new Object()

    private final String str1 = '1'
    private final String str2 = '2'
    private final String str3 = '3'
    private final String str4 = '4'
    private final String str5 = '5'

    @Test
    void testCreateAndResiveVector() {
        StringObjectVector vector = new StringObjectVector(3)
        assertEquals(0, vector.size)
        assertEquals(3, vector.maxSize)
        vector.add(str1, obj1)
        assertEquals(1, vector.size)
        assertEquals(3, vector.maxSize)
        vector.add(str2, obj2)
        assertEquals(2, vector.size)
        assertEquals(3, vector.maxSize)
        vector.add(str3, obj3)
        assertEquals(3, vector.size)
        assertEquals(3, vector.maxSize)
        vector.add(str4, obj4)
        assertEquals(4, vector.size)
        assertEquals(6, vector.maxSize)
        vector.add(str5, obj5)
        assertEquals(5, vector.size)
        assertEquals(6, vector.maxSize)
    }

    @Test
    void testFindAndContains() {
        StringObjectVector vector = new StringObjectVector(3)
        vector.add(str1, obj1)
        vector.add(str2, obj2)
        vector.add(str3, obj3)
        vector.add(str4, obj4)
        vector.add(str5, obj5)
        assertEquals('should have found obj1', obj1, vector.find(str1))
        assertEquals('should have found obj2', obj2, vector.find(str2))
        assertEquals('should have found obj3', obj3, vector.find(str3))
        assertEquals('should have found obj4', obj4, vector.find(str4))
        assertEquals('should have found obj5', obj5, vector.find(str5))

        assertFalse(vector.contains(null))
        assertFalse(vector.contains(new Object()))
        assertFalse(vector.containsName(''))
        assertFalse(vector.contains(str1))


        assertTrue(vector.contains(obj1))
        assertTrue(vector.contains(obj2))
        assertTrue(vector.contains(obj3))
        assertTrue(vector.contains(obj4))
        assertTrue(vector.contains(obj5))

        assertTrue(vector.containsName(str1))
        assertTrue(vector.containsName(str2))
        assertTrue(vector.containsName(str3))
        assertTrue(vector.containsName(str4))
        assertTrue(vector.containsName(str5))

        assertNull(vector.find(''))
        assertNull(vector.find(null))

        assertEquals(5, vector.size)
        vector.add(null, obj1)
        assertEquals(6, vector.size)
        assertEquals(obj1, vector.find(null))
    }

    @Test
    void testElementAtNameAt() {
        StringObjectVector vector = new StringObjectVector(3)

        try {
            vector.elementAt(0)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            vector.nameAt(0)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }

        vector.add(str1, obj1)
        vector.add(str2, obj2)
        vector.add(str3, obj3)
        vector.add(str4, obj4)
        vector.add(str5, obj5)
        assertEquals('should have found obj1', obj1, vector.elementAt(0))
        assertEquals('should have found obj2', obj2, vector.elementAt(1))
        assertEquals('should have found obj2', obj3, vector.elementAt(2))
        assertEquals('should have found obj2', obj4, vector.elementAt(3))
        assertEquals('should have found obj2', obj5, vector.elementAt(4))
        assertEquals('should have found str1', str1, vector.nameAt(0))
        assertEquals('should have found str2', str2, vector.nameAt(1))
        assertEquals('should have found str2', str3, vector.nameAt(2))
        assertEquals('should have found str2', str4, vector.nameAt(3))
        assertEquals('should have found str2', str5, vector.nameAt(4))
        try {
            vector.elementAt(5)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            vector.nameAt(5)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            vector.elementAt(-1)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        try {
            vector.nameAt(-1)
            fail('Expecting exception')
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        vector.add(null, null)
        vector.add(null, null)
        assertEquals('should have found str2', null, vector.nameAt(5))
        assertEquals('should have found str2', null, vector.nameAt(6))
        assertEquals('should have found str2', null, vector.elementAt(5))
        assertEquals('should have found str2', null, vector.elementAt(6))
    }

    @Test
    void testGetNameAndValue() {
        StringObjectVector vector = new StringObjectVector(3)
        vector.add(str1, obj1)
        vector.add(str2, obj2)
        vector.add(str3, obj3)
        vector.add(str4, obj4)
        vector.add(str5, obj5)
        Object[] elts = vector.getElements()
        assertEquals(5, elts.length)
        assertEquals(obj1, elts[0])
        assertEquals(obj2, elts[1])
        assertEquals(obj3, elts[2])
        assertEquals(obj4, elts[3])
        assertEquals(obj5, elts[4])

        String[] names = vector.getNames()
        assertEquals(5, names.length)
        assertEquals(str1, names[0])
        assertEquals(str2, names[1])
        assertEquals(str3, names[2])
        assertEquals(str4, names[3])
        assertEquals(str5, names[4])

        vector.add(null, null)
        vector.add(null, null)

        elts = vector.getElements()
        assertEquals(7, elts.length)
        assertEquals(obj1, elts[0])
        assertEquals(obj2, elts[1])
        assertEquals(obj3, elts[2])
        assertEquals(obj4, elts[3])
        assertEquals(obj5, elts[4])
        assertEquals(null, elts[5])
        assertEquals(null, elts[6])

        names = vector.getNames()
        assertEquals(7, names.length)
        assertEquals(str1, names[0])
        assertEquals(str2, names[1])
        assertEquals(str3, names[2])
        assertEquals(str4, names[3])
        assertEquals(str5, names[4])
        assertEquals(null, names[5])
        assertEquals(null, names[6])
    }
}
