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

package org.codehaus.groovy.eclipse.test.debug;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.eclipse.debug.ui.ValidBreakpointLocationFinder;
import org.codehaus.groovy.eclipse.test.Activator;
import org.codehaus.groovy.eclipse.test.EclipseTestCase;
import org.codehaus.groovy.eclipse.test.SynchronizationUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * @author Andrew Eisenberg
 * @created Jul 24, 2009
 *
 * Tests that breakpoint locations are as expected
 *
 */
public class BreakpointLocationTests extends EclipseTestCase {
    private static final String BREAKPOINT_SCRIPT_NAME = "BreakpointTesting.groovy";
    
    private GroovyCompilationUnit unit;
    
    private IDocument document;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
         GroovyRuntime.addGroovyRuntime(testProject.getProject());

         InputStream input = null;
         final URL url = Activator.bundle().getEntry(
                 "/testData/groovyfiles/" + BREAKPOINT_SCRIPT_NAME);
         try {
             input = url.openStream();
             IFile file = testProject.createGroovyTypeAndPackage("shapes",
                     BREAKPOINT_SCRIPT_NAME, input);
             
             unit = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(file);
         } finally {
             IOUtils.closeQuietly(input);
         }
         String text;
         try {
             input = url.openStream();
             text = IOUtils.toString(input);
         } finally {
             IOUtils.closeQuietly(input);
         }
         document = new Document(text);
         
         unit.becomeWorkingCopy(null);
         unit.makeConsistent(null);
    }
    
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        unit.discardWorkingCopy();
    }
    
    public void testBreakpointInScript1() throws Exception {
        doBreakpointTest(1);
    }
    
    public void testBreakpointInScript2() throws Exception {
        doBreakpointTest(2);
    }

    public void testBreakpointInScript3() throws Exception {
        doBreakpointTest(3);
    }

    public void testBreakpointInScript4() throws Exception {
        doBreakpointTest(4);
    }

    // this one is failing because the x() method is defined
    // within the run() method
//    public void testBreakpointInScript5() throws Exception {
//        doBreakpointTest(5);
//    }

    public void testBreakpointInScript6() throws Exception {
        doBreakpointTest(6);
    }

    public void testBreakpointInScript7() throws Exception {
        doBreakpointTest(7);
    }

    public void testBreakpointInScript8() throws Exception {
        doBreakpointTest(8);
    }

    public void testBreakpointInScript9() throws Exception {
        doBreakpointTest(9);
    }

    public void testBreakpointInScript10() throws Exception {
        doBreakpointTest(10);
    }

    public void testBreakpointInScript11() throws Exception {
        doBreakpointTest(11);
    }

    public void testBreakpointInScript12() throws Exception {
        doBreakpointTest(12);
    }

    public void testBreakpointInScript13() throws Exception {
        doBreakpointTest(13);
    }
    
    private void doBreakpointTest(int i) throws Exception {
        int location = document.get().indexOf("// " + i);
        int line = document.getLineOfOffset(location) + 3;  // note that the class creation process adds 3 lines to the top of the file 
        ValidBreakpointLocationFinder finder = new ValidBreakpointLocationFinder(line);
        ASTNode node = finder.findValidBreakpointLocation(unit.getModuleNode());
        assertNotNull("Could not find a breakpoint for line " + line, node);
        assertEquals("Wrong expected line number", line, node.getLineNumber());
    }
}
