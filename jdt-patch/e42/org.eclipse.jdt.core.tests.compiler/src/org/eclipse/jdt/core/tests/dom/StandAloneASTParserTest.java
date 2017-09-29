/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;

public class StandAloneASTParserTest extends AbstractRegressionTest {
	public StandAloneASTParserTest(String name) {
		super(name);
	}
	/**
	 * Internal synonynm for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	public ASTNode runConversion(
			int astLevel,
			String source,
			boolean resolveBindings,
			boolean statementsRecovery,
			boolean bindingsRecovery,
			String unitName) {

		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(source.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		parser.setCompilerOptions(getCompilerOptions());
		parser.setUnitName(unitName);
		return parser.createAST(null);
	}
	public void test1() {
		String contents =
				"package p;\n" + 
				"public class X {\n" + 
				"	public int i;\n" + 
				"	public static void main(String[] args) {\n" + 
				"		int length = args.length;\n" + 
				"		System.out.println(length);\n" + 
				"	}\n" + 
				"}";
		ASTNode node = runConversion(JLS3_INTERNAL, contents, true, true, true, "p/X.java");
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) node;
		List types = unit.types();
		TypeDeclaration typeDeclaration  = (TypeDeclaration) types.get(0);
		ITypeBinding binding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertNull("Got a java element", binding.getJavaElement());
		assertEquals("Wrong name", "p.X", binding.getQualifiedName());
		MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(1);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		assertNull("Got a java element", methodBinding.getJavaElement());
		Block body = methodDeclaration.getBody();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) body.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertNull("Got a java element", variableBinding.getJavaElement());
		ExpressionStatement statement2 = (ExpressionStatement) body.statements().get(1);
		Expression expression = statement2.getExpression();
		MethodInvocation invocation = (MethodInvocation) expression;
		Expression expression2 = invocation.getExpression();
		assertNotNull("No binding", expression2.resolveTypeBinding());
		
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typeDeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		IVariableBinding variableBinding2 = fragment2.resolveBinding();
		assertNotNull("No binding", variableBinding2);
		assertNull("Got a java element", variableBinding2.getJavaElement());
	}

	public void test2() {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Ljava/lang/String;";
		final IBinding[] bindings = new IBinding[1];

		FileASTRequestor requestor = new FileASTRequestor() {
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (key.equals(bindingKey)) {
					bindings[0] = binding;
				}
			}
		};

		parser.createASTs(new String[] {}, null, new String[] {key}, requestor, null);

		assertNotNull("No binding", bindings[0]);
		assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
		ITypeBinding typeBinding = (ITypeBinding) bindings[0];
		assertEquals("Wrong binding", "java.lang.String", typeBinding.getQualifiedName());
		assertNull("No java element", typeBinding.getJavaElement());
	}

	public void test3() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/X;";
		final IBinding[] bindings = new IBinding[1];

		String contents =
			"package p;\n" + 
			"public class X extends Y {\n" + 
			"	public int i;\n" + 
			"	public static void main(String[] args) {\n" + 
			"		int length = args.length;\n" + 
			"		System.out.println(length);\n" + 
			"	}\n" + 
			"}";
		
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" + 
			"public class Y {}";
		File fileY = new File(packageDir, "Y.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = file.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];
	
			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};
	
			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);
	
			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);
	
			assertNotNull("No binding", bindings[0]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
			ITypeBinding typeBinding = (ITypeBinding) bindings[0];
			assertEquals("Wrong binding", "p.X", typeBinding.getQualifiedName());
			assertNull("No java element", typeBinding.getJavaElement());
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void test4() {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		try {
			parser.setEnvironment(null, null, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test5() {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		try {
			parser.setEnvironment(null, new String[] {}, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test6() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/X;";
		final IBinding[] bindings = new IBinding[2];

		String contents =
			"package p;\n" + 
			"public class X extends Y {\n" + 
			"	public int i;\n" + 
			"	public static void main(String[] args) {\n" + 
			"		int length = args.length;\n" + 
			"		System.out.println(length);\n" + 
			"	}\n" + 
			"}";
		
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" + 
			"public class Y {}";
		File fileY = new File(packageDir, "Y.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = file.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];
	
			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Ljava/lang/Object;"});
						for (int i = 0; i < temp.length; ++i) {
							bindings[i + 1] = temp[i];
						}
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};
	
			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);
	
			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);
	
			assertNotNull("No binding", bindings[0]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
			ITypeBinding typeBinding = (ITypeBinding) bindings[0];
			assertEquals("Wrong binding", "p.X", typeBinding.getQualifiedName());
			assertNull("No java element", typeBinding.getJavaElement());
			IPackageBinding packageBinding = typeBinding.getPackage();
			assertNull("No java element", packageBinding.getJavaElement());
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
			assertNotNull("No binding", bindings[1]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[1].getKind());
			typeBinding = (ITypeBinding) bindings[1];
			assertEquals("Wrong binding", "java.lang.Object", typeBinding.getQualifiedName());
		} finally {
			file.delete();
			fileY.delete();
		}
	}
}
