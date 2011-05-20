 /*
 * Copyright 2003-2009 the original author or authors.
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

import junit.framework.TestCase;

import org.codehaus.groovy.eclipse.editor.GroovyPartitionScanner;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;

/**
 * @author Andrew Eisenberg
 * @created Jul 22, 2009
 *
 * Tests for the GroovyTagScanner
 */
public class GroovyPartitionScannerTests extends TestCase {

    GroovyPartitionScanner scanner;

    @Override
    protected void setUp() throws Exception {
        System.out.println("------------------------------");
        System.out.println("Starting: " + getName());
        super.setUp();
        scanner = new GroovyPartitionScanner();
    }


    public void testSingleQuotes() throws Exception {
        tryString("''''''", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    public void testSingleQuotes2() throws Exception {
        tryString("'''\n'''", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    public void testSingleQuotes3() throws Exception {
        tryString("'''dsfasddsfds\n'''", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }

    public void testDoubleQuotes() throws Exception {
        tryString("\"\"\"\"\"\"", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }

    public void testDoubleQuotes2() throws Exception {
        tryString("\"\"\"\n\"\"\"", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    public void testDoubleQuotes3() throws Exception {
        tryString("\"\"\"dsafasdfasdds\n\"\"\"", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }

    public void testDollarSlash1() throws Exception {
        tryString("$/ /$", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    
    public void testDollarSlash2() throws Exception {
        tryString("$/\n/$", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    
    public void testDollarSlash3() throws Exception {
        tryString("$/fdafsdasda/ $fdsaafds\n/$", 0, GroovyPartitionScanner.GROOVY_MULTILINE_STRINGS);
    }
    
    public void testNone() throws Exception {
        tryString("\"\n\"\"\"", 0, IJavaPartitions.JAVA_STRING);
    }
    public void testNone2() throws Exception {
        tryString("''\n'''", 0, IJavaPartitions.JAVA_STRING);
    }

    public void testComment() throws Exception {
        tryString("/* blah\n" + 
                  " * blah\n" + 
                  " */", 
                  0, IJavaPartitions.JAVA_MULTI_LINE_COMMENT);
    }
    
    public void testJavaDoc() throws Exception {
        tryString("/** blah\n" + 
                  " * blah\n" + 
                  " */", 
                0, IJavaPartitions.JAVA_DOC);
    }

    private void tryString(String string, int start, String expectedContentType) {
        IDocument doc = new Document(string);
        scanner.setRange(doc, start, string.length());
        IToken token = scanner.nextToken();
        assertEquals("Incorrect content type for '" + string + "'", expectedContentType, token.getData());

    }
}
