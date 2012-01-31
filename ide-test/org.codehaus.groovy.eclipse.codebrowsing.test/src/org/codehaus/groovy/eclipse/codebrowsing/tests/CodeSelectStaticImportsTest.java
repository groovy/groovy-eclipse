/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.codebrowsing.tests;

import org.eclipse.jdt.core.SourceRange;


/**
 * @author Andrew Eisenberg
 * @created Jan 21, 2012
 *
 */
public class CodeSelectStaticImportsTest extends BrowsingTestCase {

    public CodeSelectStaticImportsTest() {
        super(CodeSelectStaticImportsTest.class.getName());
    }

    public void testStaticImport1() throws Exception {
        createUnit("Other", "class Other {\n  static int FOO\n static boolean BAR() { } }");
        String contents = "import static Other.FOO";
        String toLookFor = "FOO";
        assertCodeSelect(contents, new SourceRange(contents.indexOf(toLookFor), toLookFor.length()), toLookFor);
    }

    public void testStaticImport2() throws Exception {
        createUnit("Other", "class Other {\n  static int FOO\n static boolean BAR() { } }");
        String contents = "import static Other.BAR";
        String toLookFor = "BAR";
        assertCodeSelect(contents, new SourceRange(contents.indexOf(toLookFor), toLookFor.length()), toLookFor);
    }

    public void testStaticImport3() throws Exception {
        createUnit("Other", "class Other {\n  static int FOO\n static boolean BAR() { } }");
        String contents = "import static Other.FOO\nFOO";
        String toLookFor = "FOO";
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf(toLookFor), toLookFor.length()), toLookFor);
    }

    public void testStaticImport4() throws Exception {
        createUnit("Other", "class Other {\n  static int FOO\n static boolean BAR() { } }");
        String contents = "import static Other.BAR\nBAR";
        String toLookFor = "BAR";
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf(toLookFor), toLookFor.length()), toLookFor);
    }

    public void testStaticImport5() throws Exception {
        createUnit("Other", "class Other {\n  static int FOO\n static boolean BAR() { } }");
        String contents = "import static Other.BAR\nBAR";
        String toLookFor = "Other";
        assertCodeSelect(contents, new SourceRange(contents.lastIndexOf(toLookFor), toLookFor.length()), toLookFor);
    }
}