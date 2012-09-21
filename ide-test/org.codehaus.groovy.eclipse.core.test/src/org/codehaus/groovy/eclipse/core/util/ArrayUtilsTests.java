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

import junit.framework.TestCase;

public class ArrayUtilsTests extends TestCase {
    public void testArrayRemove1() throws Exception {
        String[] res = (String[]) ArrayUtils.remove(new String[] { "" }, 0);
        assertEquals(0, res.length);
    }
    public void testArrayRemove2() throws Exception {
        String[] res = (String[]) ArrayUtils.remove(new String[] { "a", "b" }, 0);
        assertEquals(1, res.length);
        assertEquals("b", res[0]);
    }
    public void testArrayRemove3() throws Exception {
        String[] res = (String[]) ArrayUtils.remove(new String[] { "a", "b" }, 1);
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
    }
    public void testArrayRemove4() throws Exception {
        String[] res = (String[]) ArrayUtils.remove(new String[] { "a", "b", "c" }, 1);
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("c", res[1]);
    }

    public void testArrayRemoveElement1() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { }, "");
        assertEquals(0, res.length);
    }
    public void testArrayRemoveElement2() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a" }, "");
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
    }
    public void testArrayRemoveElement3() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a" }, "a");
        assertEquals(0, res.length);
    }
    public void testArrayRemoveElement4() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a", "b" }, "a");
        assertEquals(1, res.length);
        assertEquals("b", res[0]);
    }
    public void testArrayRemoveElement5() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a", "b" }, "b");
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
    }
    public void testArrayRemoveElement6() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a", "b", "c" }, "b");
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("c", res[1]);
    }
    public void testArrayRemoveElement7() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a", "b", "c" }, "c");
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
    }
    public void testArrayRemoveElement8() throws Exception {
        String[] res = (String[]) ArrayUtils.removeElement(new String[] { "a", "b", "c" }, "a");
        assertEquals(2, res.length);
        assertEquals("b", res[0]);
        assertEquals("c", res[1]);
    }


    public void testArrayAdd1() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[0], "a");
        assertEquals(1, res.length);
        assertEquals("a", res[0]);
    }
    public void testArrayAdd2() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a" }, "b");
        assertEquals(2, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
    }
    public void testArrayAdd3() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a", "b" }, "c");
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
    }

    public void testArrayAdd4() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a", "b" }, 0, "c");
        assertEquals(3, res.length);
        assertEquals("c", res[0]);
        assertEquals("a", res[1]);
        assertEquals("b", res[2]);
    }
    public void testArrayAdd5() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a", "b" }, 1, "c");
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("c", res[1]);
        assertEquals("b", res[2]);
    }
    public void testArrayAdd6() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a", "b" }, 2, "c");
        assertEquals(3, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
    }
    public void testArrayAdd7() throws Exception {
        String[] res = (String[]) ArrayUtils.add(new String[] { "a", "b", "d" }, 2, "c");
        assertEquals(4, res.length);
        assertEquals("a", res[0]);
        assertEquals("b", res[1]);
        assertEquals("c", res[2]);
        assertEquals("d", res[3]);
    }


}
