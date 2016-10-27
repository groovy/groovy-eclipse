/*
 * Copyright 2009-2016 the original author or authors.
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
package org.eclipse.jdt.groovy.core.tests.basic;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public final class GroovySimpleTests_Compliance_1_8 extends AbstractGroovyRegressionTest {

    public static Test suite() {
        TestSuite suite = new TestSuite(ErrorRecoveryTests.class.getName());
        suite.addTest(buildUniqueComplianceTestSuite(GroovySimpleTests_Compliance_1_8.class, JDK1_8));
        return suite;
    }

    public GroovySimpleTests_Compliance_1_8(String name) {
        super(name);
    }

    public void testDefaultAndStaticMethodInInterface() {
        if (GroovyUtils.GROOVY_LEVEL < 23 || !isJRELevel(AbstractCompilerTest.F_1_8)) return;

        Map<String, String> customOptions = getCompilerOptions();
        customOptions.put(CompilerOptions.OPTION_Source, VERSION_1_8);

        runConformTest(
            true, // flush output directory
            new String[] {
                "p/IExample.java",
                "package p;\n" + "public interface IExample {\n" +
                "   void testExample();\n" +
                "   static void callExample() {}\n" +
                "   default void callDefault() {}\n" +
                "}\n",

                "p/ExampleGr.groovy",
                "package p\n" + "class ExampleGr implements IExample {\n" +
                "public void testExample() {}\n" + "}\n"
            },
            null, // no class libraries
            customOptions, // custom options
            "", // expected compiler log
            "", // expected output string
            null, // do not check error string
            new JavacTestOptions("-source 1.8")
        );
    }
}
