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
package org.codehaus.groovy.eclipse.test.ui

import static org.eclipse.jdt.internal.ui.JavaPlugin.getDefault as getJavaPlugin
import static org.eclipse.jdt.ui.PreferenceConstants.*

import org.junit.Before
import org.junit.Test

final class SmartInsertTests extends GroovyEditorTestSuite {

    @Before
    void setUp() {
        javaPlugin.preferenceStore.setValue(EDITOR_SMART_OPENING_BRACE, true)
        javaPlugin.preferenceStore.setValue(EDITOR_SMART_SEMICOLON, true)
    }

    @Test
    void testInsertCurlyBrace1() {
        makeEditor("new Object(${CARET})")

        send('{')

        assertEditorContents("new Object() {${CARET}")
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/1093
    void testInsertCurlyBrace2() {
        addGroovySource '''\
            |class C {
            |  C(a, b, Closure c) {
            |  }
            |}
            |'''.stripMargin()

        makeEditor("new C(1, 2, ${CARET})")

        send('{')

        assertEditorContents("new C(1, 2, ) {${CARET}") // TODO
    }

    //

    @Test
    void testInsertSemicolon1() {
        makeEditor("new Object(${CARET})")

        send(';')

        assertEditorContents("new Object();${CARET}")
    }
}
