/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.test.ui;

import org.eclipse.core.resources.ProjectScope 
import org.eclipse.jdt.core.JavaCore 
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants 




/**
 * Some additional tests for the GroovyAutoIndenter. There is no particular
 * reason to put these tests here rather than in the GroovyAutoIndenter class.
 * <p>
 * The only real reason is that this class (GroovyAutoIndenter2) is a .groovy 
 * class, and therefore we can write tests in Groovy syntax (and use """ quoted
 * strings for specifying test input.
 * 
 * @author kdvolder
 * @created 2010-05-20
 */
public class GroovyAutoIndenterTests2 extends GroovyEditorTest {
    
    private Hashtable savedOptions;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        savedOptions = JavaCore.getOptions();
        
        //Our tests are sensitive to tab/space settings so ensure they are
        //set to predictable default values.
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
        setJavaPreference(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
        
        //Also ensure that project specific settings on the test project are turned off
        // (or they will override our test settings on the plugin instance scope level)
        ProjectScope projectPrefScope = new ProjectScope(testProject.getProject());
        projectPrefScope.getNode(JavaCore.PLUGIN_ID).clear();
    }
    
    @Override
    protected void tearDown() throws Exception {
        JavaCore.setOptions(savedOptions);
        super.tearDown();
    }
    
    protected void setJavaPreference(String name, String value) {
        Hashtable options = JavaCore.getOptions();
        options.put(name, value);
        JavaCore.setOptions(options);
    }

	void testGre786_GSP_Autoindenting() {
		String initText = '''
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
'''
		makeEditor(initText)
		send('\n')
		String resultText = initText.replace(
"""<%
    if (a < b>) {<***>
  %>""",
"""<%
    if (a < b>) {
        <***>
    }
  %>""")
 
		assertEditorContents resultText
	}

    void testGRE_771_indentAfterMultilineString() {
        makeEditor """
    static foo = '''
Some perfectly
formatted text'''<***>"""
        send("\n")
        assertEditorContents """
    static foo = '''
Some perfectly
formatted text'''
    <***>"""
    }
    
    /**
     * When MLS contain escaped code, they actually result in multiple tokens.
     * This case is handled differently (much like the beg and end token
     * of the MLS are like opening and closing braces. So we test this
     * case separately.
     */
    void testIndentAfterMultilineStringWithTokens() {
        makeEditor '''
    static letter = """
Dear ${name},
How are you?"""<***>'''
        send("\n")
        assertEditorContents '''
    static letter = """
Dear ${name},
How are you?"""
    <***>'''
    }
    
    void testGRE744_1() throws Exception {
        //First example from http://jira.codehaus.org/browse/GRECLIPSE-744
        makeEditor("""
class Foo {
    def show() {
        swing.actions() {
            echoAction= swing.action(name: 'Echo back',
                                     enabled: bind(source: model, sourceProperty: 'loggedin'),
                                     closure: { controller.setEchoBack(it.source.selected) })<***>
""");
        send("\n");
        assertEditorContents("""
class Foo {
    def show() {
        swing.actions() {
            echoAction= swing.action(name: 'Echo back',
                                     enabled: bind(source: model, sourceProperty: 'loggedin'),
                                     closure: { controller.setEchoBack(it.source.selected) })
            <***>
""")
    }
    
    void testGRE744_2() throws Exception {
        //First example from http://jira.codehaus.org/browse/GRECLIPSE-744
        makeEditor("""
class Foo {
    def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
        "initialStyle": new File(design.obtainCSSFilePath()).getText(),
        "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").getText(),
        "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
        "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]<***>
""");
        send("\n");
        assertEditorContents("""
class Foo {
    def model = ["view": g.render("template": "/editor/main", "model": ["currentLocale": currentLocale]),
        "initialStyle": new File(design.obtainCSSFilePath()).getText(),
        "generalStyle": new File("\${org.codehaus.groovy.grails.commons.ConfigurationHolder.config.commonCssPath}customization.css").getText(),
        "fonts": FontFamily.obtainFontsMap(), "notLogged": ! (session.MemberId as Boolean),
        "noLogout": ! member.loginRequired, "sessionId": session.id, "basicMode": design.basicMode ]
    <***>
""");
    }
    
    void testGRE744_3() throws Exception {
        //First example from http://jira.codehaus.org/browse/GRECLIPSE-744
        makeEditor("""
class Bagaga {

    static final String RESULTS = "results"<***>
}
""");
        send("\n\n\n");
        assertEditorContents("""
class Bagaga {

    static final String RESULTS = "results"
    
    
    <***>
}
""");
    }
    
    void testGRE757() {
        makeEditor("""
class Bagaga {

    def foo(def a, def b) {<***>
    }
}
""");
        send("\nif (a < b)\n")
        send("\t");
        assertEditorContents("""
class Bagaga {

    def foo(def a, def b) {
        if (a < b)
            <***>
    }
}
""")
        send("foo()\n")
        assertEditorContents("""
class Bagaga {

    def foo(def a, def b) {
        if (a < b)
            foo()
            <***>
    }
}
""")
        sendBackTab()
        assertEditorContents("""
class Bagaga {

    def foo(def a, def b) {
        if (a < b)
            foo()
        <***>
    }
}
""")
        send("else\n")
        assertEditorContents("""
class Bagaga {

    def foo(def a, def b) {
        if (a < b)
            foo()
        else
        <***>
    }
}
""" );
    }
    
    void testGRE620() {
        makeEditor("""
class Bagaga {
<***>
}
""")
        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')
        sendPaste('\tstatic final String RESULTS = "results"\n')
        
        assertEditorContents("""
class Bagaga {
    static final String RESULTS = "results"
    static final String RESULTS = "results"
    static final String RESULTS = "results"
    <***>
}
""")
    }
    
    void testGRE295() {
        makeEditor("""
class BracketBug {
    String name
    static constratins = {
        date(validator: {<***>)
    }
}
""")
        send("\n")
        
        assertEditorContents("""
class BracketBug {
    String name
    static constratins = {
        date(validator: {
            <***>)
    }
}
""")
    }
    
    void testGRE761() {
        makeEditor("""
def dodo()
{
    def x "abx"
    x.each<***>{
""")
        send("\n")
        
        assertEditorContents("""
def dodo()
{
    def x "abx"
    x.each
    <***>{
""")
    }
    
    void testEnterPressedAtEndOfFile() {
        makeEditor("""
def dodo()
{
    def x "abx"
    x.each<***>""")
        send("\n")
        
        assertEditorContents("""
def dodo()
{
    def x "abx"
    x.each
    <***>""")
    }
    
    void testEnterPressedInEmptyFile() {
        makeEditor("""""")
        send("\n")
        
        assertEditorContents("""
<***>""")
    }
    
    void testEnterPressedAtBeginningOfFile() {
        makeEditor("""<***>
def foo() {
}""")
        send("\n")
        
        assertEditorContents("""
<***>
def foo() {
}""")
    }
    
    void testEnterPressedAfterLongCommentAtBeginningOfFile() {
        makeEditor("""/*
 * A longer comment
 * spanning several
 * lines */<***>""")
        send("\n")
        
        assertEditorContents("""/*
 * A longer comment
 * spanning several
 * lines */
<***>""")
    }
    
    void testEnterAfterHalfAComment() {
        makeEditor("""/*
 * A longer comment
 * got started <***>
""")
        send("\n")
        
        assertEditorContents("""/*
 * A longer comment
 * got started 
 * <***>
""")
    }
    
    void testEnterInWhiteSpaceFile() {
        makeEditor """



<***>"""
        send("\n")
        
        assertEditorContents("""




<***>""")
    }
    
    void testEnterPressedInsideToken() {
        makeEditor("""
def dodo() {
    def x = ab<***>cde 
}
""")
        send("\n")
        
        assertEditorContents("""
def dodo() {
    def x = ab
    <***>cde 
}
""")
    }
    
    void testGRE763SmartPaste() {
        makeEditor("""
def doit() {
    <***>
}
""")
        sendPaste """    def x = 10
    def y = 20"""
        assertEditorContents """
def doit() {
    def x = 10
    def y = 20<***>
}
"""
    }
    
    void testSmartPaste() {
        makeEditor("""
def doit() {
<***>
}
""")
        sendPaste ("""
def foo(int x) {
   if (x>0)
       println "pos"
   else
       println "neg"
}
""")
        assertEditorContents """
def doit() {
    
    def foo(int x) {
       if (x>0)
           println "pos"
       else
           println "neg"
    }
    <***>
}
"""
    }
    
    void testSmartPasteWrongFirstLine() {
        makeEditor("""
def doit() {
<***>
}
""")
        sendPaste ("""def foo(int x) {
             if (x>0)
                 println "pos"
             else
                 println "neg"
         }
         """)
        assertEditorContents """
def doit() {
    def foo(int x) {
        if (x>0)
            println "pos"
        else
            println "neg"
    }
    <***>
}
"""
    }
    
    void test_GRE_767_smarttab() {
        makeEditor("""package com.kameleoon.pixel

public class InlineTest extends BaseTest
{
    public Map setupInlineTest()
    {
        def inlineDivDecoration = createDecoration()
        inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
<***>        inlineDivDecoration.tagName = HTMLElement.DIV
    }
}
""")
        send('\t')
        assertEditorContents """package com.kameleoon.pixel

public class InlineTest extends BaseTest
{
    public Map setupInlineTest()
    {
        def inlineDivDecoration = createDecoration()
        inlineDivDecoration.properties = ["cssId": "inlineDiv", "backgroundColor": java.lang.Integer.parseInt("55dad8", 16)]
        <***>inlineDivDecoration.tagName = HTMLElement.DIV
    }
}
"""
    }
    
    void testSmartTabMiddleOfWhiteSpace() {
        makeEditor("""
public class Blah {

    def foo() {
   <***>       blah()
""")
        send('\t')
        assertEditorContents """
public class Blah {

    def foo() {
        <***>blah()
"""
    }
    
    void testSmartTabEndOfWhiteSpace() {
        makeEditor("""
public class Blah {

    def foo() {
          <***>blah()
""")
        send('\t')
        assertEditorContents """
public class Blah {

    def foo() {
              <***>blah()
"""
    }
    
    void test_smartTabOnCloseBrace() {
        makeEditor("""
public class Blah {

    def foo() {
   <***>                 }
""")
        send('\t')
        assertEditorContents """
public class Blah {

    def foo() {
    <***>}
"""
    }
	void testAutoCloseBracesInString() {
        makeEditor("""
public class Blah {

    void echo(msg) {
        println "Echoing: <***>"
    }
}
""")
        send('${')
        assertEditorContents '''
public class Blah {

    void echo(msg) {
        println "Echoing: ${<***>}"
    }
}
'''
    }
	
	void testAutoCloseBracesInMultiString() {
        makeEditor('''
public class Blah {

    void echo(msg) {
        println """Echoing: 
        <***>
        """
    }
}
''')
        send('${')
        assertEditorContents '''
public class Blah {

    void echo(msg) {
        println """Echoing: 
        ${<***>}
        """
    }
}
'''
    }
    
    
	void testAutoCloseBracesInString2() {
		makeEditor("""
public class Blah {

	void echo(msg) {
		println "Echoing: <***>}"
	}
}
""")
		send('${')
		assertEditorContents '''
public class Blah {

	void echo(msg) {
		println "Echoing: ${<***>}"
	}
}
'''
	}

	void testAutoCloseBracesInString3() {
		makeEditor("""
public class Blah {

	void echo(msg) {
		println "Echoing: <***>boohoo}"
	}
}
""")
		send('${')
		assertEditorContents '''
public class Blah {

	void echo(msg) {
		println "Echoing: ${<***>boohoo}"
	}
}
'''
	}
	void testAutoCloseBracesInString4() {
		makeEditor("""
public class Blah {

	void echo(msg) {
		println "Echoing: <***>"
	}
}
""")
		send('{')
		assertEditorContents '''
public class Blah {

	void echo(msg) {
		println "Echoing: {"
	}
}
'''
	}
}