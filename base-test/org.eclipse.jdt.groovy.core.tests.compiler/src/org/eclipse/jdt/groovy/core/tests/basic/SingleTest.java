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

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public final class SingleTest extends AbstractGroovyRegressionTest {

    public void testGreclipse719_2() {
        this.runNegativeTest(new String[] {
            "MyDomainClass.groovy",
            "int anInt = 10;\n"+
            "def Method[][] methodMethodArray = anInt.class.methods;\n"+
            "println methodArray.name;"},
            "----------\n" +
            "1. ERROR in MyDomainClass.groovy (at line 2)\n" +
            "	def Method[][] methodMethodArray = anInt.class.methods;\n" +
            "	    ^^^^^^^^^^\n" +
            "Groovy:unable to resolve class Method[][] \n" +
            "----------\n");
    }

    public SingleTest(String name) {
        super(name);
    }

    public static Test suite() {
        return buildUniqueComplianceTestSuite(SingleTest.class, ClassFileConstants.JDK1_5);
    }

    protected void setUp() throws Exception {
        super.setUp();
        complianceLevel = ClassFileConstants.JDK1_5;
    }
}
