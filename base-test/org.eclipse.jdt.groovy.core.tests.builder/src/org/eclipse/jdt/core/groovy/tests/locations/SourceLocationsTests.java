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

package org.eclipse.jdt.core.groovy.tests.locations;

import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.groovy.tests.builder.GroovierBuilderTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.CompilationUnit;

/**
 * Tests that source locations for groovy compilation units are computed properly
 * 
 * Source locations are deteremined by special marker comments in the code
 * markers /*m1s* / /*f1s* / /*t1s* / indicate start of method, field and type
 * markers /*m1e* / /*f1e* / /*t1e* / indicate end of method, field and type
 * markers /*m1sn* / /*f1sn* / /*t1sn* / indicate start of method, field and type names
 * markers /*m1en* / /*f1en* / /*t1en* / indicate end of method, field and type names
 * markers /*m1sb* / indicate the start of a method body
 * NOTE: the start of a type body is not being calculated correctly  
 * 
 * @author Andrew Eisenberg
 * @created Jun 29, 2009
 *
 */
public class SourceLocationsTests extends GroovierBuilderTests {
	public SourceLocationsTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(SourceLocationsTests.class);
	}

	
	public void testSourceLocations() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/public class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public static void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\");\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/;\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}
	public void testSourceLocationsNoSemiColons() throws Exception {
	    String source = "package p1;\n"+
	    "/*t0s*/public class /*t0sn*/Hello/*t0en*/ {\n"+
	    "   /*m0s*/public static void /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n"+
	    "      System.out.println(\"Hello world\");\n"+
	    "   }/*m0e*/\n"+
	    "   /*f1s*/int /*f1sn*/x/*f1en*/ = 9/*f1e*/\n"+
	    "}/*t0e*/\n";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsNoModifiers() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/def /*m0sn*/main/*m0en*/(String[] args) /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsMultipleVariableFragments() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   int /*f0sn*/x/*f0en*/, /*f1sn*/y/*f1en*/, /*f2sn*/z/*f2en*/ = 7\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsNoParameterTypes() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/def /*m0sn*/main/*m0en*/(args, fargs, blargs) /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsNoParameters() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/def /*m0sn*/main/*m0en*/() /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*m1s*/def /*m1sn*/main2/*m1en*/() /*m1sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m1e*/\n"+
		"   /*m2s*/def /*m2sn*/main3/*m2en*/() /*m2sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m2e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsDefaultParameters() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/def /*m0sn*/main/*m0en*/(args = \"hi!\") /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*m1s*/def /*m1sn*/main2/*m1en*/(args = \"hi!\", blargs = \"bye\") /*m1sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsConstructor() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public /*m0sn*/Hello/*m0en*/() /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsConstructorWithParam() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public /*m0sn*/Hello/*m0en*/(String x) /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsConstructorWithParamNoType() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public /*m0sn*/Hello/*m0en*/(x) /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	public void testSourceLocationsConstructorWithDefaultParam() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public /*m0sn*/Hello/*m0en*/(args = \"9\") /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}
	
	public void testSourceLocationsForScript1() throws Exception {
        String source = "package p1;\n"+
        "def x";
        ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
        assertScript(source, unit, "def x", "def x");	    
        assertUnit(unit, source);
	}

	public void testSourceLocationsForScript2() throws Exception {
	    String source = "package p1;\n"+
	    "def x() { }";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertScript(source, unit, "def x", "{ }");	    
	    assertUnit(unit, source);
	}
	
	public void testSourceLocationsForScript3() throws Exception {
	    String source = "package p1;\n"+
	    "x() \n def x() { }";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertScript(source, unit, "x()", "{ }");	    
	    assertUnit(unit, source);
	}
	
	public void testSourceLocationsForScript4() throws Exception {
	    String source = "package p1;\n"+
	    "def x() { }\nx()";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertScript(source, unit, "def x", "x()");	    
	    assertUnit(unit, source);
	}
	
	public void testSourceLocationsForScript5() throws Exception {
	    String source = "package p1;\n"+
	    "def x() { }\nx()\ndef y() { }";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertScript(source, unit, "def x", "def y() { }");	    
	    assertUnit(unit, source);
	}
	
	public void testSourceLocationsForScript6() throws Exception {
	    String source = "package p1;\n"+
	    "x()\n def x() { }\n\ndef y() { }\ny()";
	    ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
	    assertScript(source, unit, "x()", "\ny()");	    
	    assertUnit(unit, source);
	}
	
    public void testSourceLocationsConstructorWithDefaultParams() throws Exception {
		String source = "package p1;\n"+
		"/*t0s*/class /*t0sn*/Hello/*t0en*/ {\n"+
		"   /*m0s*/public /*m0sn*/Hello/*m0en*/(args = \"9\", String blargs = \"8\") /*m0sb*/{\n"+
		"      System.out.println(\"Hello world\")\n"+
		"   }/*m0e*/\n"+
		"   /*f1s*/def /*f1sn*/x/*f1en*//*f1e*/\n"+
		"}/*t0e*/\n";
		ICompilationUnit unit = createCompilationUnitFor("p1", "Hello", source);
		assertUnitWithSingleType(source, unit);
	}

	// next test a variety of slocs for var decl fragments

	private void assertUnitWithSingleType(String source, ICompilationUnit unit)
			throws Exception, JavaModelException {
		assertUnit(unit, source);
		
		ASTParser newParser = ASTParser.newParser(AST.JLS3);
		newParser.setSource(unit);
        org.eclipse.jdt.core.dom.CompilationUnit ast = (org.eclipse.jdt.core.dom.CompilationUnit) newParser.createAST(null);
		int maxLength = ((CompilationUnit) unit).getContents().length-1;
		IType decl = unit.getTypes()[0];
		AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) ast.types().get(0);
        assertDeclaration(decl, typeDecl, 0, source, maxLength);
		IJavaElement[] children = decl.getChildren();
		List<BodyDeclaration> bodyDecls = typeDecl.bodyDeclarations(); 
		for (int i = 0, j= 0, k = 0, l = 0; i < children.length; i++, j++, l++) {
			// look for method variants that use default params
			if (i > 0 && (children[i] instanceof IMethod) && children[i].getElementName().equals(children[i-1].getElementName())) {
				j--;
			}
			// check for multiple declaration fragments inside a field declaration
			// start locations and end locations for fragments are not calculated entirely correctly
			// the first fragment has a start and end of the entire declaration
			// the subsequent fragments have a start at the name start and an end after the fragment's optional expression (or the name end if there is none.
			// so, here check to see if the name start and source start are the same.  If so, then this is a second fragment

			assertDeclaration((IMember) children[i], bodyDecls.get(i), j, source, maxLength);
		}
	}



	private void assertDeclaration(IMember decl, BodyDeclaration bd, int methodNumber, String source, int maxLength) throws Exception {
		char astKind;
 		if (decl instanceof IMethod) {
			astKind = 'm';
		} else if (decl instanceof IField) {
			astKind = 'f';
		} else {
			astKind = 't';
		}
		
		String startTag = "/*" + astKind + methodNumber + "s*/";
		int start = source.indexOf(startTag) + startTag.length();
		
		String endTag = "/*" + astKind + methodNumber + "e*/";
		int end = Math.min(source.indexOf(endTag) + endTag.length(), maxLength);

		boolean ignore = false;
		if (decl instanceof IField && (start == 6 || end == 6)) {
			 // a field with multiple variable declaration fragments
			 // this is not yet being calculated properly
			 ignore = true;
		}
		
		
		if (!ignore) {
			ISourceRange declRange = decl.getSourceRange();
			assertEquals(decl + "\nhas incorrect source start value", start, declRange.getOffset());
			assertEquals(decl + "\nhas incorrect source end value", end, declRange.getOffset() + declRange.getLength());
			
			// now check the AST
            assertEquals(bd + "\nhas incorrect source start value", start, bd.getStartPosition());
            int sourceEnd = bd.getStartPosition() + bd.getLength();
            // It seems like field declarations should not include the trailing ';' in their source end if one exists 
            if (bd instanceof FieldDeclaration) sourceEnd --;  
            assertEquals(bd + "\nhas incorrect source end value", end, sourceEnd);
		}
		
		String nameStartTag = "/*" + astKind + methodNumber + "sn*/";
		int nameStart = source.indexOf(nameStartTag) + nameStartTag.length();
		
		String nameEndTag = "/*" + astKind + methodNumber + "en*/";
		int nameEnd = source.indexOf(nameEndTag);
		// because the name of the constructor is not stored in the Antlr AST, 
		// we calculate offsets of the constructor name by looking at the end
		// of the modifiers and the start of the opening paren
		if (decl instanceof IMethod && ((IMethod) decl).isConstructor()) {
			nameEnd+= nameEndTag.length();
		}
		
		ISourceRange nameDeclRange = decl.getNameRange();
		assertEquals(decl + "\nhas incorrect source start value", nameStart, nameDeclRange.getOffset());
		assertEquals(decl + "\nhas incorrect source end value", nameEnd, nameDeclRange.getOffset() + nameDeclRange.getLength());
		
        // now check the AST
		if (bd instanceof FieldDeclaration) {
		    FieldDeclaration fd = (FieldDeclaration) bd;
		    SimpleName name = ((VariableDeclarationFragment) fd.fragments().get(0)).getName();
            assertEquals(bd + "\nhas incorrect source start value", nameStart, name.getStartPosition());
		    assertEquals(bd + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + name.getLength());
		}
		if (bd instanceof MethodDeclaration) {
		    MethodDeclaration md = (MethodDeclaration) bd;
            SimpleName name = md.getName();
            assertEquals(bd + "\nhas incorrect source start value", nameStart, name.getStartPosition());
            assertEquals(bd + "\nhas incorrect source end value", nameEnd, name.getStartPosition() + name.getLength());
		}
		
		
		if (decl.getElementType() == IJavaElement.METHOD) {
		    // body start is only calculated for methods
		    String bodyStartTag = "/*" + astKind + methodNumber + "sb*/";
		    int bodyStart = source.indexOf(bodyStartTag) + bodyStartTag.length();
            MethodDeclaration md = (MethodDeclaration) bd;
            assertEquals(bd + "\nhas incorrect body start value", bodyStart, md.getBody().getStartPosition());
		}
	}
	
	private void assertUnit(ICompilationUnit unit, String source) throws Exception {
		assertEquals(unit + "\nhas incorrect source start value", 0, unit.getSourceRange().getOffset());
		assertEquals(unit + "\nhas incorrect source end value", source.length(), unit.getSourceRange().getLength());
	}
	
    private void assertScript(String source, ICompilationUnit unit, String startText, String endText) throws Exception {
        IType script = unit.getTypes()[0];
        IMethod runMethod = script.getMethod("run", new String[0]);
        int start = source.indexOf(startText);
        int end = source.lastIndexOf(endText)+endText.length();
        assertEquals("Wrong start for script class.  Text:\n" + source, start, script.getSourceRange().getOffset());
        assertEquals("Wrong end for script class.  Text:\n" + source, end, script.getSourceRange().getOffset()+script.getSourceRange().getLength());
        assertEquals("Wrong start for run method.  Text:\n" + source, start, runMethod.getSourceRange().getOffset());
        assertEquals("Wrong end for run method.  Text:\n" + source, end, runMethod.getSourceRange().getOffset()+script.getSourceRange().getLength());
    }


	
	private IPath createGenericProject() throws Exception {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.addGroovyJars(projectPath);
		fullBuild(projectPath);
		
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		
		return root;
	}
	
	private ICompilationUnit createCompilationUnitFor(String pack, String name, String source) throws Exception {
		IPath root = createGenericProject();
		IPath path = env.addGroovyClass(root, pack, name, source);
				
		fullBuild();
		expectingNoProblems();
		IFile groovyFile = getFile(path);
		return JavaCore.createCompilationUnitFrom(groovyFile);
	}
	
	
    private IFile getFile(IPath path) {
        return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
    }
}