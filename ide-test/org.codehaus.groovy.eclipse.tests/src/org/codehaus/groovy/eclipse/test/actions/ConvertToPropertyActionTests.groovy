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
package org.codehaus.groovy.eclipse.test.actions

import static org.eclipse.jdt.core.JavaCore.*
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*

import org.codehaus.groovy.eclipse.test.ui.GroovyEditorTest

final class ConvertToPropertyActionTests extends GroovyEditorTest {

    private static final String ACTION_ID = 'org.codehaus.groovy.eclipse.ui.convertToProperty'

    private void convertToProperty(CharSequence sourceCode) {
        makeEditor(sourceCode.toString())
        editor.getAction(ACTION_ID).run()
    }

    void testGetterToProperty() {
        convertToProperty "new Date().get${CARET}Hours();"
        assertEditorContents "new Date().hours;"
    }

    void testIsserToProperty() {
        convertToProperty "[].is${CARET}Empty();"
        assertEditorContents "[].empty;"
    }

    void testSetterToProperty() {
        convertToProperty "new Date().set${CARET}Time(1234L);"
        assertEditorContents "new Date().time = 1234L;"
    }

    void testChainedGetterProperty() {
        convertToProperty "getClass().getResource('URL').g${CARET}etText()"
        assertEditorContents "getClass().getResource('URL').text"
    }

    void testChainedIsserToProperty() {
        convertToProperty "Thread.currentThread().isInter${CARET}rupted()"
        assertEditorContents "Thread.currentThread().interrupted"
    }

    void testChainedSetterToProperty() {
        convertToProperty "Calendar.getInstance().setTime${CARET}Zone(null)"
        assertEditorContents "Calendar.getInstance().timeZone = null"
    }

    void testImplicitGetterToProperty() {
        convertToProperty "new Date().with {\n get${CARET}Hours()\n}"
        assertEditorContents "new Date().with {\n hours\n}"
    }

    void testImplicitIsserToProperty() {
        testProject.createGroovyTypeAndPackage '', 'Foo.groovy', 'class Foo { static void isSomething() {} }'
        convertToProperty "Foo.isSome${CARET}thing()"
        assertEditorContents "Foo.something"
    }

    void testImplicitSetterToProperty() {
        convertToProperty "new Date().with { set${CARET}Time(1234L) }"
        assertEditorContents "new Date().with { time = 1234L }"
    }

    void testStaticGetterToProperty() {
        convertToProperty "import java.lang.management.*; ManagementFactory.getRun${CARET}timeMXBean()"
        assertEditorContents "import java.lang.management.*; ManagementFactory.runtimeMXBean"
    }

    void testStaticIsserToProperty() {
        convertToProperty ""
        assertEditorContents ""
    }

    void testStaticSetterToProperty() {
        convertToProperty "URL.setURL${CARET}StreamHandlerFactory(null)"
        assertEditorContents "URL.uRLStreamHandlerFactory = null"
    }

    void testSpaceBeforeAssignmentPreference() {
        try {
            setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, DO_NOT_INSERT)

            convertToProperty "new Date().set${CARET}Time(1234L);"
            assertEditorContents "new Date().time= 1234L;"

        } finally {
            setJavaPreference(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR, INSERT)
        }
    }

    void testSpaceAfterAssignmentPreference() {
        try {
            setJavaPreference(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, DO_NOT_INSERT)

            convertToProperty "new Date().set${CARET}Time(1234L);"
            assertEditorContents "new Date().time =1234L;"

        } finally {
            setJavaPreference(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR, INSERT)
        }
    }

    void testNoConversion0() {
        convertToProperty "new Da${CARET}te()"
        assertEditorContents "new Date()"
    }

    void testNoConversion1() {
        convertToProperty "[:].ge${CARET}t('key')"
        assertEditorContents "[:].get('key')"
    }

    void testNoConversion2() {
        convertToProperty "[:].ge${CARET}tAt('key')"
        assertEditorContents "[:].getAt('key')"
    }

    void testNoConversion3() {
        convertToProperty "[:].pu${CARET}t('key', 'val')"
        assertEditorContents "[:].put('key', 'val')"
    }

    void testNoConversion4() {
        convertToProperty "System.get${CARET}Property('abc')"
        assertEditorContents "System.getProperty('abc')"
    }

    void testNoConversion5() {
        convertToProperty "System.set${CARET}Property('abc', 'xyz')"
        assertEditorContents "System.setProperty('abc', 'xyz')"
    }

    void testNoConversion6() {
        convertToProperty "System.current${CARET}TimeMillis()"
        assertEditorContents "System.currentTimeMillis()"
    }

    void testNoConversion7() {
        convertToProperty "Calendar.getInstance().is${CARET}Set(Calendar.YEAR)"
        assertEditorContents "Calendar.getInstance().isSet(Calendar.YEAR)"
    }

    void testNoConversion8() {
        testProject.createGroovyTypeAndPackage '', 'Foo.groovy', 'class Foo { void setSomething() {} }'
        convertToProperty "new Foo().set${CARET}Something()"
        assertEditorContents "new Foo().setSomething()"
    }

    void testNoConversion9() {
        testProject.createGroovyTypeAndPackage '', 'Foo.groovy', 'class Foo { void setSomething(Object... args) {} }'
        convertToProperty "new Foo().set${CARET}Something()"
        assertEditorContents "new Foo().setSomething()"
    }

    void testNoConversion10() {
        convertToProperty "new Date().set${CARET}Time(time: 1234L)"
        assertEditorContents "new Date().setTime(time: 1234L)"
    }

    // TODO: Convert "setX(p1: v1, p2: v2)" to "x = [p1: v1, p2: v2]"?
}
