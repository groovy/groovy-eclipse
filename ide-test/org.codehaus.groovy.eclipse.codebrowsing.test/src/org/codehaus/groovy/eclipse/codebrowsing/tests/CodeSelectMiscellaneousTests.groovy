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
package org.codehaus.groovy.eclipse.codebrowsing.tests

import org.junit.Test

final class CodeSelectMiscellaneousTests extends BrowsingTestSuite {

    @Test
    void testSelectArrowInClosure1() {
        String contents = 'def closure = { -> }'
        assertCodeSelect([contents], '->', null)
    }

    @Test
    void testSelectArrowInClosure2() {
        String contents = 'def closure = { -> return null }'
        assertCodeSelect([contents], '->', null)
    }

    @Test
    void testSelectArrowInClosure3() {
        String contents = 'def closure = { String param -> return null }'
        assertCodeSelect([contents], '->', null)
    }

    @Test
    void testSelectColonInAssert1() {
        String contents = 'assert false : "message"'
        assertCodeSelect([contents], ':', null)
    }

    @Test
    void testSelectColonInForEach1() {
        String contents = 'for (Object item : []){}'
        assertCodeSelect([contents], ':', null)
    }

    @Test
    void testSelectDotsInRange1() {
        String contents = 'def range = 1..9'
        assertCodeSelect([contents], '..', null)
    }

    @Test
    void testSelectDotsInRange2() {
        String contents = 'def range = 1..<9'
        assertCodeSelect([contents], '..<', null)
    }

    @Test
    void testSelectStatementLabel1() {
        String contents = 'def m(){ label: for (item in []){} }'
        assertCodeSelect([contents], 'label', null)
    }

    @Test
    void testSelectStatementLabel2() {
        String contents = '''\
            |class C {
            |  def m1() {
            |    def anon= new Object() {
            |      def m2() {
            |        label: for (item in []) {}
            |      }
            |    }
            |  }
            |}
            |'''.stripMargin()
        assertCodeSelect([contents], 'label', null)
    }
}
