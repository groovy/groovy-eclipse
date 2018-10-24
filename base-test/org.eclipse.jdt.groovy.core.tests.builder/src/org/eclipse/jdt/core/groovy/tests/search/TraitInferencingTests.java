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
package org.eclipse.jdt.core.groovy.tests.search;

import org.junit.Test;

public final class TraitInferencingTests extends InferencingTestSuite {

    private void assertDeclType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertDeclaringType(source, offset, offset + target.length(), type);
    }

    private void assertExprType(String source, String target, String type) {
        final int offset = source.lastIndexOf(target);
        assertType(source, offset, offset + target.length(), type);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testPrivateMethod() {
        String source =
            "trait Auditable {\n" +
            "  private boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8854
    public void testPrivateStaticMethod() {
        String source =
            "trait Auditable {\n" +
            "  private static boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "check", "Auditable");
        assertExprType(source, "check", "java.lang.Boolean");
    }

    @Test // https://issues.apache.org/jira/browse/GROOVY-8272
    public void testPublicStaticSuperMethod() {
        String source =
            "trait Checkable {\n" +
            "  static boolean check() {\n" +
            "    true\n" +
            "  }\n" +
            "}\n" +
            "trait Auditable extends Checkable {\n" +
            "  boolean audit() {\n" +
            "    if (check()) {\n" +
            "      ;\n" +
            "    }\n" +
            "  }\n" +
            "}\n";

        assertDeclType(source, "check", "Checkable");
        assertExprType(source, "check", "java.lang.Boolean");
    }
}
