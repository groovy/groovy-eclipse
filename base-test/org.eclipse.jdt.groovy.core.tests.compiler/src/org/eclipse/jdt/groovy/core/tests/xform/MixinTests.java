/*
 * Copyright 2009-2021 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.lang.Mixin}.
 */
public final class MixinTests extends GroovyCompilerTestSuite {

    @Test
    public void testMixin1() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {\n" +
            "  def getB() {\n" +
            "    'works'\n" +
            "  }\n" +
            "}\n" +
            "@Mixin(A)\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    print b\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }

    @Test // GROOVY-10200
    public void testMixin2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "class A {\n" +
            "  def getB() {\n" +
            "    'works'\n" +
            "  }\n" +
            "}\n" +
            "class C {\n" +
            "  @Mixin(A)\n" +
            "  static class D {\n" +
            "    void test() {\n" +
            "      print b\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "new C.D().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "works");
    }
}
