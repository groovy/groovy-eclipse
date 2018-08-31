/*
 * Copyright 2009-2018 the original author or authors.
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
package org.eclipse.jdt.core.groovy.tests.locations;

import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isAtLeastGroovy;
import static org.eclipse.jdt.groovy.core.tests.GroovyBundle.isParrotParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.groovy.tests.builder.BuilderTestSuite;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.groovy.core.util.JavaConstants;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests that source locations for groovy compilation units are computed properly.
 * <p>
 * Source locations are deteremined by special marker comments in the code:<pre>
 * markers /*m1s* / /*f1s* / /*t1s* / indicate start of method, field and type
 * markers /*m1e* / /*f1e* / /*t1e* / indicate end of method, field and type
 * markers /*m1sn* / /*f1sn* / /*t1sn* / indicate start of method, field and type names
 * markers /*m1en* / /*f1en* / /*t1en* / indicate end of method, field and type names
 * markers /*m1sb* / indicate the start of a method body</pre>
 *
 * NOTE: The start of a type body is not being calculated correctly.
 */
public final class SourceLocationsTests extends BuilderTestSuite {

    private static void assertUnitWithSingleType(String source, ICompilationUnit unit) throws Exception {
        assertUnit(unit, source);

        ASTParser newParser = ASTParser.newParser(JavaConstants.AST_LEVEL);
        newParser.setSource(unit);
        CompilationUnit ast = (CompilationUnit) newParser.createAST(null);
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

                range = variant.getNameRange();
                assertEquals(0, range.getOffset());
                assertEquals(0, range.getLength());

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

    private static void assertDeclaration(IMember decl, BodyDeclaration bd, int memberNumber, String source) throws Exception {
        char astKind;
        switch (decl.getElementType()) {
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
        if (source.substring(start).startsWith("/*")) {
            start = source.indexOf("*/", start) + 2;
        }

        String endTag = "/*" + astKind + memberNumber + "e*/";
        int len = (isParrotParser() || (decl instanceof IMethod && !Flags.isAbstract(((IMethod) decl).getFlags())) ? 0 : endTag.length());
        int end = source.indexOf(endTag) + len;
        while (len == 0 && source.substring(0, end).endsWith("*/")) {
            end = source.substring(0, end).lastIndexOf("/*");
        }

        ISourceRange declRange = decl.getSourceRange();
        assertEquals(decl + "\nhas incorrect source start value", start, declRange.getOffset());
        assertEquals(decl + "\nhas incorrect source end value", end, declRange.getOffset() + declRange.getLength());

        // now check the AST
        assertEquals(bd + "\nhas incorrect source start value", start, bd.getStartPosition());
        int bodyEnd = bd.getStartPosition() + bd.getLength(); // adjust for possible ';'
        if (decl instanceof IMethod && Flags.isAbstract(((IMethod) decl).getFlags()) &&
                !Flags.isAnnotation(decl.getDeclaringType().getFlags())) {
            bodyEnd += 1;
        } else if (bd instanceof FieldDeclaration) {
            bodyEnd -= 1;
        }
        assertEquals(bd + "\nhas incorrect source end value", end, bodyEnd);

        String nameStartTag = "/*" + astKind + memberNumber + "sn*/";
        int nameStart = source.indexOf(nameStartTag) + nameStartTag.length();

        String nameEndTag = "/*" + astKind + memberNumber + "en*/";
        int nameEnd = source.indexOf(nameEndTag);
        // because the name of the constructor is not stored in the Antlr AST,
        // we calculate offsets of the constructor name by looking at the end
        // of the modifiers and the start of the opening paren
        if (astKind == 'm' && ((IMethod) decl).isConstructor() && !isParrotParser()) {
            nameEnd += nameEndTag.length();
        }

        ISourceRange nameDeclRange = decl.getNameRange();
        assertEquals(decl + "\nhas incorrect source start value", nameStart, nameDeclRange.getOffset());
        assertEquals(decl + "\nhas incorrect source end value", nameEnd, nameDeclRange.getOffset() + nameDeclRange.getLength());

        // now check the AST
        if (bd instanceof FieldDeclaration) {
            SimpleName name = ((VariableDeclarationFragment) ((FieldDeclaration) bd).fragments().get(0)).getName();

            assertEquals(bd + "\nhas incorrect source start value", nameStart, name.getStartPosition());
            assertEquals(bd + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + name.getLength());
        } else if (bd instanceof MethodDeclaration) {
            SimpleName name = ((MethodDeclaration) bd).getName();

            assertEquals(bd + "\nhas incorrect source start value", nameStart, name.getStartPosition());
            assertEquals(bd + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + name.getLength());
        }

        if (astKind == 'm') {
            // body start is only calculated for methods
            String bodyStartTag = "/*" + astKind + memberNumber + "sb*/";
            int bodyStart = source.indexOf(bodyStartTag) + bodyStartTag.length();
            if (bd instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) bd;
                // will be null for interfaces or abstract methods
                if (md.getBody() != null) {
                    int actualBodyStart = md.getBody().getStartPosition();
                    assertEquals(bd + "\nhas incorrect body start value", bodyStart, actualBodyStart);
                }
            }
        }
    }

    private static void assertScript(String source, ICompilationUnit unit, String startText, String endText) throws Exception {
        assertUnit(unit, source);
        IType script = unit.getTypes()[0];
        IMethod runMethod = script.getMethod("run", new String[0]);
        int start = source.indexOf(startText);
        int end = source.lastIndexOf(endText) + endText.length();
        assertEquals("Wrong start for script class.  Text:\n" + source, start, script.getSourceRange().getOffset());
        assertEquals("Wrong end for script class.  Text:\n" + source, end, script.getSourceRange().getOffset() + script.getSourceRange().getLength());
        assertEquals("Wrong start for run method.  Text:\n" + source, start, runMethod.getSourceRange().getOffset());
        assertEquals("Wrong end for run method.  Text:\n" + source, end, runMethod.getSourceRange().getOffset() + script.getSourceRange().getLength());
    }

    private static void assertUnit(ICompilationUnit unit, String source) throws Exception {
        assertEquals(unit + "\nhas incorrect source start value", 0, unit.getSourceRange().getOffset());
        assertEquals(unit + "\nhas incorrect source end value", source.length(), unit.getSourceRange().getLength());
    }

    private IPath createGenericProject() throws Exception {
        IPath projectPath = env.addProject("Project");
        env.addGroovyJars(projectPath);
        env.removePackageFragmentRoot(projectPath, "");
        IPath root = env.addPackageFragmentRoot(projectPath, "src");
        env.setOutputFolder(projectPath, "bin");
        fullBuild(projectPath);
        return root;
    }

    private ICompilationUnit createCompUnit(String pack, String name, String source) throws Exception {
        IPath root = createGenericProject();
        IPath path = env.addGroovyClass(root, pack, name, source);

        fullBuild();
        expectingNoProblems();
        IFile groovyFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        return JavaCore.createCompilationUnitFrom(groovyFile);
    }

    //--------------------------------------------------------------------------

    @Test
    public void testSourceLocations() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/public class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public static void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\");\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/;\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoSemiColons() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/public class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public static void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\");\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoModifiers() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "  /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsMultipleVariableFragments() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*f0s*/int /*f0sn*/x/*f0en*/ = 1/*f0e*/, /*f1s*//*f1sn*/y/*f1en*/ = 2/*f1e*/, /*f2s*//*f2sn*/z/*f2en*/ = 3/*f2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoParameterTypes() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(args, fargs, blargs) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsNoParameters() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/() /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "  /*m1s*/def /*m1sn*/main2/*m1en*/() /*m1sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m1e*/\n" +
            "  /*m2s*/def /*m2sn*/main3/*m2en*/() /*m2sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsDefaultParameters() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/def /*m0sn*/main/*m0en*/(args = \"hi!\") /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "  /*m2s*/def /*m2sn*/main2/*m2en*/(args = \"hi!\", blargs = \"bye\") /*m2sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m2e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructor() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/() /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithParam() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/(String x) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithParamNoType() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/(x) /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsConstructorWithDefaultParam() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/(args = \"9\") /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsForScript1() throws Exception {
        String source =
            "package p1\n" +
            "def x";
        assertScript(source, createCompUnit("p1", "Hello", source), "def x", "def x");
    }

    @Test
    public void testSourceLocationsForScript2() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}";
        assertScript(source, createCompUnit("p1", "Hello", source), "def x", "{}");
    }

    @Test
    public void testSourceLocationsForScript3() throws Exception {
        String source =
            "package p1\n" +
            "x() \n def x() {}";
        assertScript(source, createCompUnit("p1", "Hello", source), "x()", "{}");
    }

    @Test
    public void testSourceLocationsForScript4() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}\nx()";
        assertScript(source, createCompUnit("p1", "Hello", source), "def x", "x()");
    }

    @Test
    public void testSourceLocationsForScript5() throws Exception {
        String source =
            "package p1\n" +
            "def x() {}\nx()\ndef y() {}";
        assertScript(source, createCompUnit("p1", "Hello", source), "def x", "def y() {}");
    }

    @Test
    public void testSourceLocationsForScript6() throws Exception {
        String source =
            "package p1\n" +
            "x()\n def x() {}\n\ndef y() {}\ny()";
        assertScript(source, createCompUnit("p1", "Hello", source), "x()", "\ny()");
    }

    @Test
    public void testSourceLocationsConstructorWithDefaultParams() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/(args = \"9\", String blargs = \"8\") /*m0sb*/{\n" +
            "    System.out.println(\"Hello world\")\n" +
            "  }/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsInterface1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/interface /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public String /*m0sn*/hello/*m0en*/(args, String blargs)/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsInterface2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/interface /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/String /*m0sn*/hello/*m0en*/(args, String blargs) throws Exception, Error/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAbstractClass1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/abstract class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public abstract /*m0sn*/hello/*m0en*/(args, String blargs)/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAbstractClass2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/abstract class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/abstract /*m0sn*/hello/*m0en*/(args, String blargs) throws Exception/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsAnnotationDeclaration() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/@interface /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/String /*m0sn*/val/*m0en*/()/*m0e*/\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsEnumDeclaration() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/enum /*t0sn*/Hello/*t0en*/ {\n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test @Ignore
    public void testSourceLocationsTrailingWhitespace1() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "}/*t0e*/    \t    \n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test @Ignore
    public void testSourceLocationsTrailingWhitespace2() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*f0s*/int /*f0sn*/foo/*f0en*/ = 1/*f0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsTrailingWhitespace3() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/public /*m0sn*/Hello/*m0en*/(int one, int two) /*m0sb*/{\n" +
            "  }/*m0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test
    public void testSourceLocationsTrailingWhitespace4() throws Exception {
        String source =
            "package p1\n" +
            "/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n" +
            "  /*m0s*/protected String /*m0sn*/bar/*m0en*/(int one, int two) /*m0sb*/{\n" +
            "  }/*m0e*/    \t    \n" +
            "}/*t0e*/\n";
        assertUnitWithSingleType(source, createCompUnit("p1", "Hello", source));
    }

    @Test // STS-3878
    public void testErrorPositionForUnsupportedOperation() throws Exception {
        assumeTrue(!isAtLeastGroovy(26));
        String source =
            "def a = 'a'\n" +
            "def b = 'b'\n" +
            "println a === b\n";
        IPath root = createGenericProject();
        IPath path = env.addGroovyClass(root, "", "Hello", source);
        incrementalBuild();
        expectingSpecificProblemFor(root, new Problem("p/Hello",
            "Groovy:Operator (\"===\" at 3:11:  \"===\" ) not supported @ line 3, column 11.", path, 34, 37, 60, IMarker.SEVERITY_ERROR));
    }
}
