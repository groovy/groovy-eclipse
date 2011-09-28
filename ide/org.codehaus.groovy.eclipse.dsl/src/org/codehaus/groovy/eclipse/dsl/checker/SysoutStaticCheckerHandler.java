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
package org.codehaus.groovy.eclipse.dsl.checker;

import java.io.PrintStream;

import org.codehaus.groovy.ast.ASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Shell;

/**
 * Prints the results of static checking to sysout or some other specified print stream
 * @author andrew
 * @created Aug 31, 2011
 */
public class SysoutStaticCheckerHandler implements IStaticCheckerHandler {

    private int numProblems = 0;
    
    private final PrintStream out;
    
    public SysoutStaticCheckerHandler(PrintStream out) {
        this.out = out;
    }
    
    public void handleUnknownReference(ASTNode node, Position position, int line) {
        out.println(createUnknownMessage(node, line));
        numProblems++;
    }

    public void handleTypeAssertionFailed(ASTNode node, String expectedType, String actualType, Position position, int line) {
        out.println(createInvalidTypeMessage(node, expectedType, actualType, line));
        numProblems++;
    }

    public void setResource(IFile resource) {
        out.println("\nChecking: " + resource.getFullPath());
    }
    
    private String createUnknownMessage(ASTNode node, int line) {
        return "\tLine " + line + ": unknown type: " + node.getText();
    }
    
    private String createInvalidTypeMessage(ASTNode node, String expectedType, String actualType, int line) {
        return "\tLine " + line + ": Invalid inferred type.  " + node.getText() + "  Expected: " + expectedType + " Actual: " + actualType;
    }

    public int numProblemsFound() {
        return numProblems;
    }

    public void handleResourceStart(IResource resource) throws CoreException {
        // do nothing
    }
    
    public boolean finish(Shell shell) {
        String message = createMessage();
        out.println(message);
//        out.flush();
        if (out != System.out) {
            out.close();
            System.out.println(message);
        }
        return numProblems == 0;
    }
    
    private String createMessage() {
        if (numProblems == 0) {
            return "SUCCESS";
        } else if (numProblems == 1) {
            return "FAILURE found 1 type checking problem";
        } else {
            return "FAILURE found " + numProblems + " type checking problems";
        }
    }
}