/*
 * Copyright 2009-2019 the original author or authors.
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
 * Test cases for {@link groovy.util.logging.Log}, et al.
 */
public final class LoggingTests extends GroovyCompilerTestSuite {

    @Test
    public void testCommons() {
        //@formatter:off
        String[] sources = {
            "CommonsExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Commons\n" +
            "class CommonsExample {\n" +
            "  def meth() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testLog() {
        //@formatter:off
        String[] sources = {
            "LogExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Log\n" +
            "class LogExample {\n" +
            //"  static void main(args) {\n" +
            //"    new LogExample().meth()\n" +
            //"  }\n" +
            "  def meth() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, ""); // TODO: Test the output produced
    }

    // TODO: Test category and name attributes

    @Test
    public void testLog4j() {
        //@formatter:off
        String[] sources = {
            "Log4jExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Log4j\n" +
            "class Log4jExample {\n" +
            "  def meth() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testLog4j2() {
        //@formatter:off
        String[] sources = {
            "Log4j2Example.groovy",
            "import groovy.util.logging.*\n" +
            "@Log4j2\n" +
            "class Log4j2Example {\n" +
            "  def meth() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }

    @Test
    public void testSlf4j() {
        //@formatter:off
        String[] sources = {
            "Slf4jExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Slf4j\n" +
            "class Slf4jExample {\n" +
            "  def meth() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources, "");
    }
}
