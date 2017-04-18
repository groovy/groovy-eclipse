/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.tests

import org.junit.Test

final class GuessingCompletionTests extends CompletionTestCase {

    @Test
    void testParamGuessing1() throws Exception {
        String contents = "String yyy\n" +
            "def xxx(String x) { }\n" +
            "xxx"
        String[][] expectedChoices = [ [ "yyy", "\"\"" ] as String[] ].toArray()
        checkProposalChoices(contents, "xxx", "xxx(yyy)", expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        String contents =
            "String yyy\n" +
            "int zzz\n" +
            "def xxx(String x, int z) { }\n" +
            "xxx"
        String[][] expectedChoices = [
            [ "yyy", "\"\"" ] as String[],
            [ "zzz", "0" ] as String[]
        ].toArray()
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz)", expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        String contents =
            "String yyy\n" +
            "Integer zzz\n" +
            "boolean aaa\n" +
            "def xxx(String x, int z, boolean a) { }\n" +
            "xxx"
        String[][] expectedChoices = [
            [ "yyy", "\"\"" ] as String[],
            [ "zzz", "0" ] as String[],
            [ "aaa", "false", "true" ] as String[]
        ].toArray()
        checkProposalChoices(contents, "xxx", "xxx(yyy, zzz, aaa)", expectedChoices)
    }

    @Test // GRECLIPSE-1268  This test may fail in some environments since the ordering of
    // guessed parameters is not based on actual source location.  Need a way to map
    // from variable name to local variable declaration in GroovyExtendedCompletionContext.computeVisibleElements(String)
    void testParamGuessing4() {
        String contents =
            "Closure yyy\n" +
            "def zzz = { }\n" +
            "def xxx(Closure c) { }\n" +
            "xxx"
        String[][] expectedChoices = [
            ["zzz", "yyy", "{  }"] as String[]
        ].toArray()
        try {
            checkProposalChoices(contents, "xxx", "xxx {", expectedChoices)
        } catch (AssertionError e) {
            try {
                checkProposalChoices(contents, "xxx", "xxx yyy", expectedChoices)
            } catch (AssertionError e2) {
                checkProposalChoices(contents, "xxx", "xxx zzz", expectedChoices)
            }
        }
    }
}
