/*******************************************************************************
 * Copyright (c) 2006. 2010 IBM Corporation, SpringSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Andrew Eisenberg - completely rewritten for 2.1.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.test.rename;

import org.codehaus.groovy.eclipse.refactoring.test.AbstractRefactoringTest;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

/**
 * Test that moving compilaiton units and scripts between packages updates 
 * import statements appropriately
 */
public class MoveCURefactoringTests extends AbstractRefactoringTest {
    
    
    public void testSimpleMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
        }, 
        new String[] {
                "Java.java", 
        }, new String[] {
                "package p;\npublic class Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
        });
    }
    
    public void testSimpleMove2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
        }, 
        new String[] {
                "Groovy.groovy", 
        }, new String[] {
                "package p;\npublic class Groovy { }", 
        }, new String[] { 
                "package NEW;\npublic class Groovy { }", 
        });
    }
    
    public void testSimpleMove3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Groovy.groovy", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic class Groovy extends Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\n\nimport NEW.Java;\n\npublic class Groovy extends Java { }", 
        });
    }
    
    public void testQualifiedMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Java.java", 
                "Groovy.groovy", 
        }, new String[] {
                "package p;\npublic class Java { }", 
                "package p;\npublic class Groovy extends p.Java { }", 
        }, new String[] { 
                "package NEW;\npublic class Java { }", 
                "package p;\npublic class Groovy extends NEW.Java { }", 
        });
    }
    
    public void testSimpleMove4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p1", 
                "p2", 
        }, 
        new String[] {
                "Groovy.groovy", 
                "Groovy2.groovy", 
        }, new String[] {
                "package p1\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\nimport p1.Groovy\npublic class Groovy2 extends Groovy { }", 
        }, new String[] { 
                "package NEW\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\nimport NEW.Groovy;\npublic class Groovy2 extends Groovy { }", 
        });
    }
    
    public void testQualifiedMove2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p1", 
                "p2", 
        }, 
        new String[] {
                "Groovy.groovy", 
                "Groovy2.groovy", 
        }, new String[] {
                "package p1\npublic class Groovy {\np2.Groovy2 g }", 
                "package p2\npublic class Groovy2 extends p1.Groovy { }", 
        }, new String[] { 
                "package NEW\npublic class Groovy {\np2.Groovy2 g }", 
                "package p2\npublic class Groovy2 extends NEW.Groovy { }", 
        });
    }
    
    // reference to non-primary type moved
    // moved with a reference to non-primary type

    
    public void testNonPrimaryMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p1", 
                "p2", 
        }, 
        new String[] {
                "GroovyFoo.groovy", 
                "GroovyFoo2.groovy", 
        }, new String[] {
                "package p1\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\nimport p1.Groovy\npublic class Groovy2 extends Groovy { }", 
        }, new String[] { 
                "package NEW\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\nimport NEW.Groovy;\npublic class Groovy2 extends Groovy { }", 
        });
    }
    
    
    public void testNonPrimaryQualifiedMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p1", 
                "p2", 
        }, 
        new String[] {
                "GroovyFoo.groovy", 
                "GroovyFoo2.groovy", 
        }, new String[] {
                "package p1\npublic class Groovy {\np2.Groovy2 g }", 
                "package p2\npublic class Groovy2 extends p1.Groovy { }", 
        }, new String[] { 
                "package NEW\npublic class Groovy {\np2.Groovy2 g }", 
                "package p2\npublic class Groovy2 extends NEW.Groovy { }", 
        });
    }
    
    public void testMoveBack1() throws Exception {
        performRefactoringAndUndo("p2", 
                new String[] { 
                "p1", 
                "p2", 
        }, 
        new String[] {
                "Groovy.groovy", 
                "Groovy2.groovy", 
        }, new String[] {
                "package p1\nimport p2.Groovy2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\nimport p1.Groovy\npublic class Groovy2 extends Groovy { }", 
        }, new String[] { 
                "package p2\npublic class Groovy {\nGroovy2 g }", 
                "package p2\npublic class Groovy2 extends Groovy { }", 
        });
    }

    // Failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    public void _testInnerMove1() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Groovy.groovy", 
                "Groovy2.groovy", 
        }, new String[] {
                "package p;\npublic class Groovy { \n class Inner { } }", 
                "package p;\npublic class Groovy2 extends Groovy.Inner { }", 
        }, new String[] { 
                "package NEW;\npublic class Groovy { \n class Inner { } }", 
                "package p;\nimport NEW.Groovy;\npublic class Groovy2 extends Groovy.Inner { }", 
        });
    }

    // Failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    public void _testInnerMove2() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Groovy2.groovy", 
                "Groovy.groovy", 
        }, new String[] {
                "package p;\npublic class Groovy2 extends Groovy.Inner { }", 
                "package p;\npublic class Groovy { \n class Inner { } }", 
        }, new String[] { 
                "package NEW;\nimport p.Groovy;\npublic class Groovy2 extends Groovy.Inner { }", 
                "package p;\npublic class Groovy { \n class Inner { } }", 
        });
    }

    // Failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    public void _testInnerMove3() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Groovy.groovy", 
                "Groovy2.groovy", 
        }, new String[] {
                "package p;\npublic class Groovy { \n class Inner { } }", 
                "package p;\nimport Groovy.Inner\npublic class Groovy2 extends Inner { }", 
        }, new String[] { 
                "package NEW;\npublic class Groovy { \n class Inner { } }", 
                "package p;\nimport NEW.Groovy.Inner;\npublic class Groovy2 extends Inner { }", 
        });
    }

    // Failing, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=350205
    public void _testInnerMove4() throws Exception {
        performRefactoringAndUndo("NEW", 
                new String[] { 
                "p", 
                "p", 
        }, 
        new String[] {
                "Groovy2.groovy", 
                "Groovy.groovy", 
        }, new String[] {
                "package p;\nimport Groovy.Inner\npublic class Groovy2 extends Inner { }", 
                "package p;\npublic class Groovy { \n class Inner { } }", 
        }, new String[] { 
                "package NEW;\nimport p.Groovy.Inner;\npublic class Groovy2 extends Inner { }", 
                "package p;\npublic class Groovy { \n class Inner { } }", 
        });
    }

    // assume we are moving the first CU to the new specified package
    private void performRefactoringAndUndo(String newPackageName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        IPackageFragment newPackage = testProject.createPackage(newPackageName);
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        
        MoveDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createMoveDescriptor();
        descriptor.setDestination(newPackage);
        descriptor.setUpdateReferences(true);
        descriptor.setProject(testProject.getProject().getName());
        descriptor.setUpdateQualifiedNames(true);
        descriptor.setMoveResources(new IFile[0], new IFolder[0], new ICompilationUnit[] { units[0] });
        
        Refactoring refactoring = createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, true);
        
        result = ignoreKnownErrors(result);

        assertTrue("Refactoring produced an error: " + result, result.isOK());
        
        ICompilationUnit newUnit = getNewUnit(newPackageName, cuNames[0]);
        ICompilationUnit origUnit = units[0];
        units[0] = newUnit;
        assertContents(units, finalContents);
        
        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        
        units[0] = origUnit;
        assertContents(units, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        units[0] = newUnit;
        assertContents(units, finalContents);
    }

    private ICompilationUnit getNewUnit(String newPackName, String name) throws JavaModelException {
        int dotIndex = name.indexOf('.');
        String typeName = name.substring(0, dotIndex); 
        String qualName = newPackName.length() > 0 ? newPackName + "." + typeName : typeName;
        return testProject.getJavaProject().findType(qualName).getCompilationUnit();
    }
}