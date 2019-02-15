/*
 * Copyright 2009-2019 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeFalse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class SpockInferencingTests extends InferencingTestSuite {

    private String xforms;

    @Before
    public void setUp() throws Exception {
        assumeFalse(isAtLeastGroovy(30)); // TODO: Remove when spock-core supports Groovy 3

        IPath projectPath = project.getFullPath();
        env.addJar(projectPath, "lib/spock-core-1.2-groovy-2.4.jar");
        env.addEntry(projectPath, JavaCore.newContainerEntry(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/4")));

        xforms = System.setProperty("greclipse.globalTransformsInReconcile", "org.spockframework.compiler.SpockTransform");
    }

    @After
    public void tearDown() {
        if (xforms == null) {
            System.clearProperty("greclipse.globalTransformsInReconcile");
        } else {
            System.setProperty("greclipse.globalTransformsInReconcile", xforms);
        }
    }

    @Test
    public void testBasics() throws Exception {
        createUnit("foo", "Bar", "package foo; class Bar {\n Integer baz\n}");

        String source =
            "final class SpockTests extends spock.lang.Specification {\n" +
            "  void 'test the basics'() {\n" +
            "   given:\n" +
            "    def bar = new foo.Bar()\n" +
            "    \n" +
            "   expect:\n" +
            "    bar == bar\n" +
            "    bar.equals(bar)\n" +
            "    !bar.equals(null)\n" +
            "    bar != new foo.Bar(baz:42)\n" +
            "  }\n" +
            "}\n";

        int offset = source.indexOf("bar");
        assertType(source, offset, offset + 3, "foo.Bar");

        offset = source.indexOf("bar", offset + 3);
        assertType(source, offset, offset + 3, "foo.Bar");
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/812
    public void testDataTable() {
        String source =
            "final class SpockTests extends spock.lang.Specification {\n" +
            "  @spock.lang.Unroll\n" +
            "  void 'test #a == #b'() {\n" +
            "   expect:\n" +
            "    a == b\n" +
            "   where:\n" +
            "    a | b\n" +
            "    1 | 1\n" +
            "    2 | a\n" +
            "  }\n" +
            "}\n";

        int offset = source.indexOf("a == b");
        assertType(source, offset, offset + 1, "java.lang.Object");
        assertDeclaringType(source, offset, offset + 1, "SpockTests");
    }
}
