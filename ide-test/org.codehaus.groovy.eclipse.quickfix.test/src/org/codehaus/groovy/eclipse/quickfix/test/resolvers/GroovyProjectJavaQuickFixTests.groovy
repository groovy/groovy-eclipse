/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.test.resolvers

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.quickfix.test.QuickFixTestSuite
import org.junit.Test

/**
 * Tests Groovy quick fixes for Java files in a Groovy project.
 */
@CompileStatic
final class GroovyProjectJavaQuickFixTests extends QuickFixTestSuite {

    @Test
    void testNoGroovyAddImportQuickFix() {
        def unit = addJavaSource('''\
            |class C {
            |  void doSomething() {
            |    ImageBuilder imageBuilder = null;
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 0 : "Expected no Groovy quick fixes for unresolved type: ImageBuilder in $unit.resource.name, as it is a Java file"
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword1() {
        def unit = addJavaSource('''\
            |class C {
            |  def getSomething() {
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword2() {
        def unit = addJavaSource('''\
            |class C {
            |  void doSomething(Object x) {
            |    List l = x as List;
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword3() {
        def unit = addJavaSource('''\
            |class C {
            |  void doSomething(Object x) {
            |    for (Object i in x) {
            |    }
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixGroovyKeyword4() {
        def unit = addJavaSource('''\
            |trait T {
            |  void doSomething() {
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon1() {
        def unit = addJavaSource('''\
            |class C {
            |  void doSomething() {
            |    ImageBuilder imageBuilder = null
            |  }
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }

    @Test
    void testConvertToGroovyQuickFixMissingSemiColon2() {
        def unit = addJavaSource('''\
            |abstract class C {
            |  abstract void doSomething()
            |}'''.stripMargin())

        def proposals = getGroovyQuickFixes(unit)

        assert proposals.length == 1 : 'Expected a convert to Groovy file quick fix proposal; none found'
        assert proposals[0].displayString == 'Convert to Groovy file and open in Groovy editor' : 'Display string mismatch for convert to Groovy quick fix'
    }
}
