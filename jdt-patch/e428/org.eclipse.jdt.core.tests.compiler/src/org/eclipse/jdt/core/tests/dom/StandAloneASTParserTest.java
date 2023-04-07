/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class StandAloneASTParserTest extends AbstractRegressionTest {
	public StandAloneASTParserTest(String name) {
		super(name);
	}

	private static final int AST_JLS_LATEST = AST.getJLSLatest();

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
	protected File createFile(File dir, String fileName, String contents) throws IOException {
		File file = new File(dir, fileName);
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(contents);
		}
		return file;
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

	@Deprecated
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
	 * @deprecated
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
	@Deprecated
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
	@Deprecated
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
	public void testBug526996_002() {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		String contents =
						"public class zzei {\n"+
						"    private final Context mContext;\n"+
						"    private final String zzAg;\n"+
						"    private zzb<zzbb> zzAh;\n"+
						"    private zzb<zzbb> zzAi;\n"+
						"    private zze zzAj;\n"+
						"    private int zzAk;\n"+
						"    private final VersionInfoParcel zzpI;\n"+
						"    private final Object zzpK;\n"+
						"\n"+
						"    public interface zzb<T> {\n"+
						"        void zzc(T t);\n"+
						"    }\n"+
						"\n"+
						"    class zza {\n"+
						"        static int zzAu = 60000;\n"+
						"        static int zzAv = 10000;\n"+
						"    }\n"+
						"\n"+
						"    public class zzc<T> implements zzb<T> {\n"+
						"        public void zzc(T t) {\n"+
						"        }\n"+
						"    }\n"+
						"\n"+
						"    public class zzd extends zzjh<zzbe> {\n"+
						"        private final zze zzAw;\n"+
						"        private boolean zzAx;\n"+
						"        private final Object zzpK = new Object();\n"+
						"\n"+
						"        public zzd(zze com_google_android_gms_internal_zzei_zze) {\n"+
						"            this.zzAw = com_google_android_gms_internal_zzei_zze;\n"+
						"        }\n"+
						"\n"+
						"        public void release() {\n"+
						"            synchronized (this.zzpK) {\n"+
						"                if (this.zzAx) {\n"+
						"                    return;\n"+
						"                }\n"+
						"                this.zzAx = true;\n"+
						"                zza(new com.google.android.gms.internal.zzjg.zzc<zzbe>(this) {\n"+
						"                    final /* synthetic */ zzd zzAy;\n"+
						"\n"+
						"                    {\n"+
						"                        this.zzAy = r1;\n"+
						"                    }\n"+
						"\n"+
						"                    public void zzb(zzbe com_google_android_gms_internal_zzbe) {\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(\"Ending javascript session.\");\n"+
						"                        ((zzbf) com_google_android_gms_internal_zzbe).zzcs();\n"+
						"                    }\n"+
						"\n"+
						"                    public /* synthetic */ void zzc(Object obj) {\n"+
						"                        zzb((zzbe) obj);\n"+
						"                    }\n"+
						"                }, new com.google.android.gms.internal.zzjg.zzb());\n"+
						"                zza(new com.google.android.gms.internal.zzjg.zzc<zzbe>(this) {\n"+
						"                    final /* synthetic */ zzd zzAy;\n"+
						"\n"+
						"                    {\n"+
						"                        this.zzAy = r1;\n"+
						"                    }\n"+
						"\n"+
						"                    public void zzb(zzbe com_google_android_gms_internal_zzbe) {\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(\"Releasing engine reference.\");\n"+
						"                        this.zzAy.zzAw.zzek();\n"+
						"                    }\n"+
						"\n"+
						"                    public /* synthetic */ void zzc(Object obj) {\n"+
						"                        zzb((zzbe) obj);\n"+
						"                    }\n"+
						"                }, new com.google.android.gms.internal.zzjg.zza(this) {\n"+
						"                    final /* synthetic */ zzd zzAy;\n"+
						"\n"+
						"                    {\n"+
						"                        this.zzAy = r1;\n"+
						"                    }\n"+
						"\n"+
						"                    public void run() {\n"+
						"                        this.zzAy.zzAw.zzek();\n"+
						"                    }\n"+
						"                });\n"+
						"            }\n"+
						"        }\n"+
						"    }\n"+
						"\n"+
						"    public class zze extends zzjh<zzbb> {\n"+
						"        private int zzAA;\n"+
						"        private zzb<zzbb> zzAi;\n"+
						"        private boolean zzAz;\n"+
						"        private final Object zzpK = new Object();\n"+
						"\n"+
						"        public zze(zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb) {\n"+
						"            this.zzAi = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb;\n"+
						"            this.zzAz = false;\n"+
						"            this.zzAA = 0;\n"+
						"        }\n"+
						"\n"+
						"        public zzd zzej() {\n"+
						"            final zzd com_google_android_gms_internal_zzei_zzd = new zzd(this);\n"+
						"            synchronized (this.zzpK) {\n"+
						"                zza(new com.google.android.gms.internal.zzjg.zzc<zzbb>(this) {\n"+
						"                    final /* synthetic */ zze zzAC;\n"+
						"\n"+
						"                    public void zza(zzbb com_google_android_gms_internal_zzbb) {\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(\"Getting a new session for JS Engine.\");\n"+
						"                        com_google_android_gms_internal_zzei_zzd.zzg(com_google_android_gms_internal_zzbb.zzcq());\n"+
						"                    }\n"+
						"\n"+
						"                    public /* synthetic */ void zzc(Object obj) {\n"+
						"                        zza((zzbb) obj);\n"+
						"                    }\n"+
						"                }, new com.google.android.gms.internal.zzjg.zza(this) {\n"+
						"                    final /* synthetic */ zze zzAC;\n"+
						"\n"+
						"                    public void run() {\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(\"Rejecting reference for JS Engine.\");\n"+
						"                        com_google_android_gms_internal_zzei_zzd.reject();\n"+
						"                    }\n"+
						"                });\n"+
						"                zzx.zzaa(this.zzAA >= 0);\n"+
						"                this.zzAA++;\n"+
						"            }\n"+
						"            return com_google_android_gms_internal_zzei_zzd;\n"+
						"        }\n"+
						"\n"+
						"        protected void zzek() {\n"+
						"            boolean z = true;\n"+
						"            synchronized (this.zzpK) {\n"+
						"                if (this.zzAA < 1) {\n"+
						"                    z = false;\n"+
						"                }\n"+
						"                zzx.zzaa(z);\n"+
						"                com.google.android.gms.ads.internal.util.client.zzb.v(\"Releasing 1 reference for JS Engine\");\n"+
						"                this.zzAA--;\n"+
						"                zzem();\n"+
						"            }\n"+
						"        }\n"+
						"\n"+
						"        public void zzel() {\n"+
						"            boolean z = true;\n"+
						"            synchronized (this.zzpK) {\n"+
						"                if (this.zzAA < 0) {\n"+
						"                    z = false;\n"+
						"                }\n"+
						"                zzx.zzaa(z);\n"+
						"                com.google.android.gms.ads.internal.util.client.zzb.v(\"Releasing root reference. JS Engine will be destroyed once other references are released.\");\n"+
						"                this.zzAz = true;\n"+
						"                zzem();\n"+
						"            }\n"+
						"        }\n"+
						"\n"+
						"        protected void zzem() {\n"+
						"            synchronized (this.zzpK) {\n"+
						"                zzx.zzaa(this.zzAA >= 0);\n"+
						"                if (this.zzAz && this.zzAA == 0) {\n"+
						"                    com.google.android.gms.ads.internal.util.client.zzb.v(\"No reference is left (including root). Cleaning up engine.\");\n"+
						"                    zza(new com.google.android.gms.internal.zzjg.zzc<zzbb>(this) {\n"+
						"                        final /* synthetic */ zze zzAC;\n"+
						"\n"+
						"                        {\n"+
						"                            this.zzAC = r1;\n"+
						"                        }\n"+
						"\n"+
						"                        public void zza(final zzbb com_google_android_gms_internal_zzbb) {\n"+
						"                            zzip.runOnUiThread(new Runnable(this) {\n"+
						"                                final /* synthetic */ AnonymousClass3 zzAD;\n"+
						"\n"+
						"                                public void run() {\n"+
						"                                    this.zzAD.zzAC.zzAi.zzc(com_google_android_gms_internal_zzbb);\n"+
						"                                    com_google_android_gms_internal_zzbb.destroy();\n"+
						"                                }\n"+
						"                            });\n"+
						"                        }\n"+
						"\n"+
						"                        public /* synthetic */ void zzc(Object obj) {\n"+
						"                            zza((zzbb) obj);\n"+
						"                        }\n"+
						"                    }, new com.google.android.gms.internal.zzjg.zzb());\n"+
						"                } else {\n"+
						"                    com.google.android.gms.ads.internal.util.client.zzb.v(\"There are still references to the engine. Not destroying.\");\n"+
						"                }\n"+
						"            }\n"+
						"        }\n"+
						"    }\n"+
						"\n"+
						"    public zzei(Context context, VersionInfoParcel versionInfoParcel, String str) {\n"+
						"        this.zzpK = new Object();\n"+
						"        this.zzAk = 1;\n"+
						"        this.zzAg = str;\n"+
						"        this.mContext = context.getApplicationContext();\n"+
						"        this.zzpI = versionInfoParcel;\n"+
						"        this.zzAh = new zzc();\n"+
						"        this.zzAi = new zzc();\n"+
						"    }\n"+
						"\n"+
						"    public zzei(Context context, VersionInfoParcel versionInfoParcel, String str, zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb, zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb2) {\n"+
						"        this(context, versionInfoParcel, str);\n"+
						"        this.zzAh = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb;\n"+
						"        this.zzAi = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb2;\n"+
						"    }\n"+
						"\n"+
						"    private zze zzeg() {\n"+
						"        final zze com_google_android_gms_internal_zzei_zze = new zze(this.zzAi);\n"+
						"        zzip.runOnUiThread(new Runnable(this) {\n"+
						"            final /* synthetic */ zzei zzAm;\n"+
						"\n"+
						"            public void run() {\n"+
						"                final zzbb zza = this.zzAm.zza(this.zzAm.mContext, this.zzAm.zzpI);\n"+
						"                zza.zza(new com.google.android.gms.internal.zzbb.zza(this) {\n"+
						"                    final /* synthetic */ AnonymousClass1 zzAo;\n"+
						"\n"+
						"                    public void zzcr() {\n"+
						"                        zzip.zzKO.postDelayed(new Runnable(this) {\n"+
						"                            final /* synthetic */ AnonymousClass1 zzAp;\n"+
						"\n"+
						"                            {\n"+
						"                                this.zzAp = r1;\n"+
						"                            }\n"+
						"\n"+
						"                            /* JADX WARNING: inconsistent code. */\n"+
						"                            /* Code decompiled incorrectly, please refer to instructions dump. */\n"+
						"                            public void run() {\n"+
						"                                /*\n"+
						"                                r3 = this;\n"+
						"                                r0 = r3.zzAp;\n"+
						"                                r0 = r0.zzAo;\n"+
						"                                r0 = r0.zzAm;\n"+
						"                                r1 = r0.zzpK;\n"+
						"                                monitor-enter(r1);\n"+
						"                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0.getStatus();	 Catch:{ all -> 0x003f }\n"+
						"                                r2 = -1;\n"+
						"                                if (r0 == r2) throw GOTO_REPLACEMENT_1_L_0x0025;\n"+
						"                            L_0x0018:\n"+
						"                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0.getStatus();	 Catch:{ all -> 0x003f }\n"+
						"                                r2 = 1;\n"+
						"                                if (r0 != r2) throw GOTO_REPLACEMENT_2_L_0x0027;\n"+
						"                            L_0x0025:\n"+
						"                                monitor-exit(r1);	 Catch:{ all -> 0x003f }\n"+
						"                            L_0x0026:\n"+
						"                                return;\n"+
						"                            L_0x0027:\n"+
						"                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = r0;	 Catch:{ all -> 0x003f }\n"+
						"                                r0.reject();	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = new com.google.android.gms.internal.zzei$1$1$1$1;	 Catch:{ all -> 0x003f }\n"+
						"                                r0.<init>(r3);	 Catch:{ all -> 0x003f }\n"+
						"                                com.google.android.gms.internal.zzip.runOnUiThread(r0);	 Catch:{ all -> 0x003f }\n"+
						"                                r0 = \"Could not receive loaded message in a timely manner. Rejecting.\";\n"+
						"                                com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x003f }\n"+
						"                                monitor-exit(r1);	 Catch:{ all -> 0x003f }\n"+
						"                                throw GOTO_REPLACEMENT_3_L_0x0026;\n"+
						"                            L_0x003f:\n"+
						"                                r0 = move-exception;\n"+
						"                                monitor-exit(r1);	 Catch:{ all -> 0x003f }\n"+
						"                                throw r0;\n"+
						"                                */\n"+
						"                                throw new UnsupportedOperationException(\"Method not decompiled: com.google.android.gms.internal.zzei.1.1.1.run():void\");\n"+
						"                            }\n"+
						"                        }, (long) zza.zzAv);\n"+
						"                    }\n"+
						"                });\n"+
						"                zza.zza(\"/jsLoaded\", new zzdl(this) {\n"+
						"                    final /* synthetic */ AnonymousClass1 zzAo;\n"+
						"\n"+
						"                    /* JADX WARNING: inconsistent code. */\n"+
						"                    /* Code decompiled incorrectly, please refer to instructions dump. */\n"+
						"                    public void zza(com.google.android.gms.internal.zzjn r4, java.util.Map<java.lang.String, java.lang.String> r5) {\n"+
						"                        /*\n"+
						"                        r3 = this;\n"+
						"                        r0 = r3.zzAo;\n"+
						"                        r0 = r0.zzAm;\n"+
						"                        r1 = r0.zzpK;\n"+
						"                        monitor-enter(r1);\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.getStatus();	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = -1;\n"+
						"                        if (r0 == r2) throw GOTO_REPLACEMENT_4_L_0x001f;\n"+
						"                    L_0x0014:\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.getStatus();	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = 1;\n"+
						"                        if (r0 != r2) throw GOTO_REPLACEMENT_5_L_0x0021;\n"+
						"                    L_0x001f:\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }\n"+
						"                    L_0x0020:\n"+
						"                        return;\n"+
						"                    L_0x0021:\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = 0;\n"+
						"                        r0.zzAk = r2;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.zzAh;	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0.zzc(r2);	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0.zzg(r2);	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = r3.zzAo;	 Catch:{ all -> 0x0051 }\n"+
						"                        r2 = r0;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0.zzAj = r2;	 Catch:{ all -> 0x0051 }\n"+
						"                        r0 = \"Successfully loaded JS Engine.\";\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x0051 }\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }\n"+
						"                        throw GOTO_REPLACEMENT_6_L_0x0020;\n"+
						"                    L_0x0051:\n"+
						"                        r0 = move-exception;\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }\n"+
						"                        throw r0;\n"+
						"                        */\n"+
						"                        throw new UnsupportedOperationException(\"Method not decompiled: com.google.android.gms.internal.zzei.1.2.zza(com.google.android.gms.internal.zzjn, java.util.Map):void\");\n"+
						"                    }\n"+
						"                });\n"+
						"                final zziy com_google_android_gms_internal_zziy = new zziy();\n"+
						"                zzdl anonymousClass3 = new zzdl(this) {\n"+
						"                    final /* synthetic */ AnonymousClass1 zzAo;\n"+
						"\n"+
						"                    public void zza(zzjn com_google_android_gms_internal_zzjn, Map<String, String> map) {\n"+
						"                        synchronized (this.zzAo.zzAm.zzpK) {\n"+
						"                            com.google.android.gms.ads.internal.util.client.zzb.zzaG(\"JS Engine is requesting an update\");\n"+
						"                            if (this.zzAo.zzAm.zzAk == 0) {\n"+
						"                                com.google.android.gms.ads.internal.util.client.zzb.zzaG(\"Starting reload.\");\n"+
						"                                this.zzAo.zzAm.zzAk = 2;\n"+
						"                                this.zzAo.zzAm.zzeh();\n"+
						"                            }\n"+
						"                            zza.zzb(\"/requestReload\", (zzdl) com_google_android_gms_internal_zziy.get());\n"+
						"                        }\n"+
						"                    }\n"+
						"                };\n"+
						"                com_google_android_gms_internal_zziy.set(anonymousClass3);\n"+
						"                zza.zza(\"/requestReload\", anonymousClass3);\n"+
						"                if (this.zzAm.zzAg.endsWith(\".js\")) {\n"+
						"                    zza.zzs(this.zzAm.zzAg);\n"+
						"                } else if (this.zzAm.zzAg.startsWith(\"<html>\")) {\n"+
						"                    zza.zzu(this.zzAm.zzAg);\n"+
						"                } else {\n"+
						"                    zza.zzt(this.zzAm.zzAg);\n"+
						"                }\n"+
						"                zzip.zzKO.postDelayed(new Runnable(this) {\n"+
						"                    final /* synthetic */ AnonymousClass1 zzAo;\n"+
						"\n"+
						"                    /* JADX WARNING: inconsistent code. */\n"+
						"                    /* Code decompiled incorrectly, please refer to instructions dump. */\n"+
						"                    public void run() {\n"+
						"                        /*\n"+
						"                        r3 = this;\n"+
						"                        r0 = r3.zzAo;\n"+
						"                        r0 = r0.zzAm;\n"+
						"                        r1 = r0.zzpK;\n"+
						"                        monitor-enter(r1);\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = r0.getStatus();	 Catch:{ all -> 0x0037 }\n"+
						"                        r2 = -1;\n"+
						"                        if (r0 == r2) throw GOTO_REPLACEMENT_7_L_0x001f;\n"+
						"                    L_0x0014:\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = r0.getStatus();	 Catch:{ all -> 0x0037 }\n"+
						"                        r2 = 1;\n"+
						"                        if (r0 != r2) throw GOTO_REPLACEMENT_8_L_0x0021;\n"+
						"                    L_0x001f:\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }\n"+
						"                    L_0x0020:\n"+
						"                        return;\n"+
						"                    L_0x0021:\n"+
						"                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = r0;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0.reject();	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = new com.google.android.gms.internal.zzei$1$4$1;	 Catch:{ all -> 0x0037 }\n"+
						"                        r0.<init>(r3);	 Catch:{ all -> 0x0037 }\n"+
						"                        com.google.android.gms.internal.zzip.runOnUiThread(r0);	 Catch:{ all -> 0x0037 }\n"+
						"                        r0 = \"Could not receive loaded message in a timely manner. Rejecting.\";\n"+
						"                        com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x0037 }\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }\n"+
						"                        throw GOTO_REPLACEMENT_9_L_0x0020;\n"+
						"                    L_0x0037:\n"+
						"                        r0 = move-exception;\n"+
						"                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }\n"+
						"                        throw r0;\n"+
						"                        */\n"+
						"                        throw new UnsupportedOperationException(\"Method not decompiled: com.google.android.gms.internal.zzei.1.4.run():void\");\n"+
						"                    }\n"+
						"                }, (long) zza.zzAu);\n"+
						"            }\n"+
						"        });\n"+
						"        return com_google_android_gms_internal_zzei_zze;\n"+
						"    }\n"+
						"}\n";

		File file = new File(rootDir, "zzei.java");
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
						"public final class dm {\n"+
						"    private final byte[] a;\n"+
						"    private final boolean b;\n"+
						"    private int c;\n"+
						"    private int d;\n"+
						"    private int e;\n"+
						"    private final InputStream f;\n"+
						"    private int g;\n"+
						"    private boolean h;\n"+
						"    private int i;\n"+
						"    private int j;\n"+
						"    private int k;\n"+
						"    private int l;\n"+
						"    private int m;\n"+
						"    private a n;\n"+
						"\n"+
						"    public static dm a(byte[] bArr, int i) {\n"+
						"        dm dmVar = new dm(bArr, i);\n"+
						"        try {\n"+
						"            dmVar.b(i);\n"+
						"            return dmVar;\n"+
						"        } catch (Throwable e) {\n"+
						"            throw new IllegalArgumentException(e);\n"+
						"        }\n"+
						"    }\n"+
						"\n"+
						"    public final int a() {\n"+
						"        int i = 1;\n"+
						"        if (this.e != this.c || d(1)) {\n"+
						"            i = 0;\n"+
						"        }\n"+
						"        if (i != 0) {\n"+
						"            this.g = 0;\n"+
						"            return 0;\n"+
						"        }\n"+
						"        this.g = e();\n"+
						"        if (ed.b(this.g) != 0) {\n"+
						"            return this.g;\n"+
						"        }\n"+
						"        throw dr.d();\n"+
						"    }\n"+
						"\n"+
						"    public final void a(int i) {\n"+
						"        if (this.g != i) {\n"+
						"            throw dr.e();\n"+
						"        }\n"+
						"    }\n"+
						"\n"+
						"    public final dv a(dx dxVar, do doVar) {\n"+
						"        int e = e();\n"+
						"        if (this.k >= this.l) {\n"+
						"            throw dr.g();\n"+
						"        }\n"+
						"        int b = b(e);\n"+
						"        this.k++;\n"+
						"        dv dvVar = (dv) dxVar.a(this, doVar);\n"+
						"        a(0);\n"+
						"        this.k--;\n"+
						"        this.j = b;\n"+
						"        i();\n"+
						"        return dvVar;\n"+
						"    }\n"+
						"\n"+
						"    /* JADX WARNING: inconsistent code. */\n"+
						"    /* Code decompiled incorrectly, please refer to instructions dump. */\n"+
						"    public final int e() {\n"+
						"        /*\n"+
						"        r8 = this;\n"+
						"        r6 = 0;\n"+
						"        r0 = r8.e;\n"+
						"        r1 = r8.c;\n"+
						"        if (r1 == r0) throw GOTO_REPLACEMENT_1_L_0x0081;\n"+
						"    L_0x0008:\n"+
						"        r3 = r8.a;\n"+
						"        r2 = r0 + 1;\n"+
						"        r0 = r3[r0];\n"+
						"        if (r0 < 0) throw GOTO_REPLACEMENT_2_L_0x0013;\n"+
						"    L_0x0010:\n"+
						"        r8.e = r2;\n"+
						"    L_0x0012:\n"+
						"        return r0;\n"+
						"    L_0x0013:\n"+
						"        r1 = r8.c;\n"+
						"        r1 = r1 - r2;\n"+
						"        r4 = 9;\n"+
						"        if (r1 < r4) throw GOTO_REPLACEMENT_3_L_0x0081;\n"+
						"    L_0x001a:\n"+
						"        r1 = r2 + 1;\n"+
						"        r2 = r3[r2];\n"+
						"        r2 = r2 << 7;\n"+
						"        r0 = r0 ^ r2;\n"+
						"        r4 = (long) r0;\n"+
						"        r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_4_L_0x002e;\n"+
						"    L_0x0026:\n"+
						"        r2 = (long) r0;\n"+
						"        r4 = -128; // 0xffffffffffffff80 float:NaN double:NaN;\n"+
						"        r2 = r2 ^ r4;\n"+
						"        r0 = (int) r2;\n"+
						"    L_0x002b:\n"+
						"        r8.e = r1;\n"+
						"        throw GOTO_REPLACEMENT_5_L_0x0012;\n"+
						"    L_0x002e:\n"+
						"        r2 = r1 + 1;\n"+
						"        r1 = r3[r1];\n"+
						"        r1 = r1 << 14;\n"+
						"        r0 = r0 ^ r1;\n"+
						"        r4 = (long) r0;\n"+
						"        r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));\n"+
						"        if (r1 < 0) throw GOTO_REPLACEMENT_6_L_0x0041;\n"+
						"    L_0x003a:\n"+
						"        r0 = (long) r0;\n"+
						"        r4 = 16256; // 0x3f80 float:2.278E-41 double:8.0315E-320;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        r0 = (int) r0;\n"+
						"        r1 = r2;\n"+
						"        throw GOTO_REPLACEMENT_7_L_0x002b;\n"+
						"    L_0x0041:\n"+
						"        r1 = r2 + 1;\n"+
						"        r2 = r3[r2];\n"+
						"        r2 = r2 << 21;\n"+
						"        r0 = r0 ^ r2;\n"+
						"        r4 = (long) r0;\n"+
						"        r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_8_L_0x0054;\n"+
						"    L_0x004d:\n"+
						"        r2 = (long) r0;\n"+
						"        r4 = -2080896; // 0xffffffffffe03f80 float:NaN double:NaN;\n"+
						"        r2 = r2 ^ r4;\n"+
						"        r0 = (int) r2;\n"+
						"        throw GOTO_REPLACEMENT_9_L_0x002b;\n"+
						"    L_0x0054:\n"+
						"        r2 = r1 + 1;\n"+
						"        r1 = r3[r1];\n"+
						"        r4 = r1 << 28;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        r4 = (long) r0;\n"+
						"        r6 = 266354560; // 0xfe03f80 float:2.2112565E-29 double:1.315966377E-315;\n"+
						"        r4 = r4 ^ r6;\n"+
						"        r0 = (int) r4;\n"+
						"        if (r1 >= 0) throw GOTO_REPLACEMENT_10_L_0x0087;\n"+
						"    L_0x0063:\n"+
						"        r1 = r2 + 1;\n"+
						"        r2 = r3[r2];\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_11_L_0x002b;\n"+
						"    L_0x0069:\n"+
						"        r2 = r1 + 1;\n"+
						"        r1 = r3[r1];\n"+
						"        if (r1 >= 0) throw GOTO_REPLACEMENT_12_L_0x0087;\n"+
						"    L_0x006f:\n"+
						"        r1 = r2 + 1;\n"+
						"        r2 = r3[r2];\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_13_L_0x002b;\n"+
						"    L_0x0075:\n"+
						"        r2 = r1 + 1;\n"+
						"        r1 = r3[r1];\n"+
						"        if (r1 >= 0) throw GOTO_REPLACEMENT_14_L_0x0087;\n"+
						"    L_0x007b:\n"+
						"        r1 = r2 + 1;\n"+
						"        r2 = r3[r2];\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_15_L_0x002b;\n"+
						"    L_0x0081:\n"+
						"        r0 = r8.h();\n"+
						"        r0 = (int) r0;\n"+
						"        throw GOTO_REPLACEMENT_16_L_0x0012;\n"+
						"    L_0x0087:\n"+
						"        r1 = r2;\n"+
						"        throw GOTO_REPLACEMENT_17_L_0x002b;\n"+
						"        */\n"+
						"        throw new UnsupportedOperationException(\"Method not decompiled: com.tapjoy.internal.dm.e():int\");\n"+
						"    }\n"+
						"\n"+
						"    /* JADX WARNING: inconsistent code. */\n"+
						"    /* Code decompiled incorrectly, please refer to instructions dump. */\n"+
						"    public final long f() {\n"+
						"        /*\n"+
						"        r10 = this;\n"+
						"        r8 = 0;\n"+
						"        r0 = r10.e;\n"+
						"        r1 = r10.c;\n"+
						"        if (r1 == r0) throw GOTO_REPLACEMENT_18_L_0x00bb;\n"+
						"    L_0x0008:\n"+
						"        r4 = r10.a;\n"+
						"        r1 = r0 + 1;\n"+
						"        r0 = r4[r0];\n"+
						"        if (r0 < 0) throw GOTO_REPLACEMENT_19_L_0x0014;\n"+
						"    L_0x0010:\n"+
						"        r10.e = r1;\n"+
						"        r0 = (long) r0;\n"+
						"    L_0x0013:\n"+
						"        return r0;\n"+
						"    L_0x0014:\n"+
						"        r2 = r10.c;\n"+
						"        r2 = r2 - r1;\n"+
						"        r3 = 9;\n"+
						"        if (r2 < r3) throw GOTO_REPLACEMENT_20_L_0x00bb;\n"+
						"    L_0x001b:\n"+
						"        r2 = r1 + 1;\n"+
						"        r1 = r4[r1];\n"+
						"        r1 = r1 << 7;\n"+
						"        r0 = r0 ^ r1;\n"+
						"        r0 = (long) r0;\n"+
						"        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r3 >= 0) throw GOTO_REPLACEMENT_21_L_0x002d;\n"+
						"    L_0x0027:\n"+
						"        r4 = -128; // 0xffffffffffffff80 float:NaN double:NaN;\n"+
						"        r0 = r0 ^ r4;\n"+
						"    L_0x002a:\n"+
						"        r10.e = r2;\n"+
						"        throw GOTO_REPLACEMENT_22_L_0x0013;\n"+
						"    L_0x002d:\n"+
						"        r3 = r2 + 1;\n"+
						"        r2 = r4[r2];\n"+
						"        r2 = r2 << 14;\n"+
						"        r6 = (long) r2;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r2 < 0) throw GOTO_REPLACEMENT_23_L_0x003e;\n"+
						"    L_0x0039:\n"+
						"        r4 = 16256; // 0x3f80 float:2.278E-41 double:8.0315E-320;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        r2 = r3;\n"+
						"        throw GOTO_REPLACEMENT_24_L_0x002a;\n"+
						"    L_0x003e:\n"+
						"        r2 = r3 + 1;\n"+
						"        r3 = r4[r3];\n"+
						"        r3 = r3 << 21;\n"+
						"        r6 = (long) r3;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r3 >= 0) throw GOTO_REPLACEMENT_25_L_0x004f;\n"+
						"    L_0x004a:\n"+
						"        r4 = -2080896; // 0xffffffffffe03f80 float:NaN double:NaN;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        throw GOTO_REPLACEMENT_26_L_0x002a;\n"+
						"    L_0x004f:\n"+
						"        r3 = r2 + 1;\n"+
						"        r2 = r4[r2];\n"+
						"        r6 = (long) r2;\n"+
						"        r2 = 28;\n"+
						"        r6 = r6 << r2;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r2 < 0) throw GOTO_REPLACEMENT_27_L_0x0062;\n"+
						"    L_0x005c:\n"+
						"        r4 = 266354560; // 0xfe03f80 float:2.2112565E-29 double:1.315966377E-315;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        r2 = r3;\n"+
						"        throw GOTO_REPLACEMENT_28_L_0x002a;\n"+
						"    L_0x0062:\n"+
						"        r2 = r3 + 1;\n"+
						"        r3 = r4[r3];\n"+
						"        r6 = (long) r3;\n"+
						"        r3 = 35;\n"+
						"        r6 = r6 << r3;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r3 >= 0) throw GOTO_REPLACEMENT_29_L_0x0076;\n"+
						"    L_0x006f:\n"+
						"        r4 = -34093383808; // 0xfffffff80fe03f80 float:2.2112565E-29 double:NaN;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        throw GOTO_REPLACEMENT_30_L_0x002a;\n"+
						"    L_0x0076:\n"+
						"        r3 = r2 + 1;\n"+
						"        r2 = r4[r2];\n"+
						"        r6 = (long) r2;\n"+
						"        r2 = 42;\n"+
						"        r6 = r6 << r2;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r2 < 0) throw GOTO_REPLACEMENT_31_L_0x008b;\n"+
						"    L_0x0083:\n"+
						"        r4 = 4363953127296; // 0x3f80fe03f80 float:2.2112565E-29 double:2.1560793202584E-311;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        r2 = r3;\n"+
						"        throw GOTO_REPLACEMENT_32_L_0x002a;\n"+
						"    L_0x008b:\n"+
						"        r2 = r3 + 1;\n"+
						"        r3 = r4[r3];\n"+
						"        r6 = (long) r3;\n"+
						"        r3 = 49;\n"+
						"        r6 = r6 << r3;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r3 >= 0) throw GOTO_REPLACEMENT_33_L_0x009f;\n"+
						"    L_0x0098:\n"+
						"        r4 = -558586000294016; // 0xfffe03f80fe03f80 float:2.2112565E-29 double:NaN;\n"+
						"        r0 = r0 ^ r4;\n"+
						"        throw GOTO_REPLACEMENT_34_L_0x002a;\n"+
						"    L_0x009f:\n"+
						"        r3 = r2 + 1;\n"+
						"        r2 = r4[r2];\n"+
						"        r6 = (long) r2;\n"+
						"        r2 = 56;\n"+
						"        r6 = r6 << r2;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r6 = 71499008037633920; // 0xfe03f80fe03f80 float:2.2112565E-29 double:6.838959413692434E-304;\n"+
						"        r0 = r0 ^ r6;\n"+
						"        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));\n"+
						"        if (r2 >= 0) throw GOTO_REPLACEMENT_35_L_0x00c1;\n"+
						"    L_0x00b2:\n"+
						"        r2 = r3 + 1;\n"+
						"        r3 = r4[r3];\n"+
						"        r4 = (long) r3;\n"+
						"        r3 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));\n"+
						"        if (r3 >= 0) throw GOTO_REPLACEMENT_36_L_0x002a;\n"+
						"    L_0x00bb:\n"+
						"        r0 = r10.h();\n"+
						"        throw GOTO_REPLACEMENT_37_L_0x0013;\n"+
						"    L_0x00c1:\n"+
						"        r2 = r3;\n"+
						"        throw GOTO_REPLACEMENT_38_L_0x002a;\n"+
						"        */\n"+
						"        throw new UnsupportedOperationException(\"Method not decompiled: com.tapjoy.internal.dm.f():long\");\n"+
						"    }\n"+
						"\n"+
						"    private long h() {\n"+
						"        long j = 0;\n"+
						"        for (int i = 0; i < 64; i += 7) {\n"+
						"            if (this.e == this.c) {\n"+
						"                c(1);\n"+
						"            }\n"+
						"            byte[] bArr = this.a;\n"+
						"            int i2 = this.e;\n"+
						"            this.e = i2 + 1;\n"+
						"            byte b = bArr[i2];\n"+
						"            j |= ((long) (b & 127)) << i;\n"+
						"            if ((b & 128) == 0) {\n"+
						"                return j;\n"+
						"            }\n"+
						"        }\n"+
						"        throw dr.c();\n"+
						"    }\n"+
						"}\n";

		File fileY = new File(rootDir, "dm.java");
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
				}
			};
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(new String[0], new String[] { rootDir.getAbsolutePath() }, null, true);
		    String[] files = null;
			try {
				files = new String[] {file.getCanonicalPath(), fileY.getCanonicalPath()};
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
	public void testBug530299_001() {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var x = new X();\n" +
				"       for (var i = 0; i < 10; ++i) {}\n" +
				"	}\n" +
				"}";
	    ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
	    parser.setSource(contents.toCharArray());
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, new String[] {null}, null, true);
		parser.setResolveBindings(true);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
		parser.setCompilerOptions(options);
		ASTNode node = parser.createAST(null);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit cu = (CompilationUnit) node;
		assertTrue("Problems in compilation", cu.getProblems().length == 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
		Type type = vStmt.getType();
		assertNotNull(type);
		assertTrue("not a var", type.isVar());
	}
	public void testBug482254() throws IOException {
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

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File fileY = new File(packageDir, "Y.java");
		String canonicalPath = fileY.getCanonicalPath();

		packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		fileY = new File(packageDir, "Z.java");
		String canonicalPath2 = fileY.getCanonicalPath();

		contents =
				"enum X {\n" +
				"              /** */\n" +
				"    FOO\n" +
				"}";

			File file2 = new File(rootDir, "X.java");
			writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file2));
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

		try {
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.createASTs(
					new String[] { file.getCanonicalPath(), canonicalPath, canonicalPath2, file2.getCanonicalPath() },
					null,
					new String[] {},
					new FileASTRequestor() {},
					null);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	/*
	 * To test isVar returning false for ast level 10 and compliance 9
	 */
	public void testBug533210_0001() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public static void main(String[] args) {\n" +
				"		var s = new Y();\n" +
				"	}\n" +
				"}\n" +
				"class Y {}";

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
			CompilationUnit cu = (CompilationUnit) node;
			TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
			MethodDeclaration[] methods = typeDeclaration.getMethods();
			MethodDeclaration methodDeclaration = methods[0];
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			Type type = vStmt.getType();
			SimpleType simpleType = (SimpleType) type;
			assertFalse("A var", simpleType.isVar());
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			assertFalse("A var", simpleName.isVar());
	}
	// no longer a preview feature, test is not relevant
	@Deprecated
	public void _testBug545383_01() throws JavaModelException {
		String contents =
				"class X {\n"+
				"	public static int foo(int i) {\n"+
				"		int result = switch (i) {\n"+
				"		case 1 -> {break 5;}\n"+
				"		default -> 0;\n"+
				"		};\n"+
				"		return result;\n"+
				"	}\n"+
				"}\n";

		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(false);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_12);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_12);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit cu = (CompilationUnit) node;
		IProblem[] problems = cu.getProblems();
		assertTrue(problems.length > 0);
		assertTrue(problems[0].toString().contains("preview"));
	}
	public void testBug547900_01() throws JavaModelException {
		String contents =
				"class X {\n"+
				"	public static int foo(int i) {\n"+
				"		int result = switch (i) {\n"+
				"		case 1 -> {yield 5;}\n"+
				"		default -> 0;\n"+
				"		};\n"+
				"		return result;\n"+
				"	}\n"+
				"}\n";

		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(false);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit cu = (CompilationUnit) node;
		TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		VariableDeclarationStatement stmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		SwitchExpression se = (SwitchExpression) fragment.getInitializer();
		YieldStatement yieldStatement = (YieldStatement) ((Block)se.statements().get(1)).statements().get(0);
		assertNotNull("Expression null", yieldStatement.getExpression());
	}
	public void testBug558517() throws IOException {
		File f1 = null, f2 = null, packDir = null;
		try {
			File rootDir = new File(System.getProperty("java.io.tmpdir"));
			packDir = new File(rootDir, "P/src/x");
			packDir.mkdirs();

			String fileName1 = "EnsureImpl$1.java";
			String fileName2 = "C9947f.java";
			f1 = createFile(
					packDir, fileName1,
					"package x;\n" +
					"\n" +
					"class EnsureImpl$1 {\n" +
					"}\n");
			f2 = createFile(
					packDir, fileName2,
					"package x;\n" +
					"public final class C9947f {\n" +
					"    public C9947f() {\n" +
					"        try {\n" +
					"            new x.EnsureImpl$1();\n" +
					"        } catch (Throwable unused) {\n" +
					"        }\n" +
					"    }\n" +
					"}\n");
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setResolveBindings(true);
			Map<String, String> options = new HashMap<>();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options );
			parser.setEnvironment(null,
					new String[] { rootDir + "/P/src" },
					null,
					true);
			parser.createASTs(new String[] { packDir + "/" + fileName1, packDir + "/" + fileName2 },
					null,
					new String[] { "Lx/C9947f;" },
					new FileASTRequestor() {
					},
					null);
			// just ensure the above doesn't throw NPE
		} finally {
			f1.delete();
			f2.delete();
			packDir.delete();
		}
	}
}