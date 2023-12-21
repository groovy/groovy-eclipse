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
 * Test cases for {@link groovy.lang.Grab}, et al.
 */
public final class GrabTests extends GroovyCompilerTestSuite {

    @Test
    public void testGrab() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
                // not concerned with execution
            "  }\n" +
            "}\n",

            "Test.groovy",
            "@Grab('joda-time:joda-time:2.12.5;transitive=false')\n" +
            "import org.joda.time.DateTime\n" +
            "def now = new DateTime()\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    /**
     * This program has a broken grab. Without changes we get a 'general error' recorded on the first line of the source file:
     * <tt>General error during conversion: Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.x: not found]</tt>
     * <p>
     * With grab improvements we get two errors: the missing dependency and the missing type (which is at the right version of that dependency!)
     */
    @Test // GRECLIPSE-1669
    public void testGrabError() {
        //@formatter:off
        String[] sources = {
            "Test.groovy",
            "@Grapes([\n" +
            "  @Grab('joda-time:joda-time:2.12.5;transitive=false'),\n" +
            "  @Grab(group='org.aspectj', module='aspectjweaver', version='1.x')\n" +
            "])\n" +
            "class Test {\n" +
            "  void printDate() {\n" +
            "    def dt = new org.joda.time.DateTime()\n" +
            "    def world = new org.aspectj.weaver.bcel.BcelWorld()\n" +
            "    print dt\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Test.groovy (at line 3)\n" +
            "\t@Grab(group='org.aspectj', module='aspectjweaver', version='1.x')\n" +
            "\t^^^^^\n" +
            "Groovy:Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.x: not found]\n" +
            "----------\n" +
            "2. ERROR in Test.groovy (at line 8)\n" +
            "\tdef world = new org.aspectj.weaver.bcel.BcelWorld()\n" +
            "\t                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.aspectj.weaver.bcel.BcelWorld\n" +
            "----------\n");
    }

    @Test // GROOVY-11046
    public void testGrabError2() {
        //@formatter:off
        String[] sources = {
            "Main.groovy",
            "@Grab('org.apache.logging.log4j:log4j-core:2.22.0')\n" +
            "org.apache.logging.log4j.core.async.AsyncLogger log\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Main.groovy (at line 2)\n" +
            "\torg.apache.logging.log4j.core.async.AsyncLogger log\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to define class org.apache.logging.log4j.core.async.AsyncLogger : com/lmax/disruptor/EventTranslatorVararg\n" +
            "----------\n");
    }

    @Test // GRECLIPSE-1432
    public void testGrabConfig() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
                // not concerned with execution
            "  }\n" +
            "}\n",

            "Test.groovy",
            "@GrabConfig(systemClassLoader=true)\n" +
            "@Grab(group='mysql', module='mysql-connector-java', version='5.1.49', transitive=false)\n" +
            "class Test {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test // GROOVY-9376
    public void testGrabResolver() {
        //@formatter:off
        String[] sources = {
            "Main.java",
            "public class Main {\n" +
            "  public static void main(String[] args) {\n" +
                // not concerned with execution
            "  }\n" +
            "}\n",

            "Test.groovy",
            "@GrabResolver(name='restlet', root='https://maven.restlet.talend.com')\n" +
            "@Grab(group='org.restlet.jse', module='org.restlet', version='2.4.3', transitive=false)\n" +
            "import org.restlet.Restlet\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }
}
