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
package org.codehaus.groovy.eclipse.refactoring.test.extract

final class ConvertLocalToFieldTestsData {

    static class TestCase {
        final String input
        final String expected
        final String fieldName
        final boolean expectWarning

        TestCase(String input, String expected = null, String fieldName = 'xxx', boolean expectWarning = false) {
            this.input = input
            this.expected = expected
            this.fieldName = fieldName
            this.expectWarning = expectWarning
        }

        int getSelectionOffset() {
            input.indexOf('xxx/**/')
        }

        int getSelectionLength() {
            'xxx'.length()
        }
    }

    static Map<String, TestCase> testCases = [
        testMethodToModule:
        new TestCase('def test() { def xxx/**/ }', '@groovy.transform.Field def xxx\ndef test() { xxx/**/ }'),

        testClosureToModule:
        new TestCase('def test = { def xxx/**/ }', '@groovy.transform.Field def xxx\ndef test = { xxx/**/ }'),

        testDeclarationWithDef:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx/**/
            |\t}
            |}
            |'''.stripMargin()
        ),

        testDeclarationWithType:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tString xxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate String xxx
            |\tdef test() {
            |\t\txxx/**/
            |\t}
            |}
            |'''.stripMargin()
        ),

        testReference:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx
            |\t\txxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx
            |\t\txxx/**/
            |\t}
            |}
            |'''.stripMargin()
        ),

        testTupleDeclaration:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef (xxx/**/, _) = [0, 0]
            |\t}
            |}
            |'''.stripMargin()
        ),

        testRename:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t\txxx
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def renamed
            |\tdef test() {
            |\t\trenamed/**/
            |\t\trenamed
            |\t}
            |}
            |'''.stripMargin(),
            'renamed'
        ),

        testInitialization:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/ = 15
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx/**/ = 15
            |\t}
            |}
            |'''.stripMargin()
        ),

        testVariableConflict:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef variableConflict
            |\t\tdef xxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def variableConflict
            |\tdef test() {
            |\t\tdef variableConflict
            |\t\tvariableConflict/**/
            |\t}
            |}
            |'''.stripMargin(),
            'variableConflict',
            true
        ),

        testFieldConflict:
        new TestCase(
            '''\
            |class Test {
            |\tdef fieldConflict
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            null,
            'fieldConflict'
        ),

        testFieldReference:
        new TestCase(
            '''\
            |class Test {
            |\tdef xxx
            |\tdef test() {
            |\t\txxx/**/
            |\t}
            |}
            |'''.stripMargin()
        ),

        testException:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\ttry {
            |\t\t} catch(Exception xxx/**/) {
            |\t\t}
            |\t}
            |}
            |'''.stripMargin()
        ),

        testForLoop:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tfor(int xxx/**/ = 0; xxx < 10; xxx++);
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate int renamed
            |\tdef test() {
            |\t\tfor(renamed/**/ = 0; renamed < 10; renamed++);
            |\t}
            |}
            |'''.stripMargin(),
            'renamed'
        ),

        testPostfix:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx = 0
            |\t\txxx/**/++
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def renamed
            |\tdef test() {
            |\t\trenamed = 0
            |\t\trenamed/**/++
            |\t}
            |}
            |'''.stripMargin(),
            'renamed'
        ),

        testPrefix:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx = false
            |\t\t!xxx/**/
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx = false
            |\t\t!xxx/**/
            |\t}
            |}
            |'''.stripMargin()
        ),

        testMethodInvocation:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx = new Object()
            |\t\txxx/**/.toString()
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx = new Object()
            |\t\txxx/**/.toString()
            |\t}
            |}
            |'''.stripMargin()
        ),

        testParameterList:
        new TestCase(
            '''\
            |class Test {
            |\tdef test(xxx/**/) {
            |\t}
            |}
            |'''.stripMargin()
        ),

        testArgumentList:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx = "Hello, World!"
            |\t\tprintln(xxx/**/)
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx = "Hello, World!"
            |\t\tprintln(xxx/**/)
            |\t}
            |}
            |'''.stripMargin()
        ),

        testInnerClass:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tnew Object() {
            |\t\t\tdef test() {
            |\t\t\t\tdef xxx/**/
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tdef test() {
            |\t\tnew Object() {
            |\t\t\tprivate def xxx
            |\t\t\tdef test() {
            |\t\t\t\txxx/**/
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin()
        ),

        testInnerFieldConflict:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t\tnew Object() {
            |\t\t\tdef innerFieldConflict
            |\t\t\tdef test() {
            |\t\t\t\txxx
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def innerFieldConflict
            |\tdef test() {
            |\t\tinnerFieldConflict/**/
            |\t\tnew Object() {
            |\t\t\tdef innerFieldConflict
            |\t\t\tdef test() {
            |\t\t\t\tinnerFieldConflict
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            'innerFieldConflict',
            true
        ),

        testFakeField:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t\tnew Object() {
            |\t\t\tdef test() {
            |\t\t\t\txxx
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def xxx
            |\tdef test() {
            |\t\txxx/**/
            |\t\tnew Object() {
            |\t\t\tdef test() {
            |\t\t\t\txxx
            |\t\t\t}
            |\t\t}
            |\t}
            |}
            |'''.stripMargin()
        ),

        testClosure:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx
            |\t\tdef closure = {
            |\t\t\txxx/**/
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def renamed
            |\tdef test() {
            |\t\trenamed
            |\t\tdef closure = {
            |\t\t\trenamed/**/
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            'renamed'
        ),

        testClosureVariableConflict:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t\tdef closure = {
            |\t\t\tdef closureVariableConflict
            |\t\t\txxx
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def closureVariableConflict
            |\tdef test() {
            |\t\tclosureVariableConflict/**/
            |\t\tdef closure = {
            |\t\t\tdef closureVariableConflict
            |\t\t\tclosureVariableConflict
            |\t\t}
            |\t}
            |}
            |'''.stripMargin(),
            'closureVariableConflict',
            true
        ),

        testClosureParameterList:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef closure = { xxx/**/ -> xxx }
            |\t}
            |}
            |'''.stripMargin()
        ),
        testClosureImplicitIt:
        new TestCase(
            '''\
            |class Test {
            |\tdef test() {
            |\t\tdef xxx/**/
            |\t\tdef closure = { xxx }
            |\t}
            |}
            |'''.stripMargin(),
            '''\
            |class Test {
            |\tprivate def it
            |\tdef test() {
            |\t\tit/**/
            |\t\tdef closure = { it }
            |\t}
            |}
            |'''.stripMargin(),
            'it',
            true
        ),
    ]
}
