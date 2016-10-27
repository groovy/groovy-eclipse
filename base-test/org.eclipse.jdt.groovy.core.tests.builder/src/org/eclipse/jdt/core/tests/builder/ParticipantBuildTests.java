/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import java.io.*;
import java.util.*;

import junit.framework.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.tests.util.Util;

public class ParticipantBuildTests extends BuilderTests {
	public ParticipantBuildTests(String name) {
		super(name);
	}

	public void tearDown() throws Exception {
		TestBuilderParticipant.PARTICIPANT = null;
		super.tearDown();
	}

	public static Test suite() {
		return buildTestSuite(ParticipantBuildTests.class);
	}

	static class BuildTestParticipant extends CompilationParticipant {
		BuildTestParticipant() {
			TestBuilderParticipant.PARTICIPANT = this;
		}
	}

	static class ParticipantProblem extends CategorizedProblem {
		int counter = 0;
		String message;
		int id;
		char[] filename;
		ParticipantProblem(String message, String filename) {
			this.message = message;
			id = counter ++;
			this.filename = filename.toCharArray();
		}
		public String[] getArguments() { return new String[0]; }
		public int getID() { return id; }
		public String getMessage() { return message; }
		public char[] getOriginatingFileName() { return filename; }
		public int getSourceStart() { return 0; }
		public int getSourceEnd() { return 0; }
		public int getSourceLineNumber() { return 1; }
		public boolean isError() { return true; }
		public boolean isWarning() { return false; }
		public void setSourceEnd(int sourceEnd) {/* not needed */}
		public void setSourceLineNumber(int lineNumber)  {/* not needed */}
		public void setSourceStart(int sourceStart) {/* not needed */}
		public int getCategoryID() { return 0; }
		public String getMarkerType() { return "org.eclipse.jdt.core.tests.compile.problem"; }
	}

	CompilationUnit buildCompilationUnit(BuildContext file) {
		IJavaProject javaProject = JavaCore.create(file.getFile().getProject());
		ASTParser p = ASTParser.newParser(AST.JLS3);
		p.setProject(javaProject);
		p.setSource(file.getContents());
		p.setResolveBindings(true);
		p.setKind(ASTParser.K_COMPILATION_UNIT);
		p.setUnitName(file.getFile().getName());
		return (CompilationUnit) p.createAST(null);
	}

	public void testBuildStarting() throws JavaModelException {
		IPath projectPath = env.addProject("Project");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		IPath test = env.addClass(root, "", "Test",
			"public class Test extends GeneratedType {}\n"
		);

		// install compilationParticipant
		new BuildTestParticipant() {
			int buildPass = 0;
			public void buildStarting(BuildContext[] files, boolean isBatchBuild) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java"));
				if (this.buildPass == 0 || this.buildPass == 3) {
					try {
						genedType.create(new ByteArrayInputStream("public class GeneratedType {}".getBytes()), true, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				} else if (this.buildPass == 1) {
					try {
						genedType.delete(true, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordDeletedGeneratedFiles(new IFile[] {genedType});
				}
				this.buildPass++;
			}
		};
		incrementalBuild(projectPath);
		expectingNoProblems();

		// GeneratedType will be deleted
		env.addClass(root, "", "Test",
			"public class Test extends GeneratedType {}\n"
		);
		incrementalBuild(projectPath);
		expectingOnlySpecificProblemFor(test, new Problem("", "GeneratedType cannot be resolved to a type", test, 26, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		// GeneratedType will be recreated
		env.addClass(root, "", "Test",
			"public class Test extends GeneratedType {}\n"
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testDefaultValue() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "test", "EntryPoint",
			"package test;\n" +
			"public class EntryPoint { ClassWithNestedAnnotation nestedAnnotation; }"
			);

		env.addClass(root, "test", "ClassWithNestedAnnotation",
			"package test;\n" +
			"public class ClassWithNestedAnnotation {\n" +
			"	public final int FOUR = 4; \n " +
			"	public @interface NestedAnnotation {\n" +
			"		public enum Character { Winnie, Tiger, Piglet, Eore; }\n" +
			"		Character value() default Character.Eore; \n" +
			"	}\n" +
			"}"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				for (int i = 0, total = files.length; i < total; i++) {
					IFile file = files[i].getFile();
					// Traversing the members of test.ClassWithNestedAnnotation through a reference in EntryPoint.java
					if (!"EntryPoint.java".equals(file.getName())) continue;

					List<IProblem> problems = new ArrayList<IProblem>();
					CompilationUnit unit = buildCompilationUnit(files[i]);
					List<?> types = unit.types();
					for (int t = 0, l = types.size(); t < l; t++) {
						AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) types.get(t);
						ITypeBinding typeBinding = typeDecl.resolveBinding();
						if (typeBinding == null) continue;
						IVariableBinding[] fieldBindings = typeBinding.getDeclaredFields();
						for (int f = 0, fLength = fieldBindings.length; f < fLength; f++) {
							IVariableBinding field = fieldBindings[f];
							if ("nestedAnnotation".equals(field.getName())) {
								ITypeBinding fieldType = field.getType();
								ITypeBinding[] declaredTypes = fieldType.getDeclaredTypes();
								for (int d = 0, dLength = declaredTypes.length; d < dLength; d++) {
									if (!"NestedAnnotation".equals(declaredTypes[d].getName())) continue;
									IMethodBinding[] annotationMethods = declaredTypes[d].getDeclaredMethods();
									for (int m = 0, mLength = annotationMethods.length; m < mLength; m++) {
										if (!"value".equals(annotationMethods[m].getName())) continue;
										Object defaultValue = annotationMethods[m].getDefaultValue();
										assertTrue("Wrong class", defaultValue instanceof IVariableBinding);
										IVariableBinding variableBinding = (IVariableBinding) defaultValue;
										String defaultString = variableBinding.getName();
										String expected = "Eore";
										if (!expected.equals(defaultString)) {
											IProblem problem = new ParticipantProblem("expecting default = " + expected + " not " + defaultString, file.getName());
											problems.add(problem);
										}
									}
								}
								IVariableBinding[] nestedFields = fieldType.getDeclaredFields();
								for (int nf = 0, nfLength = nestedFields.length; nf < nfLength; nf++) {
									if (!nestedFields[nf].getName().equals("FOUR")) continue;
									Object constant = nestedFields[nf].getConstantValue();
									String constantStr = constant == null ? "" : constant.toString();
									String expected = "4";
									if (!constantStr.equals(expected))
										problems.add(new ParticipantProblem("expecting constant = " + expected + " not " + constantStr, file.getName()));
								}
							} else {
								problems.add(new ParticipantProblem("found unexpected field " + field, file.getName()));
							}
						}
					}
					if (!problems.isEmpty()) {
						CategorizedProblem[] problemArray = new CategorizedProblem[problems.size()];
						problemArray = problems.toArray(problemArray);
						files[i].recordNewProblems(problemArray);
					}
				}
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	/*
	 * Ensure that participants problems are correctly managed by the Java builder
	 * (regression test for bug 134345 Problems from CompilationParticipants do not get cleaned up unless there are Java errors)
	 */
	public void testParticipantProblems() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X { /* generate problem*/ }"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public void buildStarting(BuildContext[] files, boolean isBatch) {
				for (int i = 0, total = files.length; i < total; i++) {
					BuildContext context = files[i];
					if (CharOperation.indexOf("generate problem".toCharArray(), context.getContents(), true) != -1) {
						context.recordNewProblems(new CategorizedProblem[] {new ParticipantProblem("Participant problem", context.getFile().getFullPath().toString())});
					}
				}
			}
		};

		fullBuild(projectPath);
		expectingParticipantProblems(projectPath, "Participant problem");

		env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X { }"
			);
		incrementalBuild(projectPath);
		expectingParticipantProblems(projectPath, "");
	}

	public void testProcessAnnotationDeclarations() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Test",
			"@interface TestAnnotation {}\n" +
			"public class Test extends GeneratedType {}\n"
			);

		env.addClass(root, "", "Other",
			"public class Other { MissingAnnotation m; }\n"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			int count = 2;
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				if (this.count == 2) {
					this.count--;
					BuildContext result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("MissingAnnotation.java"));
					try {
						genedType.create(new ByteArrayInputStream("public @interface MissingAnnotation {}".getBytes()), true, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				} else if (this.count == 1) {
					this.count--;
					BuildContext result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java"));
					try {
						genedType.create(new ByteArrayInputStream("public class GeneratedType {}".getBytes()), true, null);
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				}
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationQualifiedReferences() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p1", "Test",
			"package p1;\n" +
			"@GeneratedAnnotation\n" +
			"public class Test { public void method() { p1.p2.GeneratedType.method(); } }\n"
			);

		env.addClass(root, "p1", "GeneratedAnnotation",
			"package p1;\n" +
			"@interface GeneratedAnnotation{}\n"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getProject().getFile(new Path("src/p1/p2/GeneratedType.java"));
				if (genedType.exists()) return;
				try {
					IFolder folder = (IFolder) genedType.getParent();
					if(!folder.exists())
						folder.create(true, true, null);
					genedType.create(new ByteArrayInputStream("package p1.p2; public class GeneratedType { public static void method(){} }".getBytes()), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationReferences() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Test",
			"@GeneratedAnnotation\n" +
			"public class Test {}\n"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedAnnotation.java"));
				if (genedType.exists()) return;
				try {
					genedType.create(new ByteArrayInputStream("@interface GeneratedAnnotation {}".getBytes()), true, null);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testResolvedMethod() throws JavaModelException {
		IPath projectPath = env.addProject("Project", "1.5");
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "", "Try",
			"@SuppressWarnings(\"all\")\n" +
			"public class Try {}"
			);

		// install compilationParticipant
		new BuildTestParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				for (int i = 0, total = files.length; i < total; i++) {
					IFile file = files[i].getFile();
					// Traversing the members of test.ClassWithNestedAnnotation through a reference in EntryPoint.java
					if (!"Try.java".equals(file.getName())) continue;

					List<IProblem> problems = new ArrayList<IProblem>();
					CompilationUnit unit = buildCompilationUnit(files[i]);
					List<?> types = unit.types();
					for (int t = 0, l = types.size(); t < l; t++) {
						AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) types.get(t);
						ITypeBinding typeBinding = typeDecl.resolveBinding();
						if (typeBinding == null) continue;
						typeBinding = typeBinding.getAnnotations()[0].getAnnotationType();
						IAnnotationBinding targetValue = typeBinding.getAnnotations()[0];
						IMethodBinding method = targetValue.getDeclaredMemberValuePairs()[0].getMethodBinding();
						if (!"value".equals(method.getName()))
							problems.add(new ParticipantProblem("method " + method.getName() + " not found", file.getName()));
					}
					if (!problems.isEmpty()) {
						CategorizedProblem[] problemArray = new CategorizedProblem[problems.size()];
						problemArray = problems.toArray(problemArray);
						files[i].recordNewProblems(problemArray);
					}
				}
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test1001() throws JavaModelException {
	IPath projectPath = env.addProject("Project", "1.5");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	env.addClass(root, "p", "X",
		"package p;\n" +
		"public class X { /* generate problem*/ }"
		);
	new BuildTestParticipant() {
		public void buildStarting(BuildContext[] files, boolean isBatch) {
			for (int i = 0, total = files.length; i < total; i++) {
				BuildContext context = files[i];
				if (CharOperation.indexOf("generate problem".toCharArray(),
						context.getContents(), true) != -1) {
					context.recordNewProblems(new CategorizedProblem[] {
							new ParticipantProblem("Participant problem", context.getFile().getFullPath().toString())});
				}
			}
		}
	};
	fullBuild(projectPath);
	Problem[] problems = env.getProblemsFor(projectPath, "org.eclipse.jdt.core.tests.compile.problem");
	assertNotNull("null problems array", problems);
	assertEquals("unexpected problems count", 1, problems.length);
	assertEquals("unexpected generated by attribute", "missing", problems[0].getSourceId());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=158611
// Checking the GENERATED_BY attribute
public void test1002() throws JavaModelException {
	IPath projectPath = env.addProject("Project", "1.5");
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	env.addClass(root, "p", "X",
		"package p;\n" +
		"public class X { /* generate problem*/ }"
		);
	final String specificGeneratedBy = "specific";
	new BuildTestParticipant() {
		public void buildStarting(BuildContext[] files, boolean isBatch) {
			for (int i = 0, total = files.length; i < total; i++) {
				BuildContext context = files[i];
				if (CharOperation.indexOf("generate problem".toCharArray(),
						context.getContents(), true) != -1) {
					context.recordNewProblems(new CategorizedProblem[] {
							new ParticipantProblem("Participant problem", context.getFile().getFullPath().toString()) {
								public String[] getExtraMarkerAttributeNames() {
									return new String[] {IMarker.SOURCE_ID};
								}
								public Object[] getExtraMarkerAttributeValues() {
									return new String[] {specificGeneratedBy};
								}
							}});
				}
			}
		}
	};
	fullBuild(projectPath);
	Problem[] problems = env.getProblemsFor(projectPath, "org.eclipse.jdt.core.tests.compile.problem");
	assertNotNull("null problems array", problems);
	assertEquals("unexpected problems count", 1, problems.length);
	assertEquals("unexpected generated by attribute", specificGeneratedBy, problems[0].getSourceId());
}
}
