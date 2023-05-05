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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public final class SpockInferencingTests extends InferencingTestSuite {

    @Before
    public void setUp() throws Exception {
        String spockCorePath;
        if (isAtLeastGroovy(40)) {
            spockCorePath = "lib/spock-core-2.3-groovy-4.0.jar";
        } else {
            spockCorePath = "lib/spock-core-2.3-groovy-3.0.jar";
        }
        env.addJar(project.getFullPath(), spockCorePath);
        if (isAtLeastGroovy(50)) System.setProperty("spock.iKnowWhatImDoing.disableGroovyVersionCheck", "true");
        env.addEntry(project.getFullPath(), JavaCore.newContainerEntry(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/5")));
    }

    @Test
    public void testBasics() {
        createUnit("foo", "Bar", "package foo; class Bar {\n Integer baz\n}");

        //@formatter:off
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
        //@formatter:on

        int offset = source.indexOf("bar");
        assertType(source, offset, offset + 3, "foo.Bar");

        offset = source.indexOf("bar", offset + 3);
        assertType(source, offset, offset + 3, "foo.Bar");
    }

    @Test
    public void testEqualsCheck() {
        createUnit("foo", "Bar", "package foo; class Bar {\n Integer baz\n}");

        //@formatter:off
        String source =
            "final class SpockTests extends spock.lang.Specification {\n" +
            "  void 'test the property'() {\n" +
            "   given:\n" +
            "    def bar = new foo.Bar()\n" +
            "    \n" +
            "   expect:\n" +
            "    !bar.equals(null)\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = source.lastIndexOf("equals");
        assertType(source, offset, offset + 6, "java.lang.Boolean");
        assertDeclaringType(source, offset, offset + 6, "java.lang.Object");
    }

    @Test
    public void testGetterCheck() {
        createUnit("foo", "Bar", "package foo; class Bar {\n Integer baz\n}");

        //@formatter:off
        String source =
            "final class SpockTests extends spock.lang.Specification {\n" +
            "  void 'test the property'() {\n" +
            "   given:\n" +
            "    def bar = new foo.Bar(baz: 42)\n" +
            "    \n" +
            "   expect:\n" +
            "    bar.getBaz() == 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = source.lastIndexOf("getBaz");
        assertType(source, offset, offset + 6, "java.lang.Integer");
        assertDeclaringType(source, offset, offset + 6, "foo.Bar");
    }

    @Test
    public void testPropertyCheck() {
        createUnit("foo", "Bar", "package foo; class Bar {\n Integer baz\n}");

        //@formatter:off
        String source =
            "final class SpockTests extends spock.lang.Specification {\n" +
            "  void 'test the property'() {\n" +
            "   given:\n" +
            "    def bar = new foo.Bar(baz: 42)\n" +
            "    \n" +
            "   expect:\n" +
            "    bar.baz == 42\n" +
            "  }\n" +
            "}\n";
        //@formatter:on

        int offset = source.lastIndexOf("baz");
        assertType(source, offset, offset + 3, "java.lang.Integer");
    }

    @Test @Ignore("see #814") // https://github.com/groovy/groovy-eclipse/issues/814
    public void testDataTableChecks() {
        //@formatter:off
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
        //@formatter:on

        int offset = source.indexOf("a == b");
        assertType(source, offset, offset + 1, "java.lang.Object");
        assertDeclaringType(source, offset, offset + 1, "SpockTests");
    }
}
