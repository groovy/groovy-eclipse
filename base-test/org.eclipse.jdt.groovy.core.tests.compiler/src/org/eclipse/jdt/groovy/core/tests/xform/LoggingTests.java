/*
 * Copyright 2009-2022 the original author or authors.
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

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.junit.Assume.assumeTrue;

import org.eclipse.jdt.groovy.core.tests.basic.GroovyCompilerTestSuite;
import org.junit.Test;

/**
 * Test cases for {@link groovy.util.logging.Log}, et al.
 */
public final class LoggingTests extends GroovyCompilerTestSuite {

    // TODO: Test category and name attributes

    @Test
    public void testCommons() {
        //@formatter:off
        String[] sources = {
            "CommonsExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Commons\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        vmArguments = new String[] {"-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"};
        addRuntimeLibrary("commons-logging:commons-logging:1.2");
        runConformTest(sources, "", "[INFO] C - yay!");
    }

    @Test
    public void testLog() {
        if (Float.parseFloat(System.getProperty("java.specification.version")) > 8)
            vmArguments = new String[] {"--add-opens", "java.logging/java.util.logging=ALL-UNNAMED"};

        //@formatter:off
        String[] sources = {
            "LogExample.groovy",
            "import groovy.util.logging.*\n" +
            "import java.util.logging.*\n" +
            "@Log\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.addHandler(new ConsoleHandler(formatter:{ record -> \"$record.level: $record.message\".toString() }))\n" +
            "    log.useParentHandlers = false\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        runConformTest(sources, "", "INFO: yay!");
    }

    @Test
    public void testLog4j() {
        assumeTrue(Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "Log4jExample.groovy",
            "import static org.apache.log4j.BasicConfigurator.configure\n" +
            "import groovy.util.logging.*\n" +
            "@Log4j\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "configure()\n" +
            "new C().test()\n",
        };
        //@formatter:on

        addRuntimeLibrary("log4j:log4j:1.2.17");
        runConformTest(sources, "0 [Thread-0] INFO C  - yay!");
    }

    @Test
    public void testLog4j2() {
        assumeTrue(Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "Log4j2Example.groovy",
            "import groovy.util.logging.*\n" +
            "@Log4j2\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "T.groovy",
            "class T implements org.apache.logging.log4j.core.util.Clock {\n" +
            "  long currentTimeMillis() { 0 }\n" +
            "}\n",
        };
        //@formatter:on

        addRuntimeLibrary("org.apache.logging.log4j:log4j-api:2.17.1", "org.apache.logging.log4j:log4j-core:2.17.1");
        vmArguments = new String[] {"-Dorg.apache.logging.log4j.level=INFO", "-Dlog4j2.clock=T"};
        runConformTest(sources, "[main] INFO  C - yay!");
    }

    @Test
    public void testPlatformLog() {
        assumeTrue(isAtLeastJava(JDK9) && isAtLeastGroovy(40) && Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "PlatformLogExample.groovy",
            "import groovy.util.logging.*\n" +
            "@PlatformLog\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.log(System.Logger.Level.INFO) { -> 'yay!' }\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",

            "T.groovy",
            "class T implements org.apache.logging.log4j.core.util.Clock {\n" +
            "  long currentTimeMillis() { 0 }\n" +
            "}\n",
        };
        //@formatter:on

        addRuntimeLibrary("org.apache.logging.log4j:log4j-api:2.17.1", "org.apache.logging.log4j:log4j-core:2.17.1", "org.apache.logging.log4j:log4j-jpl:2.17.1");
        vmArguments = new String[] {"-Dorg.apache.logging.log4j.level=INFO", "-Dlog4j2.clock=T"};
        runConformTest(sources, "[main] INFO  C - yay!");
    }

    @Test
    public void testSlf4j() {
        assumeTrue(Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "Slf4jExample.groovy",
            "import groovy.util.logging.*\n" +
            "@Slf4j\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    log.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        addRuntimeLibrary("org.slf4j:slf4j-simple:1.7.36");
        runConformTest(sources, "", "[Thread-0] INFO C - yay!");
    }

    @Test
    public void testSlf4j_5736() {
        assumeTrue(Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "Groovy5736.groovy",
            "import groovy.transform.CompileStatic\n" +
            "import groovy.util.logging.Slf4j\n" +
            "@CompileStatic @Slf4j('LOG')\n" +
            "class C {\n" +
            "  void test() {\n" +
            "    LOG.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        addRuntimeLibrary("org.slf4j:slf4j-simple:1.7.36");
        runConformTest(sources, "", "[Thread-0] INFO C - yay!");
    }

    @Test
    public void testSlf4j_7439() {
        assumeTrue(isAtLeastGroovy(40) && Boolean.getBoolean("eclipse.pde.launch"));

        //@formatter:off
        String[] sources = {
            "Groovy7439.groovy",
            "import groovy.transform.CompileStatic\n" +
            "import groovy.util.logging.Slf4j\n" +
            "@CompileStatic @Slf4j('LOG')\n" +
            "trait T {\n" +
            "  void test() {\n" +
            "    LOG.info('yay!')\n" +
            "  }\n" +
            "}\n" +
            "class C implements T {\n" +
            "}\n" +
            "new C().test()\n",
        };
        //@formatter:on

        addRuntimeLibrary("org.slf4j:slf4j-simple:1.7.36");
        runConformTest(sources, "", "[Thread-0] INFO T$Trait$Helper - yay!");
    }

    @Test // GROOVY-5736
    public void testUnresolved() {
        //@formatter:off
        String[] sources = {
            "Groovy5736.groovy",
            "@groovy.util.logging.Slf4j()\n" +
            "class Groovy5736 {\n" +
            "}\n",
        };
        //@formatter:on

        runNegativeTest(sources,
            "----------\n" +
            "1. WARNING in Groovy5736.groovy (at line 1)\n" +
            "\t@groovy.util.logging.Slf4j()\n" +
            "\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
            "Groovy:Unable to resolve class: org.slf4j.Logger\n" +
            "----------\n");
    }

    //--------------------------------------------------------------------------

    private void addRuntimeLibrary(String... spec) {
        java.util.Map<String, Object> args = new java.util.HashMap<>();
        args.put("classLoader", new groovy.lang.GroovyClassLoader());

        java.util.Map<String, String>[] deps = java.util.Arrays.stream(spec).map(this::toMap).toArray(java.util.Map[]::new);

        cpAdditions = java.util.Arrays.stream(groovy.grape.Grape.resolve(args, deps)).map(uri -> uri.getPath()).toArray(String[]::new);
    }

    private java.util.Map<String, String> toMap(String spec) {
        String[] tokens = spec.split(":");

        java.util.Map<String, String> map = new java.util.HashMap<>();
        map.put("group", tokens[0]);
        map.put("module", tokens[1]);
        map.put("version", tokens[2]);
        return map;
    }
}
