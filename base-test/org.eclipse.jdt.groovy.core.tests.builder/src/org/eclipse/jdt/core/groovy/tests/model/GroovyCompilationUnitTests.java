/*
 * Copyright 2009-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.jdt.core.groovy.tests.model;

import static org.eclipse.jdt.core.groovy.tests.ReconcilerUtils.reconcile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.junit.Test;

public final class GroovyCompilationUnitTests extends GroovyTypeRootTestSuite {

    @Test
    public void testCreateJavaCompilationUnit() throws Exception {
        IPath projectPath = env.addProject("Project");
        fullBuild(projectPath);

        IPath root = env.getPackageFragmentRootPath(projectPath, "src");

        IPath path = env.addClass(root, "p1", "Hello",
            "package p1;\n" +
            "public class Hello {\n" +
            "  public static void main(String[] args) {\n" +
            "    System.out.println(\"Hello world\");\n" +
            "  }\n" +
            "}"
        );

        ICompilationUnit unit = env.getUnit(path);

        assertTrue(unit.exists());
        assertFalse(unit instanceof GroovyCompilationUnit);
    }

    @Test
    public void testCreateGroovyCompilationUnit() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(groovyFile);
        assertTrue("CompilationUnit " + groovyFile + " should exist.", unit.exists());
        assertTrue("CompilationUnit " + groovyFile + " should be a Groovy compilation unit.", unit instanceof GroovyCompilationUnit);
    }

    @Test
    public void testGetModuleNode1() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        ModuleNode node2 = unit1.getModuleNode();
        assertSame("getModuleNode() should return the same object if nothing has changed underneath", node1, node2);
        unit1.discardWorkingCopy();
    }

    @Test
    public void testGetModuleNode2() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        ModuleNode node2 = unit2.getModuleNode();
        assertSame("getModuleNode() should return the same object if nothing has changed underneath", node1, node2);
        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();
    }

    @Test
    public void testGetModuleNode3() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(JavaConstants.AST_LEVEL, true, unit1.owner, null);
        ModuleNode node2 = unit1.getModuleNode();
        assertNotSame("getModuleNode() should not return the same object after a call to reconcile with problem detection enabled", node1, node2);
        unit1.discardWorkingCopy();
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testGetModuleNode4() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.makeConsistent(JavaConstants.AST_LEVEL, true, ICompilationUnit.FORCE_PROBLEM_DETECTION, new HashMap(), null);
        ModuleNode node2 = unit1.getModuleNode();
        assertSame("getModuleNode() should return the same object if nothing has changed underneath", node1, node2);
        unit1.discardWorkingCopy();
    }

    @Test
    public void testGetModuleNode5() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.getBuffer().append(" ");
        ModuleNode node2 = unit1.getModuleNode();
        assertNotSame("getModuleNode() should return different objects if something has changed underneath", node1, node2);
        unit1.discardWorkingCopy();
    }

    @Test
    public void testGetModuleNode6() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        ModuleNode node2 = unit2.getModuleNode();
        unit1.getBuffer().append(" ");
        ModuleNode node3 = unit1.getModuleNode();
        ModuleNode node4 = unit2.getModuleNode();
        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();

        assertSame("getModuleNode() should return the same object if nothing has changed underneath", node1, node2);
        assertNotSame("getModuleNode() should return different objects if something has changed underneath", node1, node3);
        assertSame("getModuleNode() should return the same object if nothing has changed underneath", node3, node4);
    }

    @Test
    public void testGetModuleNode7() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        unit1.getModuleNode();
        GroovyCompilationUnit unit2 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit2.becomeWorkingCopy(null);
        unit2.getModuleNode();
        unit1.getBuffer().append(" ");
        unit1.getModuleNode();
        unit2.getModuleNode();
        unit1.discardWorkingCopy();
        unit2.discardWorkingCopy();

        assertTrue("ModuleNodeMapper should be empty when there are no working copies", moduleNodeMapperCacheSize >= ModuleNodeMapper.size());
    }

    @Test
    public void testGetModuleNode8() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        ModuleNode node1 = unit1.getModuleNode();
        ModuleNode node2 = unit1.getModuleNode();
        unit1.discardWorkingCopy();

        assertNotSame("getModuleNode() should return the different objects if unit is not a working copy", node1, node2);
    }

    @Test
    public void testGetModuleNode9() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(true, null);
        ModuleNode node2 = unit1.getModuleNode();
        unit1.discardWorkingCopy();

        assertNotSame("getModuleNode() should return the different objects after a call to reconcile with force problem detection", node1, node2);
    }

    @Test
    public void testGetModuleNode10() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        unit1.becomeWorkingCopy(null);
        ModuleNode node1 = unit1.getModuleNode();
        unit1.reconcile(false, null);
        ModuleNode node2 = unit1.getModuleNode();
        unit1.discardWorkingCopy();

        assertSame("getModuleNode() should return the same object after a call to reconcile with no force problem detection", node1, node2);
    }

    @Test
    public void testGetNewModuleNode() throws Exception {
        IFile groovyFile = createSimpleGroovyProject();
        GroovyCompilationUnit unit1 = (GroovyCompilationUnit) JavaCore.createCompilationUnitFrom(groovyFile);
        ModuleNode module1 = unit1.getModuleNode();
        ModuleNode module2 = unit1.getNewModuleInfo().module;
        unit1.discardWorkingCopy();

        assertNotSame("getNewModuleNode() should have forced creation of a new module node", module1, module2);
    }

    @Test
    public void testMarkerAnnotation1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "@Anno2\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        IAnnotation annotation = type.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    @Test
    public void testMarkerAnnotation2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno2\n" +
            "  public int foo = 5\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        IAnnotation annotation = field.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    @Test
    public void testMarkerAnnotation3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  @Anno2\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on method main()", 1, method.getAnnotations().length);
        IAnnotation annotation = method.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "Anno2");
    }

    @Test
    public void testMarkerAnnotation4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "@p.Anno2\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        IAnnotation annotation = type.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    @Test
    public void testMarkerAnnotation5() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  @p.Anno2\n" +
            "  public int foo = 5\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        IAnnotation annotation = field.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    @Test
    public void testMarkerAnnotation6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  @p.Anno2\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on method main()", 1, method.getAnnotations().length);
        IAnnotation annotation = method.getAnnotations()[0];
        assertMarkerAnnotation(annotation, "p.Anno2");
    }

    @Test
    public void testMarkerAnnotation7() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "class X {\n" +
            "  @Anno2\n" +
            "  X() {\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = env.getUnit(path);
        IMethod method = unit.getType("X").getMethods()[0];
        IAnnotation[] annotations = method.getAnnotations();

        assertEquals(1, annotations.length);
        assertMarkerAnnotation(annotations[0], "Anno2");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMarkerAnnotation8() throws Exception {
        // source references to Groovy types go from Groovy AST -> GCUD -> JDT bindings -> Groovy AST
        IPath root = createAnnotationGroovyProject();
        env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "class X {\n" +
            "  @Anno2\n" +
            "  X() {\n" +
            "  }\n" +
            "}\n"
        );
        IPath path = env.addGroovyClass(root, "p", "Y",
            "package p;\n" +
            "class Y extends X {\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();

        GroovyCompilationUnit unit = env.getUnit(path);
        ClassNode x = unit.getModuleNode().getClasses().get(0).getSuperClass();
        List annotations = x.getDeclaredConstructors().get(0).getAnnotations();

        assertEquals(1, annotations.size());
    }

    @Test
    public void testSingleMemberAnnotation1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "@Anno1(Target.class)\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        assertSingleMemberAnnotation(type, "Target");
    }

    @Test
    public void testSingleMemberAnnotation2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno1(Target.class)\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        assertSingleMemberAnnotation(field, "Target");
    }

    @Test
    public void testSingleMemberAnnotation3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  @Anno1(Target.class)\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on field m", 1, method.getAnnotations().length);
        assertSingleMemberAnnotation(method, "Target");
    }

    @Test
    public void testSingleMemberAnnotation4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "@Anno1(p.Target.class)\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        assertEquals("Should have one annotation on type X", 1, type.getAnnotations().length);
        assertSingleMemberAnnotation(type, "p.Target");
    }

    @Test
    public void testSingleMemberAnnotation5() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  @Anno1(p.Target.class)\n" +
            "  public int foo = 5\n" +
            "  public static void main(String[]argv) {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IField field = type.getField("foo");
        assertEquals("Should have one annotation on field foo", 1, field.getAnnotations().length);
        assertSingleMemberAnnotation(field, "p.Target");
    }

    @Test
    public void testSingleMemberAnnotation6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "public class X {\n" +
            "  public int foo = 5\n" +
            "  @Anno1(p.Target.class)\n" +
            "  public static void m() {\n" +
            "    print \"success\"\n" +
            "  }\n" +
            "}\n"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        IType type = unit.getType("X");
        IMethod method = type.getMethod("m", new String[0]);
        assertEquals("Should have one annotation on field m", 1, method.getAnnotations().length);
        assertSingleMemberAnnotation(method, "p.Target");
    }

    @Test
    public void testAnonymousInner1() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "def foo = new Runnable() {\n" +
            "  void run() {}\n" +
            "}"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test
    public void testAnonymousInner2() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "def foo = new Runnable() {\n" +
            "  void run() {}\n" +
            "}\n" +
            "foo = new Runnable() {\n" +
            "  void run() {}\n" +
            "  void other() {}\n" +
            "}"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 2, children.length);

            for (int i = 0; i < 2; i++) {
                IType anonType = (IType) children[i];
                assertEquals("Anon type should have empty name", "", anonType.getElementName());
                IJavaElement[] anonChildren = anonType.getChildren();
                assertEquals("Expecting exactly one child, but found: " + Arrays.toString(anonChildren), i + 1, anonChildren.length);
                assertEquals("run", anonChildren[0].getElementName());
                if (i == 1) {
                    assertEquals("other", anonChildren[1].getElementName());
                }
            }
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test
    public void testAnonymousInner3() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "class Foo {\n" +
            "  def run() {\n" +
            "    def foo = new Runnable() {\n" +
            "      void run() {}\n" +
            "    }\n" +
            "  }\n" +
            "}"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test
    public void testAnonymousInner4() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "class Foo {\n" +
            "  def run() {\n" +
            "    def foo = new Runnable() {\n" +
            "      void run() {}\n" +
            "    }\n" +
            "    foo = new Runnable() {\n" +
            "      void run() {}\n" +
            "      void other() {}\n" +
            "    }\n" +
            "  }\n" +
            "}"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 2, children.length);

            for (int i = 0; i < 2; i++) {
                IType anonType = (IType) children[i];
                assertEquals("Anon type should have empty name", "", anonType.getElementName());
                IJavaElement[] innerChildren = anonType.getChildren();
                assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), i + 1, innerChildren.length);
                assertEquals("run", innerChildren[0].getElementName());
                if (i == 1) {
                    assertEquals("other", innerChildren[1].getElementName());
                }
            }
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test
    public void testAnonymousInner5() throws Exception {
        IPath path = env.addGroovyClass(createAnnotationGroovyProject(), "p", "X",
            "package p;\n" +
            "class Foo {\n" +
            "  def foo = new Runnable() {\n" +
            "    void run() {}\n" +
            "  }\n" +
            "}"
        );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            IType type = unit.getType("Foo");
            IField field = type.getField("foo");
            assertEquals(1, field.getChildren().length);
            IJavaElement inner = field.getChildren()[0];
            assertEquals(IJavaElement.TYPE, inner.getElementType());
            assertTrue("Anon inner type should exist as a local type", inner.exists());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test // ensures classes in a script are not treated as anon inners
    public void testAnonymousInner6() throws Exception {
        IPath root = createAnnotationGroovyProject();
        IPath path = env.addGroovyClass(root, "p", "X",
            "package p;\n" +
            "class Other {}\n" +
            "def foo = new Runnable() {\n" +
            "  void run() {}\n" +
            "}\n" +
            "class Other2 {}"
            );
        incrementalBuild();
        env.waitForAutoBuild();
        expectingNoProblems();
        GroovyCompilationUnit unit = env.getUnit(path);
        unit.becomeWorkingCopy(new NullProgressMonitor());
        try {
            unit.getType("Other").exists();
            unit.getType("Other2").exists();
            IType type = unit.getType("X");
            IMethod method = type.getMethod("run", new String[0]);
            IJavaElement[] children = method.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            IType anonType = (IType) children[0];
            assertEquals("Anon type should have empty name", "", anonType.getElementName());
            children = anonType.getChildren();
            assertEquals("Expecting exactly one child, but found: " + Arrays.toString(children), 1, children.length);
            assertEquals("run", children[0].getElementName());
        } finally {
            unit.discardWorkingCopy();
        }
    }

    @Test
    public void testVariadicMethod1() throws Exception {
        IPath path = env.addGroovyClass(createEmptyGroovyProject(), "X",
            "class X {\n" +
            "  private void fn(String one, int... two) {}\n" +
            "}");

        GroovyCompilationUnit unit = env.getUnit(path);
        Set<IProblem> problems = reconcile(unit);
        assertTrue(problems.isEmpty());
    }

    //--------------------------------------------------------------------------

    private void assertMarkerAnnotation(IAnnotation annotation, String expectedName)
            throws JavaModelException {
        assertEquals("Wrong name for annotation", expectedName, annotation.getElementName());
        assertEquals("Should be a marker annotation, but had member-value pairs", 0, annotation.getMemberValuePairs().length);
    }

    private void assertSingleMemberAnnotation(IAnnotatable type, String expectedName)
            throws JavaModelException {
        IAnnotation annotation = type.getAnnotations()[0];
        assertEquals("Wrong name for annotation", "Anno1", annotation.getElementName());
        assertEquals("Should be a single member annotation, but did not have exactly one member-value pair", 1, annotation.getMemberValuePairs().length);
        IMemberValuePair mvp = annotation.getMemberValuePairs()[0];
        assertEquals("value", mvp.getMemberName());
        assertEquals(IMemberValuePair.K_CLASS, mvp.getValueKind());
        assertEquals(expectedName, mvp.getValue());
    }
}
