/*
 * Copyright 2009-2025 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.locations;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.last;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests that source locations for groovy compilation units are computed properly.
 * <p>
 * Source locations are deteremined by special marker comments in the code:<pre>
 * markers /*m1s* /  /*f1s* /  /*t1s* /  indicates start of method, field and type
 * markers /*m1e* /  /*f1e* /  /*t1e* /  indicates end of method, field and type
 * markers /*m1sn* / /*f1sn* / /*t1sn* / indicates start of method, field and type name
 * markers /*m1en* / /*f1en* / /*t1en* / indicates end of method, field and type name
 * markers /*m1sb* /           /*t1sb* / indicates the start of method and type body</pre>
 */
public final class SourceLocationsTests extends BuilderTestSuite {

    private static void assertUnitWithSingleType(String source, ICompilationUnit unit) throws Exception {
        assertUnit(unit, source);

        CompilationUnit ast = null;
        unit.becomeWorkingCopy(null);
        try {
            SimpleProgressMonitor monitor = new SimpleProgressMonitor("reconcile");
            ast = unit.reconcile(JavaConstants.AST_LEVEL, true, null, monitor);
            monitor.waitForCompletion();
        } finally {
            unit.discardWorkingCopy();
        }

        IType decl = unit.getTypes()[0];
        AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) ast.types().get(0);

        assertDeclaration(decl, typeDecl, 0, source);

        IJavaElement[] children = decl.getChildren();
        List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations();
        for (int i = 0, j = 0, n = children.length; i < n; i += 1, j += 1) {
            // look for method variants that are the result of parameters with default values
            if (i > 0 && (children[i] instanceof IMethod) && children[i].getElementName().equals(children[i - 1].getElementName())) {
                IMethod variant = (IMethod) children[i];

                ISourceRange range = variant.getSourceRange();
                assertEquals(0, range.getOffset());
                assertEquals(0, range.getLength());

                range = variant.getNameRange(); // should match original
                assertEquals(((IMethod) children[i - 1]).getNameRange().getOffset(), range.getOffset());
                assertEquals(((IMethod) children[i - 1]).getNameRange().getLength(), range.getLength());

                // TODO: variant.getJavadocRange() should match original

                BodyDeclaration bodyDecl = bodyDecls.get(i);
                assertEquals(0, bodyDecl.getStartPosition());
                assertEquals(0, bodyDecl.getLength());

                assertNull(((MethodDeclaration) bodyDecl).getBody());
            } else {
                IMember member = (IMember) children[i];

                // check for multiple declaration fragments inside a field declaration
                // start locations and end locations for fragments are not calculated
                // entirely correctly the first fragment has a start and end of the
                // entire declaration the subsequent fragments have a start at the name
                // start and an end after the fragment's optional expression (or the
                // name end if there is none. so, here check to see if the name start
                // and source start are the same.  If so, then this is a second fragment

                if (decl.isEnum()) {
                    // not properly testing synthetic enum members yet
                    ISourceRange range = member.getSourceRange();
                    assertEquals(0, range.getOffset());
                    assertEquals(0, range.getLength());
                } else {
                    assertDeclaration(member, bodyDecls.get(i), j, source);
                }
            }
        }
    }

    private static void assertDeclaration(IMember decl, BodyDeclaration body, int memberNumber, String source) throws Exception {
        char astKind;
        switch (decl.getElementType()) {
        case IJavaElement.INITIALIZER:
            astKind = 'i';
            break;
        case IJavaElement.METHOD:
            astKind = 'm';
            break;
        case IJavaElement.FIELD:
            astKind = 'f';
            break;
        case IJavaElement.TYPE:
            astKind = 't';
            break;
        default:
            astKind = '?';
        }

        String startTag = "/*" + astKind + memberNumber + "s*/";
        int start = source.indexOf(startTag) + startTag.length();
        while (source.substring(start).startsWith("/*")) {
            start = source.indexOf("*/", start) + 2;
        }
        String endTag = "/*" + astKind + memberNumber + "e*/";
        int end = 0;
        if (start < startTag.length()) {
            start = 0;
        } else {
            int len = (isParrotParser() || (decl instanceof IField && source.charAt(source.indexOf(endTag) - 1) == ';') ||
                (decl instanceof IMethod && decl.getNameRange().getLength() > 0 && !Flags.isAbstract(decl.getFlags())) ? 0 : endTag.length());
            end = source.indexOf(endTag) + len;
            if (len == 0 && source.substring(0, end).endsWith("*/")) {
                end = source.substring(0, end).lastIndexOf("/*");
            } else if (len > 0) {
                while (source.substring(end).startsWith("/*")) {
                    end = source.indexOf("*/", end) + 2;
                }
            }
        }

        ISourceRange declRange = decl.getSourceRange();
        assertEquals(decl + "\nhas incorrect source start value", start, declRange.getOffset());
        assertEquals(body + "\nhas incorrect source start value", start, body.getStartPosition());
        assertEquals(decl + "\nhas incorrect source end value", end, declRange.getOffset() + declRange.getLength());

        String modsStartTag = "/*" + astKind + memberNumber + "sm*/";
        int modsStart = source.indexOf(modsStartTag);
        if (modsStart == -1) {
            modsStart = start;
        } else {
            modsStart += modsStartTag.length();
        }

        String modsEndTag = "/*" + astKind + memberNumber + "em*/";
        int modsEnd = source.indexOf(modsEndTag);
        if (modsEnd == -1) {
            modsEnd = modsStart;
        }

        List<ASTNode> mods = body.modifiers();
        if (mods != null && !mods.isEmpty()) {
            for (ASTNode mod : mods) {
                if (mod instanceof Annotation) {
                    var name = ((Annotation) mod).getTypeName();
                    assertEquals(mod.getStartPosition() + 1, name.getStartPosition());
                    assertEquals(name.getFullyQualifiedName().length(), name.getLength());
                }
            }
            if (!isParrotParser() && last(mods) instanceof Annotation) {
                modsEnd += modsEndTag.length(); // antlr2 includes comment
            }
            assertEquals(decl + "\nhas incorrect modifiers start value", modsStart, mods.get(0).getStartPosition());
            assertEquals(decl + "\nhas incorrect modifiers end value",   modsEnd,   last(mods).getStartPosition() + last(mods).getLength());
        } else {
            assertEquals(decl + "\nhas incorrect modifiers start value", modsStart, start);
            assertEquals(decl + "\nhas incorrect modifiers end value",   modsEnd,   start);
        }

        String nameStartTag = "/*" + astKind + memberNumber + "sn*/";
        int nameStart = source.indexOf(nameStartTag);
        if (nameStart == -1) {
            nameStart = start;
        } else {
            nameStart += nameStartTag.length();
        }

        String nameEndTag = "/*" + astKind + memberNumber + "en*/";
        int nameEnd = source.indexOf(nameEndTag);
        if (nameEnd == -1) {
            nameEnd = nameStart;
        } else if (astKind == 'm' && ((IMethod) decl).isConstructor() && !isParrotParser()) {
            // because the name of the constructor is not stored in the Antlr AST,
            // we calculate offsets of the constructor name by looking at the end
            // of the modifiers and the start of the opening paren
            nameEnd += nameEndTag.length();
        }

        if (astKind != 'i') {
            ISourceRange nameRange = decl.getNameRange();
            assertEquals(decl + "\nhas incorrect name start value", nameStart, nameRange.getOffset());
            assertEquals(decl + "\nhas incorrect name end value", nameEnd, nameRange.getOffset() + nameRange.getLength());
        }
        if (astKind == 'f') {
            SimpleName name = ((VariableDeclarationFragment) ((FieldDeclaration) body).fragments().get(0)).getName();
            assertEquals(body + "\nhas incorrect source start value", nameStart, name.getStartPosition());
            assertEquals(body + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + name.getLength());
        } else if (astKind == 'm') {
            SimpleName name = body instanceof MethodDeclaration ? ((MethodDeclaration) body).getName() : ((AnnotationTypeMemberDeclaration) body).getName();
            assertEquals(body + "\nhas incorrect source start value", nameStart, name.getStartPosition());
            assertEquals(body + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + (nameEnd - nameStart));
        }

        String bodyStartTag = "/*" + astKind + memberNumber + "sb*/";
        int bodyStart = source.indexOf(bodyStartTag);
        if (bodyStart != -1) {
            bodyStart += bodyStartTag.length();
        } else {
            bodyStart += 1;
        }
        if (astKind == 't') {
            //AbstractTypeDeclaration td = (AbstractTypeDeclaration) body;
            //assertEquals(body + "\nhas incorrect body start value", bodyStart, td.???.getStartPosition());
        } else if (astKind == 'i') {
            assertEquals(body + "\nhas incorrect body start value", bodyStart, ((Initializer) body).getBody().getStartPosition());
        } else if (astKind == 'm' && end > 0 && !Flags.isAbstract(decl.getFlags())) {
            assertEquals(body + "\nhas incorrect body start value", bodyStart, ((MethodDeclaration) body).getBody().getStartPosition());
        }

        int bodyEnd = body.getStartPosition() + body.getLength();
        if (bodyEnd > 0 && decl instanceof IMethod && (decl.getNameRange().getLength() == 0 ||
                (Flags.isAbstract(decl.getFlags()) && !Flags.isAnnotation(decl.getDeclaringType().getFlags())))) {
            bodyEnd += 1; // constructors and methods with a body have been set back by 1 for JDT compatibility
        } else if (body instanceof FieldDeclaration && source.charAt(source.indexOf(endTag) - 1) != ';') {
            end -= endTag.length();
        }
        assertEquals(body + "\nhas incorrect source end value", end, bodyEnd);
    }

    private static void assertRange(ISourceReference reference, int start, int until) throws Exception {
        assertEquals("Wrong offset for source element;", start, reference.getSourceRange().getOffset());
        assertEquals("Wrong end offset for source element;", until, reference.getSourceRange().getOffset() + reference.getSourceRange().getLength());
    }

    private static void assertUnit(ICompilationUnit unit, String source) throws Exception {
        assertEquals(unit + "\nhas incorrect source start value;", 0, unit.getSourceRange().getOffset());
        assertEquals(unit + "\nhas incorrect source end value;", source.length(), unit.getSourceRange().getLength());
    }

    private ICompilationUnit createCompUnit(String pack, String name, String text) throws Exception {
        IPath root = createGenericProject();
        IPath path = env.addGroovyClass(root, pack, name, text);

        fullBuild();
        expectingNoProblems();
        return env.getUnit(path);
    }

    private IPath createGenericProject() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        fullBuild(projectPath);

        return env.getPackageFragmentRootPath(projectPath, "src");
    }

    //--------------------------------------------------------------------------

    @Test
    public void testSourceLocations() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/public/*t0em*/ class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public static/*m0em*/ void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsSemicolons() throws Exception {
        String source =
            "package p1;\n" +
            "/*t0s*/public/*t0em*/ class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public static/*m0em*/ void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    println 'Hello world';\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/int /*f1sn*/x/*f1en*/ = 9;/*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsSuperclass() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/public/*t0em*/ class /*t0sn*/Hello/*t0en*/ extends Object /*t0sb*/{\n" +
            "  /*m0s*/public static/*m0em*/ void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsSuperfaces() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/public/*t0em*/ class /*t0sn*/Hello/*t0en*/ implements Runnable, Comparable<Hello> /*t0sb*/{\n" +
            "  /*m0s*/public static/*m0em*/ void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*m1s*/int /*m1sn*/compareTo/*m1en*/(Hello that) /*m1sb*/{}/*m1e*/\n" +
            "  /*m2s*/void /*m2sn*/run/*m2en*/() /*m2sb*/{}/*m2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoModifiers() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/def /*f1sn*/x/*f1en*/;/*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test // https://github.com/groovy/groovy-eclipse/issues/758
    public void testSourceLocationsAnnotationModifiers1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/@Deprecated/*m0em*/\n" +
            "  def /*m0sn*/main/*m0en*/(args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAnnotationModifiers2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/@Deprecated(/*lalala*/)/*m0em*/\n" +
            "  def /*m0sn*/main/*m0en*/(args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAnnotationModifiers3() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/@Deprecated(/*lalala*/)\n" +
            "  public static/*m0em*/ void /*m0sn*/main/*m0en*/(args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoParameters() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/() /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*m1s*/def /*m1sn*/main2/*m1en*/() /*m1sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m1e*/\n" +
            "  /*m2s*/def /*m2sn*/main3/*m2en*/() /*m2sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoParameterTypes1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/static/*m0em*/ /*m0sn*/main/*m0en*/(args) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoParameterTypes2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(args, fargs, blargs) /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsDefaultParameters() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(args = \"hi!\") /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "  /*m2s*/def /*m2sn*/main2/*m2en*/(args = \"hi!\", blargs = \"bye\") /*m2sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsThrowsTypes() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/def /*m0sn*/meth/*m0en*/() throws Exception /*m0sb*/{\n" +
            "    println 'Hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructor() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/() /*m0sb*/{\n" +
            "    println 'hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithParam() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/(String world) /*m0sb*/{\n" +
            "    println \"hello $world\"\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithParamNoType() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/(world) /*m0sb*/{\n" +
            "    println \"hello $world\"\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithDefaultParam() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/(String world = 'world') /*m0sb*/{\n" +
            "    println \"hello $world\"\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithDefaultParams() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/(one = \"1\", String two = \"2\") /*m0sb*/{\n" +
            "    println 'hello world'\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsForScript0() throws Exception {
        String source =
            "package p1\n";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("\n"), source.indexOf("\n") + 1); // odd but required for new folding

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.indexOf("\n"), source.indexOf("\n"));
    }

    @Test
    public void testSourceLocationsForScript1() throws Exception {
        String source =
            "package p1\n" +
            "def x";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("def"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.indexOf("def"), source.length());
    }

    @Test
    public void testSourceLocationsForScript2() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("def"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, 0, 0);
    }

    @Test
    public void testSourceLocationsForScript3() throws Exception {
        String source =
            "package p1\n" +
            "x() \n def x() {}";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("x"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.indexOf("x"), source.indexOf("x") + (isParrotParser() ? 3 : 4));
    }

    @Test
    public void testSourceLocationsForScript4() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}\nx()";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("def"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.lastIndexOf("x"), source.length());
    }

    @Test
    public void testSourceLocationsForScript5() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}\nx()\ndef y() {}";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("def"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.lastIndexOf("x"), source.lastIndexOf("x") + 3);
    }

    @Test
    public void testSourceLocationsForScript6() throws Exception {
        String source =
            "package p1\n" +
            "x()\n def x() {}\n\ndef y() {}\ny()";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType script = unit.getTypes()[0];
        assertRange(script, source.indexOf("x"), source.length());

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.indexOf("x"), source.length());
    }

    @Test
    public void testSourceLocationsForScript7() throws Exception {
        String source =
            "package p1\n" +
            "class C {}\n" +
            "println()\n";

        ICompilationUnit unit = createCompUnit("p1", "Hello", source);
        assertUnit(unit, source);

        IType other = unit.getTypes()[0];
        assertEquals("C", other.getElementName());
        assertRange(other, source.indexOf("class"), source.indexOf("}") + 1);

        IType script = unit.getTypes()[1];
        assertEquals("Hello", script.getElementName());
        assertRange(script, source.indexOf("println"), source.lastIndexOf("\n"));

        IMethod method = script.getMethod("run", new String[0]);
        assertRange(method, source.indexOf("println"), source.lastIndexOf("\n"));
    }

    @Test
    public void testSourceLocationsInterface1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/interface /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ String /*m0sn*/hello/*m0en*/(args, String blargs)/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsInterface2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/interface /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/String /*m0sn*/hello/*m0en*/(args, String blargs) throws Exception, Error/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAbstractClass1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/abstract/*t0em*/ class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public abstract/*m0em*/ /*m0sn*/hello/*m0en*/(args, String blargs)/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAbstractClass2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/abstract/*t0em*/ class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/abstract/*m0em*/ /*m0sn*/hello/*m0en*/(args, String blargs) throws Exception/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAnnotationDeclaration() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/@interface /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/String /*m0sn*/val/*m0en*/()/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsEnumDeclaration() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/enum /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsStaticInitializers() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*i0s*/static/*i0em*/ /*i0sb*/{\n" +
            "    int i = 0\n" +
            "  }/*i0e*/\n" +
            "  /*i1s*/static/*i1em*/\n" +
            "  /*i1sb*/{\n" +
            "    print 'f'\n" +
            "  }/*i1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsObjectInitializers1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*i1s*//*i1sb*/{\n" +
            "    int i = 0\n" +
            "  }/*i1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsObjectInitializers2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*i1s*//*i1sb*/{\n" +
            "    int i = 0\n" +
            "  }/*i1e*/\n" +
            "  /*i2s*//*i2sb*/{\n" +
            "    int j = 1\n" +
            "  }/*i2e*/\n" +
            "  /*i3s*//*i3sb*/{\n" +
            "  }/*i3e*//*m1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test @Ignore
    public void testSourceLocationsTrailingWhitespace1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "}/*t0e*/    \t    \n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test @Ignore
    public void testSourceLocationsTrailingWhitespace2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*f0s*/int /*f0sn*/foo/*f0en*/ = 1/*f0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsTrailingWhitespace3() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/public/*m0em*/ /*m0sn*/Hello/*m0en*/(int one, int two) /*m0sb*/{\n" +
            "  }/*m0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsTrailingWhitespace4() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ /*t0sb*/{\n" +
            "  /*m0s*/protected/*m0em*/ String /*m0sn*/bar/*m0en*/(int one, int two) /*m0sb*/{\n" +
            "  }/*m0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsMultipleVariableFragments() throws Exception {
        String source =
            "package p1\n" +
            "class Hello {\n" +
            "  int x = 1, y = 2, z = 3;\n" +
            "}\n";

        IType type = createCompUnit("p1", "Hello", source).getTypes()[0];

        IMember field = (IMember) type.getChildren()[0];
        ISourceRange range = field.getSourceRange();
        assertEquals(source.indexOf("int"), range.getOffset());
        assertEquals(source.indexOf(";"), range.getOffset() + range.getLength() - 1);
        range = field.getNameRange();
        assertEquals(source.indexOf("x"), range.getOffset());
        assertEquals(source.indexOf("x"), range.getOffset() + range.getLength() - 1);

        field = (IMember) type.getChildren()[1];
        range = field.getSourceRange();
        assertEquals(source.indexOf("int"), range.getOffset());
        assertEquals(source.indexOf(";"), range.getOffset() + range.getLength() - 1);
        range = field.getNameRange();
        assertEquals(source.indexOf("y"), range.getOffset());
        assertEquals(source.indexOf("y"), range.getOffset() + range.getLength() - 1);

        field = (IMember) type.getChildren()[2];
        range = field.getSourceRange();
        assertEquals(source.indexOf("int"), range.getOffset());
        assertEquals(source.indexOf(";"), range.getOffset() + range.getLength() - 1);
        range = field.getNameRange();
        assertEquals(source.indexOf("z"), range.getOffset());
        assertEquals(source.indexOf("z"), range.getOffset() + range.getLength() - 1);
    }
}
