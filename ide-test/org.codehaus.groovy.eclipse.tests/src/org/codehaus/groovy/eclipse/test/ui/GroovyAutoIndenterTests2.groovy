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

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.*
import static org.eclipse.jdt.ui.PreferenceConstants.*

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jface.preference.IPreferenceStore
import org.junit.Before
import org.junit.Test

final class GroovyAutoIndenterTests2 extends GroovyEditorTestSuite {

    @Before
    void setUp() {
        setJavaPreference(EDITOR_CLOSE_BRACES, IPreferenceStore.TRUE)
        setJavaPreference(FORMATTER_TAB_CHAR, JavaCore.SPACE)
        setJavaPreference(FORMATTER_TAB_SIZE, '4')
    }

    @Test // GRECLIPSE-786
    void testGSPAutoIndenting() {
        String text = '''\
            |<html>
            |  <head>
            |    <title>Grails Runtime Exception</title>
            |    <style type="text/css">
            |      .message {
            |        border: 1px solid black;
            |        padding: 5px;
            |        background-color:#E9E9E9;
            |      .stack {
            |        border: 1px solid black;
            |        padding: 5px;
            |        overflow:auto;
            |        height: 300px;
            |      }
            |      .snippet {
            |        padding: 5px;
            |        background-color:white;
            |        border:1px solid black;
            |        margin:3px;
            |        font-family:courier;
            |      }
            |    </style>
            |  </head>
            |  <%
            |    if (a < b>) {<***>
            |  %>
            |  <body>
            |    <h1>Grails Runtime Exception</h1>
            |    <h2>Error Details</h2>
            |      <div class="message">
            |        <strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
            |        <strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
            |        <strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
            |        <g:if test="${exception}">
            |          <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
            |          <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
            |          <strong>Class:</strong> ${exception.className} <br />
            |          <strong>At Line:</strong> [${exception.lineNumber}] <br />
            |          <strong>Code Snippet:</strong><br />
            |          <div class="snippet">
            |            <g:each var="cs" in="${exception.codeSnippet}">
            |              ${cs?.encodeAsHTML()}<br />
            |            </g:each>
            |          </div>
            |        </g:if>
            |      </div>
            |    <g:if test="${exception}">
            |      <h2>Stack Trace</h2>
            |      <div class="stack">
            |        <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
            |      </div>
            |    </g:if>
            |  </body>
            |</html>
            |'''.stripMargin()

        makeEditor(text)

        send('\n')

        assertEditorContents(text.replace('''\
            |  <%
            |    if (a < b>) {<***>
            |  %>'''.stripMargin(), '''\
            |  <%
            |    if (a < b>) {
            |        <***>
            |    }
            |  %>'''.stripMargin()))
    }

    @Test // GRECLIPSE-771
    void testIndentAfterMultilineString() {
        makeEditor('''\
            |    static foo = """
            |Some perfectly
            |formatted text"""<***>'''.stripMargin())

        send('\n')

        assertEditorContents('''\
            |    static foo = """
            |Some perfectly
            |formatted text"""
            |    <***>'''.stripMargin())
    }

    /**
     * When MLS contain escaped code, they actually result in multiple tokens.
     * This case is handled differently (much like the begin and end token of
     * the MLS are like opening and closing braces. So test this separately.
     */
    @Test
    void testIndentAfterMultilineStringWithTokens() {
        makeEditor('''\
            |    static letter = """
            |Dear ${name},
            |How are you?"""<***>'''.stripMargin())

        send('\n')

        assertEditorContents('''\
            |    static letter = """
            |Dear ${name},
            |How are you?"""
            |    <***>'''.stripMargin())
    }

    @Test // GRECLIPSE-744
    void testIndentAfterBuilderProperty() {
        makeEditor("""\
            |class Foo {
            |    def show() {
            |        swing.actions() {
            |            echoAction= swing.action(name: 'Echo back',
            |                                     enabled: bind(source: model, sourceProperty: 'loggedin'),
            |                                     closure: { controller.echoBack = it.source.selected })${CARET}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Foo {
            |    def show() {
            |        swing.actions() {
            |            echoAction= swing.action(name: 'Echo back',
            |                                     enabled: bind(source: model, sourceProperty: 'loggedin'),
            |                                     closure: { controller.echoBack = it.source.selected })
            |            ${CARET}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-744
    void testIndentAfterMultilineMapLiteral() {
        makeEditor('''\
            |class Foo {
            |    def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
            |        "initialStyle": new File(design.obtainCSSFilePath()).text,
            |        "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").text,
            |        "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
            |        "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]<***>
            |'''.stripMargin())

        send('\n')

        assertEditorContents('''\
            |class Foo {
            |    def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
            |        "initialStyle": new File(design.obtainCSSFilePath()).text,
            |        "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").text,
            |        "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
            |        "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]
            |    <***>
            |'''.stripMargin())
    }

    @Test // GRECLIPSE-744
    void testIndentAfterStaticProperty() {
        makeEditor('''\
            |class Bagaga {
            |    static final String RESULTS = "results"<***>
            |}
            |'''.stripMargin())

        send('\n\n\n')

        assertEditorContents('''\
            |class Bagaga {
            |    static final String RESULTS = "results"
            |    |
            |    |
            |    <***>
            |}
            |'''.stripMargin().replaceAll('\\|', ''))
    }

    @Test
    void testIndentAfterIfStatement1() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b)${CARET}
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b)
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterIfStatement2() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            ${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterIfStatement3() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        } else if (a < b)${CARET}
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        } else if (a < b)
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterIfStatement3a() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        }
            |        else if (a < b)${CARET}
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        }
            |        else if (a < b)
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterElseStatement1() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b)
            |            return a
            |        else${CARET}
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b)
            |            return a
            |        else
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterElseStatement2() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        } else {${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        } else {
            |            ${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testIndentAfterElseStatement3() {
        // count of braces on line is slightly different in this case
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        }
            |        else {${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a == b) {
            |            return a
            |        }
            |        else {
            |            ${CARET}
            |        }
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testGRE757() {
        makeEditor("""\
            |class Bagaga {
            |    def foo(def a, def b) {${CARET}
            |    }
            |}
            |""".stripMargin())

        send('\nif (a < b)\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a < b)
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())

        send('foo()\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a < b)
            |            foo()
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())

        sendBackTab()

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a < b)
            |            foo()
            |        ${CARET}
            |    }
            |}
            |""".stripMargin())

        send('else\n')

        assertEditorContents("""\
            |class Bagaga {
            |    def foo(def a, def b) {
            |        if (a < b)
            |            foo()
            |        else
            |            ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testGRE620() {
        makeEditor("""\
            |class Bagaga {
            |${CARET}
            |}
            |""".stripMargin())

        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')

        assertEditorContents("""\
            |class Bagaga {
            |    static final String RESULTS = "results"
            |    static final String RESULTS = "results"
            |    static final String RESULTS = "results"
            |    ${CARET}
            |}
            |""".stripMargin())
    }

    @Test
    void testGRE295() {
        makeEditor("""\
            |class BracketBug {
            |    String name
            |    static constratins = {
            |        date(validator: {${CARET})
            |    }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |class BracketBug {
            |    String name
            |    static constratins = {
            |        date(validator: {
            |            ${CARET})
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testGRE761() {
        makeEditor("""\
            |def dodo()
            |{
            |    def x "abx"
            |    x.each${CARET}{
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def dodo()
            |{
            |    def x "abx"
            |    x.each
            |    ${CARET}{
            |""".stripMargin())
    }

    @Test
    void testEnterPressedAtEndOfFile() {
        makeEditor("""\
            |def dodo()
            |{
            |    def x "abx"
            |    x.each${CARET}""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def dodo()
            |{
            |    def x "abx"
            |    x.each
            |    ${CARET}""".stripMargin())
    }

    @Test
    void testEnterPressedInEmptyFile() {
        makeEditor("${CARET}")

        send('\n')

        assertEditorContents("\n${CARET}")
    }

    @Test
    void testEnterPressedAtBeginningOfFile() {
        makeEditor("$CARET\ndef foo() {\n}")

        send('\n')

        assertEditorContents("\n$CARET\ndef foo() {\n}")
    }

    @Test
    void testEnterPressedAfterLongCommentAtBeginningOfFile() {
        makeEditor("""\
            |/*
            | * A longer comment
            | * spanning several
            | * lines */${CARET}""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |/*
            | * A longer comment
            | * spanning several
            | * lines */
            |${CARET}""".stripMargin())
    }

    @Test
    void testEnterAfterHalfAComment() {
        makeEditor("""\
            |/*
            | * A longer comment
            | * got started${CARET}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |/*
            | * A longer comment
            | * got started
            | * ${CARET}
            |""".stripMargin())
    }

    @Test
    void testEnterInWhiteSpaceFile() {
        makeEditor("""\
            |
            |
            |
            |${CARET}""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |
            |
            |
            |
            |${CARET}""".stripMargin())
    }

    @Test
    void testEnterPressedInsideToken() {
        makeEditor("""\
            |def dodo() {
            |    def x = ab${CARET}cde
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def dodo() {
            |    def x = ab
            |    ${CARET}cde
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-763
    void testSmartPaste1() {
        makeEditor("""\
            |def doit() {
            |    ${CARET}
            |}
            |""".stripMargin())

        sendPaste("""\
            |    def x = 10
            |    def y = 20""".stripMargin())

        assertEditorContents("""\
            |def doit() {
            |    def x = 10
            |    def y = 20${CARET}
            |}
            |""".stripMargin())
    }

    @Test
    void testSmartPaste2() {
        makeEditor("""\
            |def doit() {
            |${CARET}
            |}
            |""".stripMargin())

        sendPaste("""\
            |def foo(int x) {
            |  if (x>0)
            |    println "pos"
            |  else
            |    println "neg"
            |}
            |""".stripMargin())

        assertEditorContents("""\
            |def doit() {
            |    def foo(int x) {
            |        if (x>0)
            |          println "pos"
            |        else
            |          println "neg"
            |      }
            |      ${CARET}
            |}
            |""".stripMargin())
    }

    @Test
    void testSmartPasteWrongFirstLine() {
        makeEditor("""\
            |def doit() {
            |${CARET}
            |}
            |""".stripMargin())

        sendPaste("""\
            |def foo(int x) {
            |     if (x>0)
            |         println "pos"
            |     else
            |         println "neg"
            |}
            |""".stripMargin())

        assertEditorContents("""\
            |def doit() {
            |    def foo(int x) {
            |        if (x>0)
            |            println "pos"
            |        else
            |            println "neg"
            |   }
            |   ${CARET}
            |}
            |""".stripMargin())
        // TODO: Indent is only 3 on caret line and previous; is this a bug?
    }

    @Test // GRECLIPSE-767
    void testSmartTab() {
        makeEditor("""\
            |package com.kameleoon.pixel
            |final class InlineTests extends BaseTest {
            |    public Map setupInlineTest() {
            |        def inlineDivDecoration = createDecoration()
            |        inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
            |${CARET}        inlineDivDecoration.tagName = HTMLElement.DIV
            |    }
            |}
            |""".stripMargin())

        send('\t')

        assertEditorContents("""\
            |package com.kameleoon.pixel
            |final class InlineTests extends BaseTest {
            |    public Map setupInlineTest() {
            |        def inlineDivDecoration = createDecoration()
            |        inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
            |        ${CARET}inlineDivDecoration.tagName = HTMLElement.DIV
            |    }
            |}
            |""".stripMargin())
    }

    @Test
    void testSmartTabMiddleOfWhiteSpace() {
        makeEditor("""\
            |public class Blah {
            |    def foo() {
            |   ${CARET}       blah()
            |""".stripMargin())

        send('\t')

        assertEditorContents("""\
            |public class Blah {
            |    def foo() {
            |        ${CARET}blah()
            |""".stripMargin())
    }

    @Test
    void testSmartTabEndOfWhiteSpace() {
        makeEditor("""\
            |public class Blah {
            |    def foo() {
            |          ${CARET}blah()
            |""".stripMargin())

        send('\t')

        assertEditorContents("""\
            |public class Blah {
            |    def foo() {
            |              ${CARET}blah()
            |""".stripMargin())
    }

    @Test
    void testSmartTabOnCloseBrace() {
        makeEditor("""\
            |public class Blah {
            |    def foo() {
            |  ${CARET}                 }
            |""".stripMargin())

        send('\t')

        assertEditorContents("""\
            |public class Blah {
            |    def foo() {
            |    ${CARET}}
            |""".stripMargin())
    }

    @Test
    void testSmartTabIncompleteMultiLineString() {
        makeEditor("""\
            |class Foo {
            |  def bar() {
            |    String baz = '''\\
            |${CARET}
            |  }
            |}
            |""".stripMargin())

        send('\t')

        assertEditorContents("""\
            |class Foo {
            |  def bar() {
            |    String baz = '''\\
            |    ${CARET}
            |  }
            |}
            |""".stripMargin())
    }

    @Test
    void testAutoCloseBracesInGString1() {
        makeEditor('''\
            |public class Blah {
            |    void echo(msg) {
            |        println "Echoing: <***>"
            |    }
            |}
            |'''.stripMargin())

        send('${')

        assertEditorContents('''\
            |public class Blah {
            |    void echo(msg) {
            |        println "Echoing: ${<***>}"
            |    }
            |}
            |'''.stripMargin())
    }

    @Test
    void testAutoCloseBracesInGString2() {
        makeEditor('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: <***>}"
            |\t}
            |}
            |'''.stripMargin())

        send('${')

        assertEditorContents('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: ${<***>}"
            |\t}
            |}
            |'''.stripMargin())
    }

    @Test
    void testAutoCloseBracesInGString3() {
        makeEditor('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: <***>boohoo}"
            |\t}
            |}
            |'''.stripMargin())

        send('${')

        assertEditorContents('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: ${<***>boohoo}"
            |\t}
            |}
            |'''.stripMargin())
    }

    @Test
    void testAutoCloseBracesInGString4() {
        makeEditor('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: <***>"
            |\t}
            |}
            |'''.stripMargin())

        send('{')

        assertEditorContents('''\
            |public class Blah {
            |\tvoid echo(msg) {
            |\t\tprintln "Echoing: {"
            |\t}
            |}
            |'''.stripMargin())
    }

    @Test
    void testAutoCloseBracesInMultilineGString() {
        makeEditor('''\
            |public class Blah {
            |    void echo(msg) {
            |        println """Echoing:
            |        <***>
            |        """
            |    }
            |}
            |'''.stripMargin())

        send('${')

        assertEditorContents('''\
            |public class Blah {
            |    void echo(msg) {
            |        println """Echoing:
            |        ${<***>}
            |        """
            |    }
            |}
            |'''.stripMargin())
    }

    @Test // GRECLIPSE-1262
    void testAutoCloseAfterClosureArgs1() {
        makeEditor("""\
            |def x = { yyy ->${CARET}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def x = { yyy ->
            |    ${CARET}
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-1262
    void testAutoCloseAfterClosureArgs2() {
        makeEditor("""\
            |def xxx() {
            |    def x = { yyy ->${CARET}
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def xxx() {
            |    def x = { yyy ->
            |        ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-1475
    void testAutoIndentCurly1() {
        makeEditor("""\
            |def xxx() {
            |    def x = { yyy ->${CARET}}
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def xxx() {
            |    def x = { yyy ->
            |        ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-1475
    void testAutoIndentCurly2() {
        makeEditor("""\
            |def xxx() {
            |    def x = { yyy ->${CARET}  }
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def xxx() {
            |    def x = { yyy ->
            |        ${CARET}
            |    }
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-1475
    void testAutoIndentCurly3() {
        makeEditor("""\
            |def xxx() {
            |    def x = { yyy ->${CARET}  } def foo
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def xxx() {
            |    def x = { yyy ->
            |        ${CARET}
            |    } def foo
            |}
            |""".stripMargin())
    }

    @Test // GRECLIPSE-1475
    void testAutoIndentCurly4() {
        makeEditor("""\
            |def xxx() {
            |    def x = { yyy ->${CARET}  )
            |}
            |""".stripMargin())

        send('\n')

        assertEditorContents("""\
            |def xxx() {
            |    def x = { yyy ->
            |        ${CARET}  )
            |}
            |""".stripMargin())
    }

    @Test
    void testAutoIndentCurly5() {
        setJavaPreference(EDITOR_CLOSE_BRACES, IPreferenceStore.FALSE)

        makeEditor("""\
            |if (something)${CARET}
            |""".stripMargin())

        send('\n{\n}')

        assertEditorContents("""\
            |if (something)
            |{
            |}${CARET}
            |""".stripMargin())
    }

    @Test
    void testAutoIndentCurly6() {
        makeEditor("""\
            |if (something)${CARET}
            |""".stripMargin())

        send('\n{\n')

        assertEditorContents("""\
            |if (something)
            |{
            |    ${CARET}
            |}
            |""".stripMargin())
    }

    @Test
    void testMuliLineCommentPaste1() {
        makeEditor("""\
            |
            |${CARET}
            |""".stripMargin())

        sendPaste('''\
            |
            |/*
            | * A longer comment
            | * spanning several
            | * lines */'''.stripMargin())

        assertEditorContents("""\
            |
            |
            |/*
            | * A longer comment
            | * spanning several
            | * lines */${CARET}
            |""".stripMargin())
    }

    @Test
    void testMuliLineCommentPaste2() {
        makeEditor("""\
            |if (0){
            |    ${CARET}
            |}""".stripMargin())

        sendPaste('''\
            |/*
            | * comment
            | */'''.stripMargin())

        assertEditorContents("""\
            |if (0){
            |    /*
            |     * comment
            |     */${CARET}
            |}""".stripMargin())
    }

    @Test
    void testMuliLineCommentPaste3() {
        makeEditor("""\
            |if (0){
            |    ${CARET}
            |}""".stripMargin())

        sendPaste('''\
            |/*
            | * comment
            | */'''.stripMargin())

        assertEditorContents("""\
            |if (0){
            |    /*
            |     * comment
            |     */${CARET}
            |}""".stripMargin())
    }

    @Test
    void testMuliLineCommentPaste4() {
        makeEditor('')

        sendPaste('''\
            |/*
            | * comment
            | */'''.stripMargin())

        assertEditorContents("""\
            |/*
            | * comment
            | */${CARET}""".stripMargin())
    }

    @Test
    void testMuliLineCommentPaste5() {
        makeEditor("\n${CARET}")

        sendPaste('''\
            |
            |/*
            | * A longer comment
            | * spanning several
            | * lines
            | */'''.stripMargin())

        assertEditorContents("""\
            |
            |
            |/*
            | * A longer comment
            | * spanning several
            | * lines
            | */${CARET}""".stripMargin())
    }

    @Test
    void testMuliLineStringPaste1() {
        makeEditor("""\
            |if (0){
            |    <***>
            |}""".stripMargin())

        sendPaste('''\
            |"""This is a line.
            |Here is another one.
            |And one more line."""'''.stripMargin())

        assertEditorContents('''\
            |if (0){
            |    """This is a line.
            |Here is another one.
            |And one more line."""<***>
            |}'''.stripMargin())
    }

    @Test
    void testMuliLineStringPaste2() {
        makeEditor('''\
            |if (0){
            |    a = <***>
            |}'''.stripMargin())

        sendPaste('''\
            |"""This is a line.
            |Here is another one.
            |And one more line."""'''.stripMargin())

        assertEditorContents('''\
            |if (0){
            |    a = """This is a line.
            |Here is another one.
            |And one more line."""<***>
            |}'''.stripMargin())
    }

    @Test
    void testMuliLineStringPaste3() {
        makeEditor('''\
            |if (0){
            |    a = <***>
            |}'''.stripMargin())

        sendPaste('''\
            |"""This is a line.
            |        Here is another one.
            |        And one more line."""'''.stripMargin())

        assertEditorContents('''\
            |if (0){
            |    a = """This is a line.
            |        Here is another one.
            |        And one more line."""<***>
            |}'''.stripMargin())
    }

    @Test
    void testMuliLineStringPaste4() {
        makeEditor('''\
            |if (i ==0){
            |    <***>
            |}'''.stripMargin())

        sendPaste('''\
            |if (i == 0){
            |    a = """This is a line.
            |Here is another one.
            |And one more line."""
            |}'''.stripMargin())

        assertEditorContents('''\
            |if (i ==0){
            |    if (i == 0){
            |        a = """This is a line.
            |Here is another one.
            |And one more line."""
            |    }<***>
            |}'''.stripMargin())
    }
}
