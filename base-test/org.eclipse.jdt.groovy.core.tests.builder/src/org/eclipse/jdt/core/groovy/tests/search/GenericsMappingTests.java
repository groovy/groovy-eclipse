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
package org.eclipse.jdt.core.groovy.tests.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.junit.Test;

public final class GenericsMappingTests extends SearchTestSuite {

    @Test // GRECLIPSE-1448: unresolved super types should use a redirect; resolved super types should not
    public void testGenericsMapper() throws Exception {
        GroovyCompilationUnit unit = createUnit("Search",
            "class A { }\n" +
            "class B extends A { }\n" +
            "class C extends B { }");

        ClassNode classNode = unit.getModuleNode().getClasses().get(2);
        assertEquals("Got the wrong class nodee", "C", classNode.getName());
        assertFalse("resolved super types should not be redirects", classNode.getSuperClass().isRedirectNode());
        assertTrue("unresolved super types should be redirects", classNode.getUnresolvedSuperClass().isRedirectNode());
    }
}
