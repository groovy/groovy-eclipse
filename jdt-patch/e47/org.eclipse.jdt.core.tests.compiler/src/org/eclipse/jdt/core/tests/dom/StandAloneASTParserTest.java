/*******************************************************************************
 * Copyright (c) 2010, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 461250 - ArrayIndexOutOfBoundsException in SourceTypeBinding.fields
 *     Carmi Grushko - Bug 465048 - Binding is null for class literals in synchronized blocks
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
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
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;

@SuppressWarnings({ "rawtypes" })
public class StandAloneASTParserTest extends AbstractRegressionTest {
	public StandAloneASTParserTest(String name) {
		super(name);
	}
	
	private static final int AST_JLS_LATEST = AST.JLS9;

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
	public void testBug529654_001() {
		String contents =
				"module m {\n" + 
				"}";
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName("module-info.java");
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) node;
		ModuleDeclaration module = unit.getModule();
		assertTrue("Incorrect Module Name", module.getName().getFullyQualifiedName().equals("m"));
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
		ASTNode node = runConversion(AST_JLS_LATEST, contents, true, true, true, "p/X.java");
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
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
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
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
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
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		try {
			parser.setEnvironment(null, null, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test5() {
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		try {
			parser.setEnvironment(null, new String[] {}, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test6() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
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
	
	/**
	 * @deprecated
	 * @throws IOException
	 */
	public void testBug415066_001() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/C;";
		final IBinding[] bindings = new IBinding[2];

		String contents = 
			"package p;\n" + 
			"public class A{}\n" + 
			"class B{}";
		
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "A.java");
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
			"public class C extends B {}";
		File fileY = new File(packageDir, "C.java");
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
			final String canonicalPath = fileY.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];
	
			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Lp/C;"});
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
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 0;
			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	/**
	 * Negative test case
	 * @deprecated
	 * @throws IOException
	 */
	public void testBug415066_002() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/C;";
		final IBinding[] bindings = new IBinding[2];

		String contents = 
			"package p;\n" + 
			"public class A{}\n" + 
			"class B{}";
		
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "A.java");
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
			"package q;\n" +
			"import p.*;\n" +
			"public class C extends B {}";
		File fileY = new File(packageDir, "C.java");
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
			final String canonicalPath = fileY.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];
	
			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Lq/C;"});
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
			assertNotNull("No ast", units[0]);
			IProblem[] problems = units[0].getProblems();
			assertEquals("No problem", 1, problems.length);
			assertEquals("Pb(3) The type B is not visible", problems[0].toString());
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void test7() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));

		String contents =
			"enum X {\n" + 
			"              /** */\n" + 
			"    FOO\n" + 
			"}";
		
		File file = new File(rootDir, "X.java");
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
			"class Y {}";
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
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
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.createASTs(
					new String[] { file.getCanonicalPath(), fileY.getCanonicalPath() },
					null,
					new String[] {},
					new FileASTRequestor() {},
					null);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void testBug461250() {
		String source =
				"class QH<T> implements QR.Q {\n" +
				"  QR.Q q;\n" +
				"  @V(v = A, d = \"\") Map p;\n" +
				"}\n";
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(true);
		String[] emptyStringArray = new String[0];
		parser.setEnvironment(emptyStringArray, emptyStringArray, emptyStringArray, true /* includeRunningVMBootclasspath */);
		parser.setUnitName("dontCare");
		ASTNode ast = parser.createAST(null);
		assertTrue("should have parsed a CUD", ast instanceof CompilationUnit);
	}

	public void testBug465048() {
		String source =
				"class A {\n" +
				"  void f(OtherClass otherClass) {\n" +
				"    synchronized (otherClass) {\n" +
				"      Class c = InnerClass.class;\n" +  // Line = 4
				"    }\n" +
				"  }\n" +
				"  class InnerClass { }\n" +
				"}\n";
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(true);
		String[] emptyStringArray = new String[0];
		parser.setEnvironment(emptyStringArray, emptyStringArray, emptyStringArray,
				true /* includeRunningVMBootclasspath */);
		parser.setUnitName("dontCare");

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		SimpleName innerClassLiteral = (SimpleName) NodeFinder.perform(cu, cu.getPosition(4, 16), 1 /* length */);
		ITypeBinding innerClassBinding = (ITypeBinding) innerClassLiteral.resolveBinding();

		assertEquals("InnerClass", innerClassBinding.getName());
	}

	/**
	 * Verifies that ASTParser doesn't throw an IllegalArgumentException when given
	 * this valid input.
	 */
	public void testBug480545() {
	    String input = "class Test2 { void f(Test2... xs) {} }";
	    ASTParser parser = ASTParser.newParser(AST.JLS9);
	    parser.setSource(input.toCharArray());
	    Map<String, String> options = JavaCore.getOptions();
	    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
	    parser.setCompilerOptions(options);
	    assertNotNull(parser.createAST(null));
	}
	public void testBug493336_001() {
	    String input = "public class X implements á¼³ {\n" +
	    			   "  public static final class if {\n"+
                       "    public static final if ËŠ = new if(null, null, null, null);\n"+
                       "  }\n" +
                        "}";	    		
	    ASTParser parser = ASTParser.newParser(AST.JLS9);
	    parser.setSource(input.toCharArray());
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, new String[] {null}, null, true);
		
		Hashtable<String, String> options1 = JavaCore.getDefaultOptions();
		options1.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
	    options1.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
	    options1.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
	    parser.setCompilerOptions(options1);
	    assertNotNull(parser.createAST(null));
	}
	public void testBug526996_001() {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		String contents = 
				"public class X {\n" +
				"    public X() {\n" +
				"        this.f16132b =\n" +
				"/*\n" +
				"        at jadx.api.JavaClass.decompile(JavaClass.java:62)\n" +
				"*/\n" +
				"\n" +
				"            /* JADX WARNING: inconsistent code. */\n" +
				"            /* Code decompiled incorrectly, please refer to instructions dump. */\n" +
				"            public final C1984r m22030a() {\n" +
				"            }\n" +
				"        }\n" +
				"\n";
		
		File file = new File(rootDir, "X.java");
		Writer writer = null;
		try {
			try {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(contents);
			} catch (IOException e1) {
				// ignore
			}
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
				"public class Y {\n" +
				"\n" +
				"    /* JADX WARNING: inconsistent code. */\n" +
				"    protected void finalize() {\n" +
				"        for (i =\n" +
				"/*\n" +
				"        at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)\n" +
				"*/\n" +
				"        public void close() { }\n" +
				"    }\n" ;

		File fileY = new File(rootDir, "Y.java");
		Writer writer2 = null;
		try {
			try {
				writer2 = new BufferedWriter(new FileWriter(fileY));
				writer2.write(contents2);
			} catch (IOException e) {
				// ignore
			}
		} finally {
			try {
				if (writer2 != null) writer2.close();
			} catch(IOException e) {
				// ignore
			}
		}
		try {
			final FileASTRequestor astRequestor = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					super.acceptAST(sourceFilePath, ast);
					System.out.println(sourceFilePath);
				}
			};
			ASTParser parser = ASTParser.newParser(AST.JLS9);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(new String[0], new String[] { rootDir.getAbsolutePath() }, null, true);
		    String[] files = null;
			try {
				files = new String[] {file.getCanonicalPath(), fileY.getCanonicalPath()};
				System.out.println("Building...");
				parser.createASTs(files,
						null,
						new String[0],
						astRequestor,
						null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			file.delete();
			fileY.delete();
		}
	}
}