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

package org.codehaus.groovy.eclipse.codeassist.tests;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;



/**
 * @author Andrew Eisenberg
 * @created Nov 1, 2010
 *
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 * 
 * FIXADE Failing on build server not being run
 */
public class ConstructorCompletionTests extends CompletionTestCase {


    public ConstructorCompletionTests(String name) {
        super(name);
    }

    boolean orig;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        orig = GroovyPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS);
        GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, false);
    }
    @Override
    protected void tearDown() throws Exception {
        try {   
            super.tearDown();
        } finally {
            GroovyPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS, orig);
        }
    }
    
    public void testConstructorCompletion1() throws Exception {
        String contents = "package f\n\nclass YY { YY() { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY() { } }\nnew YY()\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
    public void testConstructorCompletion2() throws Exception {
        String contents = "package f\n\nclass YY { YY(x) { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY(x) { } }\nnew YY(x)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
    public void testConstructorCompletion3() throws Exception {
        String contents = "package f\n\nclass YY { YY(x, y) { } }\nnew Y\nkkk";
        String expected = "package f\n\nclass YY { YY(x, y) { } }\nnew YY(x, y)\nkkk";
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, "new Y"), "YY");
    }
    
}
