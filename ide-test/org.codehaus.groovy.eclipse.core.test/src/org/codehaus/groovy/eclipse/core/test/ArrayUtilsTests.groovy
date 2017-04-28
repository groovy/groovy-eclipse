/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.core.test

import org.codehaus.groovy.eclipse.core.util.ArrayUtils
import org.junit.Test

final class ArrayUtilsTests {

    @Test
    void testArrayRemove1() {
        String[] res = ArrayUtils.remove([''] as String[], 0)
        assert res.length == 0
    }

    @Test
    void testArrayRemove2() {
        String[] res = ArrayUtils.remove(['a', 'b'] as String[], 0)
        assert res.length == 1
        assert res[0] == 'b'
    }

    @Test
    void testArrayRemove3() {
        String[] res = ArrayUtils.remove(['a', 'b'] as String[], 1)
        assert res.length == 1
        assert res[0] == 'a'
    }

    @Test
    void testArrayRemove4() {
        String[] res = ArrayUtils.remove(['a', 'b', 'c'] as String[], 1)
        assert res.length == 2
        assert res[0] == 'a'
        assert res[1] == 'c'
    }

    @Test
    void testArrayRemoveElement1() {
        String[] res = ArrayUtils.removeElement([] as String[], '')
        assert res.length == 0
    }

    @Test
    void testArrayRemoveElement2() {
        String[] res = ArrayUtils.removeElement(['a'] as String[], '')
        assert res.length == 1
        assert res[0] == 'a'
    }

    @Test
    void testArrayRemoveElement3() {
        String[] res = ArrayUtils.removeElement(['a'] as String[], 'a')
        assert res.length == 0
    }

    @Test
    void testArrayRemoveElement4() {
        String[] res = ArrayUtils.removeElement(['a', 'b'] as String[], 'a')
        assert res.length == 1
        assert res[0] == 'b'
    }

    @Test
    void testArrayRemoveElement5() {
        String[] res = ArrayUtils.removeElement(['a', 'b'] as String[], 'b')
        assert res.length == 1
        assert res[0] == 'a'
    }

    @Test
    void testArrayRemoveElement6() {
        String[] res = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'b')
        assert res.length == 2
        assert res[0] == 'a'
        assert res[1] == 'c'
    }

    @Test
    void testArrayRemoveElement7() {
        String[] res = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'c')
        assert res.length == 2
        assert res[0] == 'a'
        assert res[1] == 'b'
    }

    @Test
    void testArrayRemoveElement8() {
        String[] res = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'a')
        assert res.length == 2
        assert res[0] == 'b'
        assert res[1] == 'c'
    }

    @Test
    void testArrayAdd1() {
        String[] res = ArrayUtils.add(new String[0], 'a')
        assert res.length == 1
        assert res[0] == 'a'
    }

    @Test
    void testArrayAdd2() {
        String[] res = ArrayUtils.add(['a'] as String[], 'b')
        assert res.length == 2
        assert res[0] == 'a'
        assert res[1] == 'b'
    }

    @Test
    void testArrayAdd3() {
        String[] res = ArrayUtils.add(['a', 'b'] as String[], 'c')
        assert res.length == 3
        assert res[0] == 'a'
        assert res[1] == 'b'
        assert res[2] == 'c'
    }

    @Test
    void testArrayAdd4() {
        String[] res = ArrayUtils.add(['a', 'b'] as String[], 0, 'c')
        assert res.length == 3
        assert res[0] == 'c'
        assert res[1] == 'a'
        assert res[2] == 'b'
    }

    @Test
    void testArrayAdd5() {
        String[] res = ArrayUtils.add(['a', 'b'] as String[], 1, 'c')
        assert res.length == 3
        assert res[0] == 'a'
        assert res[1] == 'c'
        assert res[2] == 'b'
    }

    @Test
    void testArrayAdd6() {
        String[] res = ArrayUtils.add(['a', 'b'] as String[], 2, 'c')
        assert res.length == 3
        assert res[0] == 'a'
        assert res[1] == 'b'
        assert res[2] == 'c'
    }

    @Test
    void testArrayAdd7() {
        String[] res = ArrayUtils.add(['a', 'b', 'd'] as String[], 2, 'c')
        assert res.length == 4
        assert res[0] == 'a'
        assert res[1] == 'b'
        assert res[2] == 'c'
        assert res[3] == 'd'
    }
}
