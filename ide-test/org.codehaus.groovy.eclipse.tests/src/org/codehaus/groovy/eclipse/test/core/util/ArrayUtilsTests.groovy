/*
 * Copyright 2009-2023 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.core.util

import org.eclipse.jdt.groovy.core.util.ArrayUtils
import org.junit.Test

@groovy.transform.CompileStatic
final class ArrayUtilsTests {

    @Test
    void testArrayAdd1() {
        String[] out = ArrayUtils.add(new String[0], 'a')
        assert out.length == 1
        assert out[0] == 'a'
    }

    @Test
    void testArrayAdd2() {
        String[] out = ArrayUtils.add(['a'] as String[], 'b')
        assert out.length == 2
        assert out[0] == 'a'
        assert out[1] == 'b'
    }

    @Test
    void testArrayAdd3() {
        String[] out = ArrayUtils.add(['a', 'b'] as String[], 'c')
        assert out.length == 3
        assert out[0] == 'a'
        assert out[1] == 'b'
        assert out[2] == 'c'
    }

    @Test
    void testArrayAdd4() {
        String[] out = ArrayUtils.add(['a', 'b'] as String[], 0, 'c')
        assert out.length == 3
        assert out[0] == 'c'
        assert out[1] == 'a'
        assert out[2] == 'b'
    }

    @Test
    void testArrayAdd5() {
        String[] out = ArrayUtils.add(['a', 'b'] as String[], 1, 'c')
        assert out.length == 3
        assert out[0] == 'a'
        assert out[1] == 'c'
        assert out[2] == 'b'
    }

    @Test
    void testArrayAdd6() {
        String[] out = ArrayUtils.add(['a', 'b'] as String[], 2, 'c')
        assert out.length == 3
        assert out[0] == 'a'
        assert out[1] == 'b'
        assert out[2] == 'c'
    }

    @Test
    void testArrayAdd7() {
        String[] out = ArrayUtils.add(['a', 'b', 'd'] as String[], 2, 'c')
        assert out.length == 4
        assert out[0] == 'a'
        assert out[1] == 'b'
        assert out[2] == 'c'
        assert out[3] == 'd'
    }

    @Test
    void testArrayConcat1() {
        String[] in1 = ['a','b',], in2 = ['c','d']
        String[] out = ArrayUtils.concat(in1, in2)
        assert out.length == 4
        assert out[0] == 'a'
        assert out[1] == 'b'
        assert out[2] == 'c'
        assert out[3] == 'd'
    }

    @Test
    void testArrayConcat2() {
        String[] in1 = ['a','b','c','d'], in2 = []
        String[] out = ArrayUtils.concat(in1, in2)
        assert out.length == 4
        assert out[0] == 'a'
        assert out[1] == 'b'
        assert out[2] == 'c'
        assert out[3] == 'd'
    }

    @Test
    void testArrayRemove1() {
        String[] out = ArrayUtils.remove([''] as String[], 0)
        assert out.length == 0
    }

    @Test
    void testArrayRemove2() {
        String[] out = ArrayUtils.remove(['a', 'b'] as String[], 0)
        assert out.length == 1
        assert out[0] == 'b'
    }

    @Test
    void testArrayRemove3() {
        String[] out = ArrayUtils.remove(['a', 'b'] as String[], 1)
        assert out.length == 1
        assert out[0] == 'a'
    }

    @Test
    void testArrayRemove4() {
        String[] out = ArrayUtils.remove(['a', 'b', 'c'] as String[], 1)
        assert out.length == 2
        assert out[0] == 'a'
        assert out[1] == 'c'
    }

    @Test
    void testArrayRemoveElement1() {
        String[] out = ArrayUtils.removeElement([] as String[], '')
        assert out.length == 0
    }

    @Test
    void testArrayRemoveElement2() {
        String[] out = ArrayUtils.removeElement(['a'] as String[], '')
        assert out.length == 1
        assert out[0] == 'a'
    }

    @Test
    void testArrayRemoveElement3() {
        String[] out = ArrayUtils.removeElement(['a'] as String[], 'a')
        assert out.length == 0
    }

    @Test
    void testArrayRemoveElement4() {
        String[] out = ArrayUtils.removeElement(['a', 'b'] as String[], 'a')
        assert out.length == 1
        assert out[0] == 'b'
    }

    @Test
    void testArrayRemoveElement5() {
        String[] out = ArrayUtils.removeElement(['a', 'b'] as String[], 'b')
        assert out.length == 1
        assert out[0] == 'a'
    }

    @Test
    void testArrayRemoveElement6() {
        String[] out = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'b')
        assert out.length == 2
        assert out[0] == 'a'
        assert out[1] == 'c'
    }

    @Test
    void testArrayRemoveElement7() {
        String[] out = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'c')
        assert out.length == 2
        assert out[0] == 'a'
        assert out[1] == 'b'
    }

    @Test
    void testArrayRemoveElement8() {
        String[] out = ArrayUtils.removeElement(['a', 'b', 'c'] as String[], 'a')
        assert out.length == 2
        assert out[0] == 'b'
        assert out[1] == 'c'
    }
}
