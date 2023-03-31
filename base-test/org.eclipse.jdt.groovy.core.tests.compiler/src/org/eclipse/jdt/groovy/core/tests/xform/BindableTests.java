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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.beans.Bindable}.
 */
public final class BindableTests extends GroovyCompilerTestSuite {

    @Test // https://github.com/groovy/groovy-eclipse/issues/1412
    public void testBindable1() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
            "    Foo foo = new Foo();\n" +
            "    foo.postConstruct();\n" +
            "    foo.getBar().setBaz(\"xxx\");\n" +
            "  }\n" +
            "}\n",

            "Bar.groovy",
            "class Bar {\n" +
            "  @groovy.beans.Bindable\n" +
            "  String baz\n" +
            "  String other\n" +
            "}\n",

            "Foo.groovy",
            "class Foo {\n" +
            "  Bar bar\n" +
            "  @groovy.beans.Bindable\n" +
            "  String foo\n" +
            "  @groovy.transform.CompileStatic\n" +
            "  void postConstruct() {\n" +
            "    bar = new Bar()\n" +
            "    bar.with {\n" +
            "      addPropertyChangeListener('baz') { event ->\n" +
            "        other = 'value'\n" +
            "        print 'changed'\n" +
            "      }\n" +
            "    }\n" +
            "    print 'ready;'\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runConformTest(sources, "ready;changed", "");
    }
}
