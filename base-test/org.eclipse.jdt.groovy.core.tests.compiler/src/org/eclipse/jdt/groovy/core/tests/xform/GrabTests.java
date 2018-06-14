/*
 * Copyright 2009-2018 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.xform;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for {@link groovy.lang.Grab}, et al.
 */
@Ignore("Grab is failing on CI server")
public final class GrabTests extends GroovyCompilerTestSuite {

    @Test
    public void testGrab1() {
        String[] sources = {
            "Printer.groovy",
            "@Grab('joda-time:joda-time:2.10')\n" +
            "def printDate() {\n" +
            "  def dt = new org.joda.time.DateTime()\n" +
            "}\n" +
            "printDate()\n",
        };

        runNegativeTest(sources, "");
    }

    /**
     * Improving grab, this program has a broken grab. Without changes we get a 'general error' recorded on the first line of the source file (big juicy exception)
     * General error during conversion: Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found] java.lang.RuntimeException: Error grabbing
     * Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found] at sun.reflect.GeneratedConstructorAccessor48.newInstance(Unknown Source) at
     * sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27) at java.lang.reflect.Constructor.newInstance(Constructor.java:513) at
     * org.codehaus.groovy.reflection.CachedConstructor.invoke(CachedConstructor.java:77) at ...
     * With grab improvements we get two errors - the missing dependency and the missing type (which is at the right version of that dependency!)
     */
    @Test
    public void testGrab2() {
        String[] sources = {
            "Grab1.groovy",
            "@Grapes([\n" +
            "  @Grab(group='joda-time', module='joda-time', version='1.6'),\n" +
            "  @Grab(group='org.aspectj', module='aspectjweaver', version='1.6.11x')\n" +
            "])\n" +
            "class C {\n" +
            "  def printDate() {\n" +
            "    def dt = new org.joda.time.DateTime()\n" +
            "    def world = new org.aspectj.weaver.bcel.BcelWorld()\n" +
            "    print dt\n" +
            "  }\n" +
            "  public static void main(String[] argv) {\n" +
            "    new C().printDate()\n" +
            "  }\n" +
            "}\n",
        };

        runNegativeTest(sources,
            "----------\n" +
            "1. ERROR in Grab1.groovy (at line 3)\n" +
            "\t@Grab(group='org.aspectj', module='aspectjweaver', version='1.6.11x')\n" +
            "\t ^^^\n" +
            "Groovy:Error grabbing Grapes -- [unresolved dependency: org.aspectj#aspectjweaver;1.6.11x: not found]\n" +
            "----------\n" +
            "2. ERROR in Grab1.groovy (at line 8)\n" +
            "\tdef world = new org.aspectj.weaver.bcel.BcelWorld()\n" +
            "\t                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:unable to resolve class org.aspectj.weaver.bcel.BcelWorld \n" +
            "----------\n");
    }

    @Test
    public void testGrabScriptAndImports_GRE680() {
        String[] sources = {
            "Script.groovy",
            "import org.mortbay.jetty.Server\n" +
            "import org.mortbay.jetty.servlet.*\n" +
            "import groovy.servlet.*\n" +
            "\n" +
            "@Grab(group='org.mortbay.jetty', module='jetty-embedded', version='6.1.0')\n" +
            "def runServer(duration) { }\n" +
            "runServer(10000)\n",
        };

        runNegativeTest(sources, "");
    }
}
