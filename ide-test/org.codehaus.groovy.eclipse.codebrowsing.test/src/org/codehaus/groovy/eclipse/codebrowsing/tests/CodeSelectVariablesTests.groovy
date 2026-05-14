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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Test

final class CodeSelectVariablesTests extends BrowsingTestSuite {

    @Test
    void testSelectLocalVar1() {
        assertCodeSelect(['def xxx(xxx) { xxx }'], 'xxx')
    }

    @Test
    void testSelectLocalVar2() {
        assertCodeSelect(['def xxx(xxx) { "${xxx}" }'], 'xxx')
    }

    @Test
    void testSelectLocalVar3() {
        assertCodeSelect(['def xxx = { xxx -> "${xxx}" }'], 'xxx')
    }

    @Test
    void testSelectLocalVar4() {
        String contents = 'def (xxx, yyy) = []\nxxx\nyyy'
        assertCodeSelect([contents], 'xxx')
        assertCodeSelect([contents], 'yyy')
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/355
    void testSelectLocalVar5() {
        addJavaSource 'class Calendar { static Calendar instance() { return null; } }', 'Calendar', 'domain'
        String contents = 'def cal = domain.Calendar.instance()'
        def elem = assertCodeSelect([contents], 'cal')
        assert elem.typeSignature =~ 'domain.Calendar'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1503
    void testSelectLocalVar6() {
        String contents = 'abstract class S extends Script { }\n int x; x'
        def elem = assertCodeSelect([contents], 'x')
        assert elem.typeSignature == 'I'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1503
    void testSelectLocalVar7() {
        String contents = '''\
            |abstract class S extends Script {
            |  abstract m()
            |  def run() {
            |    m()
            |  }
            |}
            |@groovy.transform.BaseScript S s
            |int x = 0
            |x
            |'''.stripMargin()
        def elem = assertCodeSelect([contents], 'x')
        assert elem.typeSignature == 'I'
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1170
    void testSelectParameter() {
        String contents = '''\
            |@groovy.transform.CompileStatic
            |void test(CharSequence xxx) {
            |  if (xxx instanceof Serializable) {
            |    xxx
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'xxx')
    }

    @Test // GRECLIPSE-1330
    void testSelectLocalVarInGString1() {
        String contents = 'def i\n"$i"'
        assertCodeSelect([contents], 'i')
    }

    @Test // GRECLIPSE-1330
    void testSelectLocalVarInGString2() {
        String contents = 'def i\n"$i"'
        assertCodeSelect([contents], '$i', 'i')
    }

    @Test // GRECLIPSE-1330
    void testSelectLocalVarInGString3() {
        String contents = 'def i\n"${i}"'
        assertCodeSelect([contents], 'i')
    }
}
