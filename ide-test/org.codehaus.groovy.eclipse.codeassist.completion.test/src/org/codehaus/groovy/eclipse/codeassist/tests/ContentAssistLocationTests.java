/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * Andrew Eisenberg - initial API and implementation
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
package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.GroovyCompletionProposalComputer;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.Document;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jul 15, 2011
 */
public class ContentAssistLocationTests extends CompletionTestCase {
    public ContentAssistLocationTests(String name) {
        super(name);
    }
    
    public void testStatement1() throws Exception {
        assertLocation("", 0, ContentAssistLocation.SCRIPT);
    }
    public void testStatement2() throws Exception {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annotying
        assertLocation("a", 1, ContentAssistLocation.STATEMENT);
//        assertLocation("a", 1, ContentAssistLocation.SCRIPT);
    }
    
    public void testStatement3() throws Exception {
        String contents = "a.g()";
        assertLocation(contents, contents.indexOf(")")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement4() throws Exception {
        String contents = "def x = { a.g() }";
        assertLocation(contents, contents.indexOf(")")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement5() throws Exception {
        assertLocation("a\n", 2, ContentAssistLocation.SCRIPT);
    }

    public void testStatement6() throws Exception {
        // This is technically a bug, but I actually want this to be
        // the expected behaviour since having the extra completions
        // available from script can be annotying
        assertLocation("a\na", 3, ContentAssistLocation.STATEMENT);
//        assertLocation("a\na", 3, ContentAssistLocation.SCRIPT);
    }

    public void testStatement7() throws Exception {
        String contents = "def x = { }";
        assertLocation(contents, contents.indexOf("{")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement8() throws Exception {
        String contents = "def x() { }";
        assertLocation(contents, contents.indexOf("{")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement9() throws Exception {
        String contents = "class Blar { def x() { } }";
        assertLocation(contents, contents.indexOf("}"), ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement10() throws Exception {
        String contents = "class Blar { def x = { } }";
        assertLocation(contents, contents.indexOf("}"), ContentAssistLocation.STATEMENT);
    }
    public void testStatement11() throws Exception {
        String contents = "def x = { a.g(    c,b) }";
        assertLocation(contents, contents.indexOf("c")+1, ContentAssistLocation.STATEMENT);
    }

    public void testStatement12() throws Exception {
        String contents = "def x = { a.g a, b }";
        assertLocation(contents, contents.indexOf("b")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement13() throws Exception {
        String contents = "a.g a, b";
        assertLocation(contents, contents.indexOf("b")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement14() throws Exception {
        String contents = "a()";
        assertLocation(contents, contents.indexOf("a")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement15() throws Exception {
        String contents = "b a()";
        assertLocation(contents, contents.indexOf("a")+1, ContentAssistLocation.STATEMENT);
    }
    
    public void testStatement16() throws Exception {
        String contents = "new ArrayList(a,b)";
        assertLocation(contents, contents.indexOf(")"), ContentAssistLocation.STATEMENT);
    }

    public void testStatement17() throws Exception {
        String contents = "new ArrayList(a,b)";
        assertLocation(contents, contents.indexOf(")")+1, ContentAssistLocation.STATEMENT);
    }
    
    
    public void testExpression() throws Exception {
        String contents = "a.g a, a.b";
        assertLocation(contents, contents.indexOf("b")+1, ContentAssistLocation.EXPRESSION);
    }
    
    public void testMethodContext1() throws Exception {
        String contents = "a.g()";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext2() throws Exception {
        String contents = "def x = { a.g() }";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext3() throws Exception {
        String contents = "def x = { a.g(a,b) }";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext5() throws Exception {
        String contents = "def x = { a.g(    c,b) }";
        assertLocation(contents, contents.indexOf(",")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext6() throws Exception {
        String contents = "def x = { a.g(    c,\nb) }";
        assertLocation(contents, contents.indexOf(",")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext7() throws Exception {
        String contents = "a.g a, a.b";
        assertLocation(contents, contents.indexOf("g")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext8() throws Exception {
        String contents = "a.g a, a.b";
        assertLocation(contents, contents.indexOf(",")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext9() throws Exception {
        String contents = "a.g a, a.b";
        assertLocation(contents, contents.indexOf(",")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext10() throws Exception {
        String contents = "new ArrayList()";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    public void testMethodContext11() throws Exception {
        String contents = "new ArrayList(a)";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext12() throws Exception {
        String contents = "new ArrayList(a,b)";
        assertLocation(contents, contents.indexOf(",")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext13() throws Exception {
        String contents = "new ArrayList<String>()";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    public void testMethodContext14() throws Exception {
        String contents = "new ArrayList<String>(a)";
        assertLocation(contents, contents.indexOf("(")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext15() throws Exception {
        String contents = "new ArrayList<String>(a,b)";
        assertLocation(contents, contents.indexOf(",")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext16() throws Exception {
        String contents = "foo \nh";
        assertLocation(contents, contents.indexOf("foo ")+4, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext17() throws Exception {
        String contents = "foo a, \nh";
        assertLocation(contents, contents.indexOf(", ")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext18() throws Exception {
        String contents = "foo a, b \nh";
        assertLocation(contents, contents.indexOf("b ")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext19() throws Exception {
        String contents = "foo (a, b )\nh";
        assertLocation(contents, contents.indexOf("b ")+2, ContentAssistLocation.METHOD_CONTEXT);
    }
    
    public void testMethodContext20() throws Exception {
        String contents = "foo (a, )\nh";
        assertLocation(contents, contents.indexOf(", ")+1, ContentAssistLocation.METHOD_CONTEXT);
    }
    

    public void testExpression1() throws Exception {
        assertLocation("a.a", 3, ContentAssistLocation.EXPRESSION);
    }
    
    public void testExpression2() throws Exception {
        assertLocation("a.", 2, ContentAssistLocation.EXPRESSION);
    }
    
    public void testExpression3() throws Exception {
        assertLocation("a.\n", 3, ContentAssistLocation.EXPRESSION);
    }
    
    public void testExpression4() throws Exception {
        String contents = "a.// \n";
        assertLocation(contents, contents.length(), ContentAssistLocation.EXPRESSION);
    }
    
    public void testExpression5() throws Exception {
        String contents = "a.g(b.)// \n";
        assertLocation(contents, contents.indexOf("b.")+2, ContentAssistLocation.EXPRESSION);
    }
    
    public void testExpression6() throws Exception {
        String contents = "def x = { a.g(    z.c,\nb) }";
        assertLocation(contents, contents.indexOf("c")+1, ContentAssistLocation.EXPRESSION);
    }

    public void testExpression7() throws Exception {
        String contents = "def x = { a.g(    c,\nz.b) }";
        assertLocation(contents, contents.indexOf("b")+1, ContentAssistLocation.EXPRESSION);
    }
    
    
    // not working because parser is broken
    public void _testImport1() throws Exception {
        String contents = "import ";
        int loc = contents.indexOf("import ") +"import ".length();
        assertLocation(contents, loc, ContentAssistLocation.IMPORT);
    }
    
    public void testImport2() throws Exception {
        String contents = "import T";
        int loc = contents.indexOf("import T") +"import T".length();
        assertLocation(contents, loc, ContentAssistLocation.IMPORT);
    }
    
    // not working because parser is broken
    public void _testImport3() throws Exception {
        String contents = "package ";
        int loc = contents.indexOf("package ") +"package ".length();
        assertLocation(contents, loc, ContentAssistLocation.IMPORT);
    }
    
    public void testImport4() throws Exception {
        String contents = "package T";
        int loc = contents.indexOf("package T") +"package T".length();
        assertLocation(contents, loc, ContentAssistLocation.PACKAGE);
    }
    
    
    public void testClassBody1() throws Exception {
        String contents = "class A { }";
        int loc = contents.indexOf("{") +1;
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY);
    }
    
    public void testClassBody2() throws Exception {
        String contents = "class A { t }";
        int loc = contents.indexOf("t") +1;
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY);
    }
    
    public void testClassBody3() throws Exception {
        String contents = "class A { void t }";
        int loc = contents.indexOf("t") +1;
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY);
    }
    
    public void testClassBody4() throws Exception {
        String contents = "class A extends T { void t }";
        int loc = contents.indexOf("ds") +4;
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS);
    }
    
    public void testClassBody5() throws Exception {
        String contents = "class A extends ArrayList { void t }";
        int loc = contents.indexOf("Arr") +3;
        assertLocation(contents, loc, ContentAssistLocation.EXTENDS);
    }
    
    public void testClassBody6() throws Exception {
        String contents = "class A extends ArrayList implements T { void t }";
        int loc = contents.indexOf("ents ") +6;
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS);
    }
    
    public void testClassBody7() throws Exception {
        String contents = "class A extends ArrayList implements Li { void t }";
        int loc = contents.indexOf(" Li") +3;
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS);
    }
    
    public void testClassBody8() throws Exception {
        String contents = "class A extends ArrayList implements Foo, Li { void t }";
        int loc = contents.indexOf(" Li") +3;
        assertLocation(contents, loc, ContentAssistLocation.IMPLEMENTS);
    }
    
    public void testClassBody9() throws Exception {
        String contents = "class A { void t \n }";
        int loc = contents.indexOf("\n") +1;
        assertLocation(contents, loc, ContentAssistLocation.CLASS_BODY);
    }
    
    public void testParameters1() throws Exception {
        String contents = "class A { void t() {} }";
        int loc = contents.indexOf("(") +1;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    public void testParameters2() throws Exception {
        String contents = "class A { void t(v) {} }";
        int loc = contents.indexOf("(v") +2;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    // this one should not propose anything
    public void testParameters3() throws Exception {
        String contents = "class A { void t(v y) {} }";
        int loc = contents.indexOf("(v y") +4;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    public void testParameters4() throws Exception {
        String contents = "class A { void t(v y = hh) {} }";
        int loc = contents.indexOf("=") +1;
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT);
    }
    
    public void testParameters5() throws Exception {
        String contents = "class A { void t(v y = hh) {} }";
        int loc = contents.indexOf("hh") +1;
        assertLocation(contents, loc, ContentAssistLocation.STATEMENT);
    }
    
    public void testParameters6() throws Exception {
        String contents = "class A { def t = {v -> hh } }";
        int loc = contents.indexOf("v") +1;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    public void testParameters7() throws Exception {
        String contents = "class A { def t = {v y -> hh } }";
        int loc = contents.indexOf("v") +1;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    public void testParameters8() throws Exception {
        String contents = "class A { def t = {v y -> hh } }";
        int loc = contents.indexOf("y") +1;
        assertLocation(contents, loc, ContentAssistLocation.PARAMETER);
    }
    
    public void testExceptions1() throws Exception {
        String contents = "class A { void t(v y = hh) throws Ex {} }";
        int loc = contents.indexOf("Ex") +1;
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS);
    }
    
    public void testExceptions2() throws Exception {
        String contents = "class A { void t(v y = hh) throws T {} }";
        int loc = contents.indexOf("ws ") +4;
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS);
    }
    
    public void testExceptions3() throws Exception {
        String contents = "class A { void t(v y = hh) throws Ex, T {} }";
        int loc = contents.indexOf(", ") +3;
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS);
    }
    
    public void testExceptions4() throws Exception {
        String contents = "class A { void t(v y = hh) throws Ex, Th {} }";
        int loc = contents.indexOf("Th") +2;
        assertLocation(contents, loc, ContentAssistLocation.EXCEPTIONS);
    }
    
    
    
    void assertLocation(String contents, int offset, ContentAssistLocation location) throws Exception {
        ICompilationUnit unit = create(contents);
        
        GroovyCompletionProposalComputer computer = new GroovyCompletionProposalComputer();
        ContentAssistContext context = computer.createContentAssistContext((GroovyCompilationUnit) unit, offset, new Document(unit.getBuffer().getContents()));
        
        assertEquals("Invalid location at index " + offset + " in text:\n" + contents, location, context.location);
    }
}
