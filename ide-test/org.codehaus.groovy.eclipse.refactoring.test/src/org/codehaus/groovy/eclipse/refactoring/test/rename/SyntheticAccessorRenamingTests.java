/*******************************************************************************
 * Copyright (c) 2011 VMware Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.refactoring.test.rename;

import org.codehaus.groovy.eclipse.refactoring.core.rename.SyntheticAccessorsRenameParticipant;
import org.codehaus.groovy.eclipse.refactoring.test.AbstractRefactoringTest;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

/**
 * Testing the {@link SyntheticAccessorsRenameParticipant}
 * 
 * @author Andrew Eisenberg
 * @created Oct 13, 2011
 */
public class SyntheticAccessorRenamingTests extends AbstractRefactoringTest {
    
    
    public void testSingleFileRename1() throws Exception {
        performRefactoringAndUndo("flar", 
                new String[] { "p" }, 
                new String[] { "First.groovy" }, 
                new String[] { 
                        "package p\n" +
                		"class First {\n" +
                		"  def foo\n" +
                		"  def run() {\n" +
                		"    foo\n" +
                		"    getFoo()\n" +
                		"    setFoo()\n" +
                		"    isFoo()\n" +
                		"  }\n" +
                		"}" 
                		}, 
                		new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def flar\n" +
                        "  def run() {\n" +
                        "    flar\n" +
                        "    getFlar()\n" +
                        "    setFlar()\n" +
                        "    isFlar()\n" +
                        "  }\n" +
                        "}" 
                        }
        ); 
    }
    // don't automatically rename if the accessors are explicitly named
    public void testSingleFileRename2() throws Exception {
        performRefactoringAndUndo("flar", 
                new String[] { "p" }, 
                new String[] { "First.groovy" }, 
                new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def foo\n" +
                        "  def getFoo() { }\n" +
                        "  def setFoo() { }\n" +
                        "  def isFoo() { }\n" +
                        "  def run() {\n" +
                        "    foo\n" +
                        "    getFoo()\n" +
                        "    setFoo()\n" +
                        "    isFoo()\n" +
                        "  }\n" +
                        "}" 
                        }, 
                        new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def flar\n" +
                        "  def getFoo() { }\n" +
                        "  def setFoo() { }\n" +
                        "  def isFoo() { }\n" +
                        "  def run() {\n" +
                        "    flar\n" +
                        "    getFoo()\n" +
                        "    setFoo()\n" +
                        "    isFoo()\n" +
                        "  }\n" +
                        "}" 
                        }
        ); 
    }
    
    public void testMultiFileRename1() throws Exception {
        performRefactoringAndUndo("flar", 
                new String[] { "p", "q" }, 
                new String[] { "First.groovy", "Script.groovy" }, 
                new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def foo\n" +
                        "}",
                        
                        "package q\n" +
                        "def f = new p.First()\n" +
                        "f.foo\n" +
                        "f.getFoo()\n" +
                        "f.setFoo()\n" +
                        "f.isFoo()"
                        }, 
                        new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def flar\n" +
                        "}",
                        
                        "package q\n" +
                        "def f = new p.First()\n" +
                        "f.flar\n" +
                        "f.getFlar()\n" +
                        "f.setFlar()\n" +
                        "f.isFlar()"
                        }
        ); 
    }

    // don't automatically rename if the accessors are explicitly named
    public void testMultiFileRename2() throws Exception {
        performRefactoringAndUndo("flar", 
                new String[] { "p", "q" }, 
                new String[] { "First.groovy", "Script.groovy" }, 
                new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def foo\n" +
                        "  def getFoo() { }\n" +
                        "  def setFoo() { }\n" +
                        "  def isFoo() { }\n" +
                        "}",
                        
                        "package q\n" +
                        "def f = new p.First()\n" +
                        "f.foo\n" +
                        "f.getFoo()\n" +
                        "f.setFoo()\n" +
                        "f.isFoo()"
                        }, 
                        new String[] { 
                        "package p\n" +
                        "class First {\n" +
                        "  def flar\n" +
                        "  def getFoo() { }\n" +
                        "  def setFoo() { }\n" +
                        "  def isFoo() { }\n" +
                        "}",
                        
                        "package q\n" +
                        "def f = new p.First()\n" +
                        "f.flar\n" +
                        "f.getFoo()\n" +
                        "f.setFoo()\n" +
                        "f.isFoo()"
                        }
        ); 
    }
    // this will have compile errors, but it should still work
        public void testJavaRename1() throws Exception {
            performRefactoringAndUndo("flar", 
                    new String[] { "p", "q" }, 
                    new String[] { "First.groovy", "Java.java" }, 
                    new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def foo\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    f.foo = null;\n" +
                            "    f.getFoo();\n" +
                            "    f.setFoo(null);\n" +
                            "    f.isFoo();\n" +
                            "  }\n" +
                            "}"
                            }, 
                            new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def flar\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    f.flar = null;\n" +
                            "    f.getFlar();\n" +
                            "    f.setFlar(null);\n" +
                            "    f.isFlar();\n" +
                            "  }\n" +
                            "}"
                            }
            ); 
        }

        // this will have compile errors, but it should still work
        // don't automatically rename if the accessors are explicitly named
        public void testJavaRename2() throws Exception {
            performRefactoringAndUndo("flar", 
                    new String[] { "p", "q" }, 
                    new String[] { "First.groovy", "Script.groovy" }, 
                    new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def foo\n" +
                            "  def getFoo() { }\n" +
                            "  def setFoo(arg) { }\n" +
                            "  def isFoo() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    f.foo = null;\n" +
                            "    f.getFoo();\n" +
                            "    f.setFoo(null);\n" +
                            "    f.isFoo();\n" +
                            "  }\n" +
                            "}"
                            }, 
                            new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def flar\n" +
                            "  def getFoo() { }\n" +
                            "  def setFoo(arg) { }\n" +
                            "  def isFoo() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    f.flar = null;\n" +
                            "    f.getFoo();\n" +
                            "    f.setFoo(null);\n" +
                            "    f.isFoo();\n" +
                            "  }\n" +
                            "}"
                            }
            ); 
        }
        
        // this will have compile errors, but it should still work
        public void testGetterOnly() throws Exception {
            performRefactoringAndUndo("getFlar", 
                    new String[] { "p", "q", "r" }, 
                    new String[] { "First.groovy", "Java.java", "Script.groovy" }, 
                    new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def getFoo() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.foo;\n" +
                            "    f.getFoo();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.foo\n" +
                            "f.getFoo()\n"
                            }, 
                            new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def getFlar() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.flar;\n" +
                            "    f.getFlar();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.flar\n" +
                            "f.getFlar()\n"
                            }
            ); 
        }
        
        // this will have compile errors, but it should still work
        public void testIsserOnly() throws Exception {
            performRefactoringAndUndo("isFlar", 
                    new String[] { "p", "q", "r" }, 
                    new String[] { "First.groovy", "Java.java", "Script.groovy" }, 
                    new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def isFoo() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.foo;\n" +
                            "    f.isFoo();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.foo\n" +
                            "f.isFoo()\n"
                            }, 
                            new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def isFlar() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.flar;\n" +
                            "    f.isFlar();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.flar\n" +
                            "f.isFlar()\n"
                            }
            ); 
        }
        
        // this will have compile errors, but it should still work
        public void testSetterOnly() throws Exception {
            performRefactoringAndUndo("setFlar", 
                    new String[] { "p", "q", "r" }, 
                    new String[] { "First.groovy", "Java.java", "Script.groovy" }, 
                    new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def setFoo() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.foo;\n" +
                            "    f.setFoo();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.foo\n" +
                            "f.setFoo()\n"
                            }, 
                            new String[] { 
                            "package p\n" +
                            "class First {\n" +
                            "  def setFlar() { }\n" +
                            "}",
                            
                            "package q;\n" +
                            "class Java {\n" +
                            "  void l() {\n" +
                            "    p.First f = new p.First();\n" +
                            "    Object o = f.flar;\n" +
                            "    f.setFlar();\n" +
                            "  }\n" +
                            "}",
                            
                            "p.First f = new p.First()\n" +
                            "f.flar\n" +
                            "f.setFlar()\n"
                            }
            ); 
        }

    private void performRefactoringAndUndo(String newName, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        performRefactoringAndUndo(newName, true, true, packNames, cuNames, initialContents, finalContents);
    }
    
    // assume we are renaming the first memebr of the first type to the new name
    private void performRefactoringAndUndo(String newName, boolean updateReferences, boolean performOnError, String[] packNames, String[] cuNames, String[] initialContents, String[] finalContents) throws Exception {
        ICompilationUnit[] units = createUnits(packNames, cuNames, initialContents);
        
        IMember toRename = (IMember) units[0].getTypes()[0].getChildren()[0];
        String id = toRename instanceof IField ? IJavaRefactorings.RENAME_FIELD
                : IJavaRefactorings.RENAME_METHOD;
        RenameJavaElementDescriptor descriptor = RefactoringSignatureDescriptorFactory
                .createRenameJavaElementDescriptor(id);
        descriptor.setUpdateReferences(updateReferences);
        descriptor.setJavaElement(toRename);
        descriptor.setNewName(newName);
        descriptor.setRenameGetters(false);
        descriptor.setRenameSetters(false);
        descriptor.setProject(testProject.getProject().getName());
        
        RenameRefactoring refactoring = (RenameRefactoring) createRefactoring(descriptor);
        RefactoringStatus result = performRefactoring(refactoring, true, performOnError);

        if (!performOnError) {
            assertTrue("Refactoring produced an error: " + result, result.isOK());
        }

        assertContents(units, finalContents);
        
        // undo
        assertTrue("anythingToUndo", RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("! anythingToRedo", !RefactoringCore.getUndoManager()
                .anythingToRedo());

        RefactoringCore.getUndoManager().performUndo(null,
                new NullProgressMonitor());
        
        assertContents(units, initialContents);

        // redo
        assertTrue("! anythingToUndo", !RefactoringCore.getUndoManager()
                .anythingToUndo());
        assertTrue("anythingToRedo", RefactoringCore.getUndoManager()
                .anythingToRedo());
        RefactoringCore.getUndoManager().performRedo(null,
                new NullProgressMonitor());
        assertContents(units, finalContents);
    }

}