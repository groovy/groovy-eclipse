/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants

/**
 * Some additional tests for the GroovyAutoIndenter. There is no particular
 * reason to put these tests here rather than in the GroovyAutoIndenter class.
 * <p>
 * The only real reason is that this class (GroovyAutoIndenter2) is a .groovy
 * class, and therefore we can write tests in Groovy syntax (and use """ quoted
 * strings for specifying test input.
 */
final class GroovyAutoIndenterTests2 extends GroovyEditorTest {

    @Override
    protected void setUp() {
        super.setUp()

        // tests are sensitive to tab/space settings so ensure they are set to predictable default values
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE)
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, '4')
    }

    // GRECLIPSE-786
    void testGSPAutoIndenting() {
        String text = '''\
            <html>
              <head>
                <title>Grails Runtime Exception</title>
                <style type="text/css">
                  .message {
                    border: 1px solid black;
                    padding: 5px;
                    background-color:#E9E9E9;
                  .stack {
                    border: 1px solid black;
                    padding: 5px;
                    overflow:auto;
                    height: 300px;
                  }
                  .snippet {
                    padding: 5px;
                    background-color:white;
                    border:1px solid black;
                    margin:3px;
                    font-family:courier;
                  }
                </style>
              </head>
              <%
                if (a < b>) {<***>
              %>
              <body>
                <h1>Grails Runtime Exception</h1>
                <h2>Error Details</h2>
                  <div class="message">
                    <strong>Error ${request.'javax.servlet.error.status_code'}:</strong> ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
                    <strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
                    <strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
                    <g:if test="${exception}">
                      <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br />
                      <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br />
                      <strong>Class:</strong> ${exception.className} <br />
                      <strong>At Line:</strong> [${exception.lineNumber}] <br />
                      <strong>Code Snippet:</strong><br />
                      <div class="snippet">
                        <g:each var="cs" in="${exception.codeSnippet}">
                          ${cs?.encodeAsHTML()}<br />
                        </g:each>
                      </div>
                    </g:if>
                  </div>
                <g:if test="${exception}">
                  <h2>Stack Trace</h2>
                  <div class="stack">
                    <pre><g:each in="${exception.stackTraceLines}">${it.encodeAsHTML()}<br/></g:each></pre>
                  </div>
                </g:if>
              </body>
            </html>
            '''.stripIndent()

        makeEditor(text)

        send('\n')

        assertEditorContents(text.replace('''\
            <%
              if (a < b>) {<***>
            %>'''.stripIndent(10), '''\
            <%
              if (a < b>) {
                  <***>
              }
            %>'''.stripIndent(10)))
    }

    // GRECLIPSE-771
    void testIndentAfterMultilineString() {
        makeEditor('''\
                static foo = """
            Some perfectly
            formatted text"""<***>'''.stripIndent())

        send('\n')

        assertEditorContents('''\
                static foo = """
            Some perfectly
            formatted text"""
                <***>'''.stripIndent())
    }

    /**
     * When MLS contain escaped code, they actually result in multiple tokens.
     * This case is handled differently (much like the begin and end token of
     * the MLS are like opening and closing braces. So test this separately.
     */
    void testIndentAfterMultilineStringWithTokens() {
        makeEditor('''\
                static letter = """
            Dear ${name},
            How are you?"""<***>'''.stripIndent())

        send('\n')

        assertEditorContents('''\
                static letter = """
            Dear ${name},
            How are you?"""
                <***>'''.stripIndent())
    }

    // GRECLIPSE-744
    void testIndentAfterBuilderProperty() {
        makeEditor('''\
            class Foo {
                def show() {
                    swing.actions() {
                        echoAction= swing.action(name: 'Echo back',
                                                 enabled: bind(source: model, sourceProperty: 'loggedin'),
                                                 closure: { controller.setEchoBack(it.source.selected) })<***>
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            class Foo {
                def show() {
                    swing.actions() {
                        echoAction= swing.action(name: 'Echo back',
                                                 enabled: bind(source: model, sourceProperty: 'loggedin'),
                                                 closure: { controller.setEchoBack(it.source.selected) })
                        <***>
            '''.stripIndent())
    }

    // GRECLIPSE-744
    void testIndentAfterMultilineMapLiteral() {
        makeEditor('''\
            class Foo {
                def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
                    "initialStyle": new File(design.obtainCSSFilePath()).getText(),
                    "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").getText(),
                    "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
                    "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]<***>
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            class Foo {
                def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
                    "initialStyle": new File(design.obtainCSSFilePath()).getText(),
                    "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").getText(),
                    "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
                    "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]
                <***>
            '''.stripIndent())
    }

    // GRECLIPSE-744
    void testIndentAfterStaticProperty() {
        makeEditor('''\
            class Bagaga {
                static final String RESULTS = "results"<***>
            }
            '''.stripIndent())

        send('\n\n\n')

        assertEditorContents('''\
            class Bagaga {
                static final String RESULTS = "results"
                |
                |
                <***>
            }
            '''.stripIndent().replaceAll('\\|', ''))
    }

    void testIndentAfterIfStatement1() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b)${CARET}
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b)
                        ${CARET}
                }
            }
            """.stripIndent())
    }

    void testIndentAfterIfStatement2() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {${CARET}
                    }
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        ${CARET}
                    }
                }
            }
            """.stripIndent())
    }

    void testIndentAfterIfStatement3() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    } else if (a < b)${CARET}
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    } else if (a < b)
                        ${CARET}
                }
            }
            """.stripIndent())
    }

    void testIndentAfterIfStatement3a() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    }
                    else if (a < b)${CARET}
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    }
                    else if (a < b)
                        ${CARET}
                }
            }
            """.stripIndent())
    }

    void testIndentAfterElseStatement1() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b)
                        return a
                    else${CARET}
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b)
                        return a
                    else
                        ${CARET}
                }
            }
            """.stripIndent())
    }

    void testIndentAfterElseStatement2() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    } else {${CARET}
                    }
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    } else {
                        ${CARET}
                    }
                }
            }
            """.stripIndent())
    }

    void testIndentAfterElseStatement3() {
        // count of braces on line is slightly different in this case
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    }
                    else {${CARET}
                    }
                }
            }
            """.stripIndent())

        send('\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a == b) {
                        return a
                    }
                    else {
                        ${CARET}
                    }
                }
            }
            """.stripIndent())
    }

    void testGRE757() {
        makeEditor("""\
            class Bagaga {
                def foo(def a, def b) {${CARET}
                }
            }
            """.stripIndent())

        send('\nif (a < b)\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a < b)
                        ${CARET}
                }
            }
            """.stripIndent())

        send('foo()\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a < b)
                        foo()
                        ${CARET}
                }
            }
            """.stripIndent())

        sendBackTab()

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a < b)
                        foo()
                    ${CARET}
                }
            }
            """.stripIndent())

        send('else\n')

        assertEditorContents("""\
            class Bagaga {
                def foo(def a, def b) {
                    if (a < b)
                        foo()
                    else
                        ${CARET}
                }
            }
            """.stripIndent())
    }

    void testGRE620() {
        makeEditor('''\
            class Bagaga {
            <***>
            }
            '''.stripIndent())

        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')

        assertEditorContents('''\
            class Bagaga {
                static final String RESULTS = "results"
                static final String RESULTS = "results"
                static final String RESULTS = "results"
                <***>
            }
            '''.stripIndent())
    }

    void testGRE295() {
        makeEditor('''\
            class BracketBug {
                String name
                static constratins = {
                    date(validator: {<***>)
                }
            }
            ''')

        send('\n')

        assertEditorContents('''\
            class BracketBug {
                String name
                static constratins = {
                    date(validator: {
                        <***>)
                }
            }
            ''')
    }

    void testGRE761() {
        makeEditor('''\
            def dodo()
            {
                def x "abx"
                x.each<***>{
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def dodo()
            {
                def x "abx"
                x.each
                <***>{
            '''.stripIndent())
    }

    void testEnterPressedAtEndOfFile() {
        makeEditor('''\
            def dodo()
            {
                def x "abx"
                x.each<***>'''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def dodo()
            {
                def x "abx"
                x.each
                <***>'''.stripIndent())
    }

    void testEnterPressedInEmptyFile() {
        makeEditor('')

        send('\n')

        assertEditorContents('\n<***>')
    }

    void testEnterPressedAtBeginningOfFile() {
        makeEditor('''<***>
            def foo() {
            }'''.stripIndent(12))

        send('\n')

        assertEditorContents('''
            <***>
            def foo() {
            }'''.stripIndent(12))
    }

    void testEnterPressedAfterLongCommentAtBeginningOfFile() {
        makeEditor('''\
            /*
             * A longer comment
             * spanning several
             * lines */<***>'''.stripIndent())

        send('\n')

        assertEditorContents('''\
            /*
             * A longer comment
             * spanning several
             * lines */
            <***>'''.stripIndent())
    }

    void testEnterAfterHalfAComment() {
        makeEditor('''\
            /*
             * A longer comment
             * got started<***>
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            /*
             * A longer comment
             * got started
             * <***>
            '''.stripIndent())
    }

    void testEnterInWhiteSpaceFile() {
        makeEditor('''



<***>''')

        send('\n')

        assertEditorContents('''




<***>''')
    }

    void testEnterPressedInsideToken() {
        makeEditor('''\
            def dodo() {
                def x = ab<***>cde
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def dodo() {
                def x = ab
                <***>cde
            }
            '''.stripIndent())
    }

    // GRECLIPSE-763
    void testSmartPaste1() {
        makeEditor('''\
            def doit() {
                <***>
            }
            '''.stripIndent())

        sendPaste('''\
            def x = 10
            def y = 20'''.stripIndent(8))

        assertEditorContents('''\
            def doit() {
                def x = 10
                def y = 20<***>
            }
            '''.stripIndent())
    }

    void testSmartPaste2() {
        makeEditor('''\
            def doit() {
            <***>
            }
            '''.stripIndent())

        sendPaste('''\
            def foo(int x) {
              if (x>0)
                println "pos"
              else
                println "neg"
            }
            '''.stripIndent())

        assertEditorContents('''\
            def doit() {
                def foo(int x) {
                    if (x>0)
                      println "pos"
                    else
                      println "neg"
                  }
                  <***>
            }
            '''.stripIndent())
    }

    void testSmartPasteWrongFirstLine() {
        makeEditor('''\
            def doit() {
            <***>
            }
            '''.stripIndent())

        sendPaste('''\
            def foo(int x) {
                 if (x>0)
                     println "pos"
                 else
                     println "neg"
            }
            '''.stripIndent())

        assertEditorContents('''\
            def doit() {
                def foo(int x) {
                    if (x>0)
                        println "pos"
                    else
                        println "neg"
               }
               <***>
            }
            '''.stripIndent())
        // Indent is only 3 on caret line and previous; is this a bug?
    }

    // GRECLIPSE-767
    void testSmartTab() {
        makeEditor('''\
            package com.kameleoon.pixel
            public class InlineTest extends BaseTest {
                public Map setupInlineTest() {
                    def inlineDivDecoration = createDecoration()
                    inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
            <***>        inlineDivDecoration.tagName = HTMLElement.DIV
                }
            }
            '''.stripIndent())

        send('\t')

        assertEditorContents('''\
            package com.kameleoon.pixel
            public class InlineTest extends BaseTest {
                public Map setupInlineTest() {
                    def inlineDivDecoration = createDecoration()
                    inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
                    <***>inlineDivDecoration.tagName = HTMLElement.DIV
                }
            }
            '''.stripIndent())
    }

    void testSmartTabMiddleOfWhiteSpace() {
        makeEditor('''\
            public class Blah {
                def foo() {
               <***>       blah()
            '''.stripIndent())

        send('\t')

        assertEditorContents('''\
            public class Blah {
                def foo() {
                    <***>blah()
            '''.stripIndent())
    }

    void testSmartTabEndOfWhiteSpace() {
        makeEditor('''\
            public class Blah {
                def foo() {
                      <***>blah()
            '''.stripIndent())

        send('\t')

        assertEditorContents('''\
            public class Blah {
                def foo() {
                          <***>blah()
            '''.stripIndent())
    }

    void testSmartTabOnCloseBrace() {
        makeEditor('''\
            public class Blah {
                def foo() {
              <***>                 }
            '''.stripIndent())

        send('\t')

        assertEditorContents('''\
            public class Blah {
                def foo() {
                <***>}
            '''.stripIndent())
    }

    void testAutoCloseBracesInGString1() {
        makeEditor('''\
            public class Blah {
                void echo(msg) {
                    println "Echoing: <***>"
                }
            }
            '''.stripIndent())

        send('${')

        assertEditorContents('''\
            public class Blah {
                void echo(msg) {
                    println "Echoing: ${<***>}"
                }
            }
            '''.stripIndent())
    }

    void testAutoCloseBracesInGString2() {
        makeEditor('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: <***>}"
            \t}
            }
            '''.stripIndent())

        send('${')

        assertEditorContents('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: ${<***>}"
            \t}
            }
            '''.stripIndent())
    }

    void testAutoCloseBracesInGString3() {
        makeEditor('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: <***>boohoo}"
            \t}
            }
            '''.stripIndent())

        send('${')

        assertEditorContents('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: ${<***>boohoo}"
            \t}
            }
            '''.stripIndent())
    }

    void testAutoCloseBracesInGString4() {
        makeEditor('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: <***>"
            \t}
            }
            '''.stripIndent())

        send('{')

        assertEditorContents('''\
            public class Blah {
            \tvoid echo(msg) {
            \t\tprintln "Echoing: {"
            \t}
            }
            '''.stripIndent())
    }

    void testAutoCloseBracesInMultilineGString() {
        makeEditor('''\
            public class Blah {
                void echo(msg) {
                    println """Echoing:
                    <***>
                    """
                }
            }
            '''.stripIndent())

        send('${')

        assertEditorContents('''\
            public class Blah {
                void echo(msg) {
                    println """Echoing:
                    ${<***>}
                    """
                }
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1262
    void testAutoCloseAfterClosureArgs1() {
        makeEditor('''\
            def x = { yyy -><***>
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def x = { yyy ->
                <***>
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1262
    void testAutoCloseAfterClosureArgs2() {
        makeEditor('''\
            def xxx() {
                def x = { yyy -><***>
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def xxx() {
                def x = { yyy ->
                    <***>
                }
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1475
    void testAutoIndentCurly1() {
        makeEditor('''\
            def xxx() {
                def x = { yyy -><***>}
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def xxx() {
                def x = { yyy ->
                    <***>
                }
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1475
    void testAutoIndentCurly2() {
        makeEditor('''\
            def xxx() {
                def x = { yyy -><***>  }
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def xxx() {
                def x = { yyy ->
                    <***>
                }
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1475
    void testAutoIndentCurly3() {
        makeEditor('''\
            def xxx() {
                def x = { yyy -><***>  } def foo
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def xxx() {
                def x = { yyy ->
                    <***>
                } def foo
            }
            '''.stripIndent())
    }

    // GRECLIPSE-1475
    void testAutoIndentCurly4() {
        makeEditor('''\
            def xxx() {
                def x = { yyy -><***>  )
            }
            '''.stripIndent())

        send('\n')

        assertEditorContents('''\
            def xxx() {
                def x = { yyy ->
                    <***>  )
            }
            '''.stripIndent())
    }

    void testMuliLineCommentPaste1() {
        makeEditor('''
<***>
''')
        sendPaste('''
/*
 * A longer comment
 * spanning several
 * lines */''')

        assertEditorContents('''

/*
 * A longer comment
 * spanning several
 * lines */<***>
''')
    }

    void testMuliLineCommentPaste2() {
        makeEditor('''if (0){
    <***>
}''')

        sendPaste('''/*
 * comment
 */''')

        assertEditorContents('''if (0){
    /*
     * comment
     */<***>
}''')
    }

    void testMuliLineCommentPaste3() {
        makeEditor('''if (0){
    <***>
}''')

        sendPaste('''/*
 * comment
 */''')

        assertEditorContents('''if (0){
    /*
     * comment
     */<***>
}''')
    }

    void testMuliLineCommentPaste4() {
        makeEditor('')

        sendPaste('''/*
 * comment
 */''')

        assertEditorContents('''/*
 * comment
 */<***>''')
    }

    void testMuliLineCommentPaste5() {
        makeEditor('\n<***>')

        sendPaste('''
/*
 * A longer comment
 * spanning several
 * lines
 */''')

        assertEditorContents('''

/*
 * A longer comment
 * spanning several
 * lines
 */<***>''')
    }

    void testMuliLineStringPaste1() {
        makeEditor('''\
            if (0){
                <***>
            }'''.stripIndent())

        sendPaste('''\
            """This is a line.
            Here is another one.
            And one more line."""'''.stripIndent())

        assertEditorContents('''\
            if (0){
                """This is a line.
            Here is another one.
            And one more line."""<***>
            }'''.stripIndent())
    }

    void testMuliLineStringPaste2() {
        makeEditor('''\
            if (0){
                a = <***>
            }'''.stripIndent())

        sendPaste('''\
            """This is a line.
            Here is another one.
            And one more line."""'''.stripIndent())

        assertEditorContents('''\
            if (0){
                a = """This is a line.
            Here is another one.
            And one more line."""<***>
            }'''.stripIndent())
    }

    void testMuliLineStringPaste3() {
        makeEditor('''\
            if (0){
                a = <***>
            }'''.stripIndent())

        sendPaste('''\
            """This is a line.
                    Here is another one.
                    And one more line."""'''.stripIndent())

        assertEditorContents('''\
            if (0){
                a = """This is a line.
                    Here is another one.
                    And one more line."""<***>
            }'''.stripIndent())
    }

    void testMuliLineStringPaste4() {
        makeEditor('''\
            if (i ==0){
                <***>
            }'''.stripIndent())

        sendPaste('''\
            if (i == 0){
                a = """This is a line.
            Here is another one.
            And one more line."""
            }'''.stripIndent())

        assertEditorContents('''\
            if (i ==0){
                if (i == 0){
                    a = """This is a line.
            Here is another one.
            And one more line."""
                }<***>
            }'''.stripIndent())
    }
}
