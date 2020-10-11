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
package org.codehaus.groovy.eclipse.test.actions

import static org.eclipse.jdt.core.JavaCore.*
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.test.ui.GroovyEditorTestSuite
import org.junit.Test

@CompileStatic
final class ConvertToPropertyActionTests extends GroovyEditorTestSuite {

    private static final String ACTION_ID = 'org.codehaus.groovy.eclipse.ui.convertToProperty'

    private void convertToProperty(CharSequence sourceCode) {
        makeEditor(sourceCode.toString())
        editor.getAction(ACTION_ID).run()
    }

    @Test
    void testGetterToProperty1() {
        convertToProperty "new Date().get${CARET}Hours();"
        assertEditorContents 'new Date().hours;'
    }

    @Test
    void testGetterToProperty2() {
        addGroovySource 'class Foo { def getURL() {} }', 'Foo'
        convertToProperty "new Foo().get${CARET}URL()"
        assertEditorContents 'new Foo().URL'
    }

    @Test
    void testGetterToProperty3() {
        addGroovySource 'class Foo { def getURLEncoder() {} }', 'Foo'
        convertToProperty "new Foo().get${CARET}URLEncoder()"
        assertEditorContents 'new Foo().URLEncoder'
    }

    @Test
    void testIsserToProperty1() {
        convertToProperty "[].is${CARET}Empty();"
        assertEditorContents '[].empty;'
    }

    @Test
    void testSetterToProperty1() {
        convertToProperty "new Date().set${CARET}Time(1234L);"
        assertEditorContents 'new Date().time = 1234L;'
    }

    @Test
    void testSetterToProperty2() {
        addGroovySource 'class Foo { void setURL(url) {} }', 'Foo'
        convertToProperty "new Foo().set${CARET}URL(null)"
        assertEditorContents 'new Foo().URL = null'
    }

    @Test
    void testSetterToProperty3() {
        addGroovySource 'class Foo { void setURLEncoder(encoder) {} }', 'Foo'
        convertToProperty "new Foo().set${CARET}URLEncoder(null)"
        assertEditorContents 'new Foo().URLEncoder = null'
    }

    @Test
    void testChainedGetterProperty() {
        convertToProperty "getClass().getResource('URL').g${CARET}etText()"
        assertEditorContents 'getClass().getResource(\'URL\').text'
    }

    @Test
    void testChainedIsserToProperty() {
        convertToProperty "Thread.currentThread().isInter${CARET}rupted()"
        assertEditorContents 'Thread.currentThread().interrupted'
    }

    @Test
    void testChainedSetterToProperty() {
        convertToProperty "Calendar.getInstance().setTime${CARET}Zone(null)"
        assertEditorContents 'Calendar.getInstance().timeZone = null'
    }

    @Test
    void testGStringGetterProperty() {
        convertToProperty "\"Time: \${new Date().get${CARET}Time()}\""
        assertEditorContents '"Time: ${new Date().time}"'
    }

    @Test
    void testGStringIsserProperty() {
        convertToProperty "def list = []; \"Empty?: \${list.is${CARET}Empty()}\""
        assertEditorContents 'def list = []; "Empty?: ${list.empty}"'
    }

    @Test
    void testImplicitGetterToProperty() {
        convertToProperty "new Date().with { get${CARET}Hours() }"
        assertEditorContents 'new Date().with { hours }'
    }

    @Test
    void testImplicitIsserToProperty() {
        addGroovySource 'class Foo { static void isSomething() {} }', 'Foo'
        convertToProperty "Foo.isSome${CARET}thing()"
        assertEditorContents 'Foo.something'
    }

    @Test
    void testImplicitSetterToProperty() {
        convertToProperty "new Date().with { set${CARET}Time(1234L) }"
        assertEditorContents 'new Date().with { time = 1234L }'
    }

    @Test
    void testStaticGetterToProperty() {
        convertToProperty "import java.lang.management.*; ManagementFactory.getRun${CARET}timeMXBean()"
        assertEditorContents 'import java.lang.management.*; ManagementFactory.runtimeMXBean'
    }

    @Test
    void testStaticIsserToProperty() {
        addGroovySource 'class Foo { static void isSomething() {} }', 'Foo'
        convertToProperty "Foo.isSome${CARET}thing();"
        assertEditorContents 'Foo.something;'
    }

    @Test
    void testStaticSetterToProperty() {
        convertToProperty "URL.setURL${CARET}StreamHandlerFactory(null)"
        assertEditorContents 'URL.URLStreamHandlerFactory = null'
    }

    @Test
    void testSpaceBeforeAssignmentPreference() {
        try {
            setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, DO_NOT_INSERT)

            convertToProperty "new Date().set${CARET}Time(1234L);"
            assertEditorContents 'new Date().time= 1234L;'
        } finally {
            setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, INSERT)
        }
    }

    @Test
    void testSpaceAfterAssignmentPreference() {
        try {
            setJavaPreference(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, DO_NOT_INSERT)

            convertToProperty "new Date().set${CARET}Time(1234L);"
            assertEditorContents 'new Date().time =1234L;'
        } finally {
            setJavaPreference(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, INSERT)
        }
    }

    @Test
    void testNoConversion0() {
        convertToProperty "new Da${CARET}te()"
        assertEditorContents 'new Date()'
    }

    @Test
    void testNoConversion1() {
        convertToProperty "[:].ge${CARET}t('key')"
        assertEditorContents '[:].get(\'key\')'
    }

    @Test
    void testNoConversion2() {
        convertToProperty "[:].ge${CARET}tAt('key')"
        assertEditorContents '[:].getAt(\'key\')'
    }

    @Test
    void testNoConversion3() {
        convertToProperty "[:].pu${CARET}t('key', 'val')"
        assertEditorContents '[:].put(\'key\', \'val\')'
    }

    @Test
    void testNoConversion4() {
        convertToProperty "System.get${CARET}Property('abc')"
        assertEditorContents 'System.getProperty(\'abc\')'
    }

    @Test
    void testNoConversion5() {
        convertToProperty "System.set${CARET}Property('abc', 'xyz')"
        assertEditorContents 'System.setProperty(\'abc\', \'xyz\')'
    }

    @Test
    void testNoConversion6() {
        convertToProperty "System.current${CARET}TimeMillis()"
        assertEditorContents 'System.currentTimeMillis()'
    }

    @Test
    void testNoConversion7() {
        convertToProperty "Calendar.getInstance().is${CARET}Set(Calendar.YEAR)"
        assertEditorContents 'Calendar.getInstance().isSet(Calendar.YEAR)'
    }

    @Test
    void testNoConversion8() {
        addGroovySource 'class Foo { void setSomething() {} }', 'Foo'
        convertToProperty "new Foo().set${CARET}Something()"
        assertEditorContents 'new Foo().setSomething()'
    }

    @Test
    void testNoConversion9() {
        addGroovySource 'class Foo { void setSomething(Object... args) {} }', 'Foo'
        convertToProperty "new Foo().set${CARET}Something()"
        assertEditorContents 'new Foo().setSomething()'
    }

    @Test
    void testNoConversion10() {
        convertToProperty "new Date().set${CARET}Time(time: 1234L)"
        assertEditorContents 'new Date().setTime(time: 1234L)'
    }

    // TODO: Convert "setX(p1: v1, p2: v2)" to "x = [p1: v1, p2: v2]"?
}
