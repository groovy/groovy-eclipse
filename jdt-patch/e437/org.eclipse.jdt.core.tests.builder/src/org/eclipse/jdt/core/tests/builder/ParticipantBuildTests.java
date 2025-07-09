/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.tests.builder.participants.TestCompilationParticipant1;
import org.eclipse.jdt.core.tests.builder.participants.TestCompilationParticipant2;
import org.eclipse.jdt.core.tests.builder.participants.TestCompilationParticipant3;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ParticipantBuildTests extends BuilderTests {
	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	public ParticipantBuildTests(String name) {
		super(name);
	}

	public void tearDown() throws Exception {
		TestCompilationParticipant1.PARTICIPANT = null;
		TestCompilationParticipant2.PARTICIPANT = null;
		TestCompilationParticipant3.PARTICIPANT = null;
		super.tearDown();
	}

	public static Test suite() {
		return buildTestSuite(ParticipantBuildTests.class);
	}

	static class PostProcessingParticipant extends CompilationParticipant {
		@Override
		public boolean isPostProcessor() {
			return true;
		}
	}

	static class ParticipantProblem extends CategorizedProblem {
		int counter = 0;
		String message;
		int id;
		char[] filename;
		ParticipantProblem(String message, String filename) {
			this.message = message;
			this.id = this.counter ++;
			this.filename = filename.toCharArray();
		}
		public String[] getArguments() { return new String[0]; }
		public int getID() { return this.id; }
		public String getMessage() { return this.message; }
		public char[] getOriginatingFileName() { return this.filename; }
		public int getSourceStart() { return 0; }
		public int getSourceEnd() { return 0; }
		public int getSourceLineNumber() { return 1; }
		public boolean isError() { return true; }
		public boolean isWarning() { return false; }
		public boolean isInfo() { return false; }
		public void setSourceEnd(int sourceEnd) {/* not needed */}
		public void setSourceLineNumber(int lineNumber)  {/* not needed */}
		public void setSourceStart(int sourceStart) {/* not needed */}
		public int getCategoryID() { return 0; }
		public String getMarkerType() { return "org.eclipse.jdt.core.tests.compile.problem"; }
	}

	CompilationUnit buildCompilationUnit(BuildContext file) {
		IJavaProject javaProject = JavaCore.create(file.getFile().getProject());
		ASTParser p = ASTParser.newParser(JLS3_INTERNAL);
		p.setProject(javaProject);
		p.setSource(file.getContents());
		p.setResolveBindings(true);
		p.setKind(ASTParser.K_COMPILATION_UNIT);
		p.setUnitName(file.getFile().getName());
		return (CompilationUnit) p.createAST(null);
	}

	public void testBuildStarting() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IPath test = env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
		);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			int buildPass = 0;
			public void buildStarting(BuildContext[] files, boolean isBatchBuild) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java")); //$NON-NLS-1$
				if (this.buildPass == 0 || this.buildPass == 3) {
					try {
						genedType.create("public class GeneratedType {}".getBytes(), true, false, null); //$NON-NLS-1$
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
		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
		);
		incrementalBuild(projectPath);
		expectingOnlySpecificProblemFor(test, new Problem("", "GeneratedType cannot be resolved to a type", test, 26, 39, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));

		// GeneratedType will be recreated
		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
		);
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testDefaultValue() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "test", "EntryPoint", //$NON-NLS-1$ //$NON-NLS-2$
			"package test;\n" + //$NON-NLS-1$
			"public class EntryPoint { ClassWithNestedAnnotation nestedAnnotation; }" //$NON-NLS-1$
			);

		env.addClass(root, "test", "ClassWithNestedAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
			"package test;\n" + //$NON-NLS-1$
			"public class ClassWithNestedAnnotation {\n" + //$NON-NLS-1$
			"	public final int FOUR = 4; \n " + //$NON-NLS-1$
			"	public @interface NestedAnnotation {\n" + //$NON-NLS-1$
			"		public enum Character { Winnie, Tiger, Piglet, Eore; }\n" + //$NON-NLS-1$
			"		Character value() default Character.Eore; \n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"}" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				for (int i = 0, total = files.length; i < total; i++) {
					IFile file = files[i].getFile();
					// Traversing the members of test.ClassWithNestedAnnotation through a reference in EntryPoint.java
					if (!"EntryPoint.java".equals(file.getName())) continue; //$NON-NLS-1$

					List problems = new ArrayList();
					CompilationUnit unit = buildCompilationUnit(files[i]);
					List types = unit.types();
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
						problemArray = (CategorizedProblem[]) problems.toArray(problemArray);
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
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		env.addClass(root, "p", "X",
			"package p;\n" +
			"public class X { /* generate problem*/ }"
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
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
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"@interface TestAnnotation {}\n" + //$NON-NLS-1$
			"public class Test extends GeneratedType {}\n" //$NON-NLS-1$
			);

		env.addClass(root, "", "Other", //$NON-NLS-1$ //$NON-NLS-2$
			"public class Other { MissingAnnotation m; }\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			int count = 2;
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				if (this.count == 2) {
					this.count--;
					BuildContext result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("MissingAnnotation.java")); //$NON-NLS-1$
					try {
						genedType.create("public @interface MissingAnnotation {}".getBytes(), true, false, null); //$NON-NLS-1$
					} catch (CoreException e) {
						e.printStackTrace();
					}
					result.recordAddedGeneratedFiles(new IFile[] {genedType});
				} else if (this.count == 1) {
					this.count--;
					BuildContext result = files[0];
					IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedType.java")); //$NON-NLS-1$
					try {
						genedType.create("public class GeneratedType {}".getBytes(), true, false, null); //$NON-NLS-1$
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
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"@GeneratedAnnotation\n" + //$NON-NLS-1$
			"public class Test { public void method() { p1.p2.GeneratedType.method(); } }\n" //$NON-NLS-1$
			);

		env.addClass(root, "p1", "GeneratedAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"@interface GeneratedAnnotation{}\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getProject().getFile(new Path("src/p1/p2/GeneratedType.java")); //$NON-NLS-1$
				if (genedType.exists()) return;
				try {
					IFolder folder = (IFolder) genedType.getParent();
					if(!folder.exists())
						folder.create(true, true, null);
					genedType.create("package p1.p2; public class GeneratedType { public static void method(){} }".getBytes(), true, false, null); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	/**
	 * Test that a build participant can inspect the declared annotations by name
	 */
	public void testProcessAnnotationHasAnnotation() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"@GeneratedAnnotation\n" + //$NON-NLS-1$
			"public class Test { public void method() {  } }\n" //$NON-NLS-1$
			);
		env.addClass(root, "p1", "GeneratedAnnotation", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"@interface GeneratedAnnotation{}\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				Optional<BuildContext> testFileContext = Arrays.stream(files).filter(bc->bc.getFile().getName().equals("Test.java")).findFirst();
				assertTrue("Testfile not found in build context!", testFileContext.isPresent());
				assertTrue(testFileContext.get().hasAnnotations());
				assertTrue(testFileContext.get().hasAnnotations("p1.GeneratedAnnotation"));
				assertFalse(testFileContext.get().hasAnnotations("gibts.nicht.Hier"));
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testProcessAnnotationReferences() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Test", //$NON-NLS-1$ //$NON-NLS-2$
			"@GeneratedAnnotation\n" + //$NON-NLS-1$
			"public class Test {}\n" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				// want to add a gen'ed source file that is referenced from the initial file to see if its recompiled
				BuildContext result = files[0];
				IFile genedType = result.getFile().getParent().getFile(new Path("GeneratedAnnotation.java")); //$NON-NLS-1$
				if (genedType.exists()) return;
				try {
					genedType.create("@interface GeneratedAnnotation {}".getBytes(), true, false, null); //$NON-NLS-1$
				} catch (CoreException e) {
					e.printStackTrace();
				}
				result.recordAddedGeneratedFiles(new IFile[] {genedType});
			}
		};

		fullBuild(projectPath);
		expectingNoProblems();
	}

	public void testPostProcessingReturnValues() throws JavaModelException {
		final String PROJECT_NAME = "Project";
		IPath projectPath = env.addProject(PROJECT_NAME);
		IProject testProject = env.getProject(PROJECT_NAME);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		// add 3 classes and build incrementally --> 3 classes should be post-processed
		// TestClass0 will return a modified class content from post-processing
		// TestClass1 will return an empty Optional, indicating no byte code change
		// from post-processing
		// TestClass2 will return the unmodified class content from post-processing
		String[] classNames = new String[3];
		for (int i = 0; i < 3; i++) {
			String className = "TestClass" + i;
			env.addClass(root, "", className, "public class " + className + " {}\n");
			classNames[i] = className;
		}

		TestCompilationParticipant2.PARTICIPANT = new PostProcessingParticipant() {
			Map<String, byte[]> postProcessedClasses;

			public void buildStarting(BuildContext[] files, boolean isBatchBuild) {
				this.postProcessedClasses = new HashMap<>();
			}

			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				String fileName = file.getFile().getName();
				String className = fileName.substring(0, fileName.indexOf("."));
				byte[] originalBytes = bytes.readAllBytes();
				Optional<byte[]> newBytes = Optional.empty();
				switch (className) {
				case "TestClass0": {
					newBytes = Optional.of("ClassContent".getBytes());
					this.postProcessedClasses.put(className, newBytes.get());
					break;
				}
				case "TestClass1": {
					this.postProcessedClasses.put(className, originalBytes);
					break;
				}
				case "TestClass2": {
					newBytes = Optional.of(originalBytes);
					this.postProcessedClasses.put(className, originalBytes);
					break;
				}
				}
				return newBytes;
			}

			public void buildFinished(IJavaProject project) {
				// check that all classes were post-processed and that their content on disk
				// matches the expectation after post-processing
				assertEquals(classNames.length, this.postProcessedClasses.size());
				for (String className : classNames) {
					byte[] expectedClassContent = this.postProcessedClasses.get(className);
					assertNotNull(expectedClassContent);
					assertArrayEquals(expectedClassContent,
							getClassFileContent(testProject, "bin/" + className + ".class"));
				}
			}
		};
		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	public void testPostProcessOrderingAndPropagation() throws JavaModelException {
		final String PROJECT_NAME = "Project";
		IPath projectPath = env.addProject(PROJECT_NAME);
		IProject testProject = env.getProject(PROJECT_NAME);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		final String TEST_CLASS_NAME = "TestClass";
		env.addClass(root, "", TEST_CLASS_NAME, "public class " + TEST_CLASS_NAME + " {}\n");

		// The set "requires" attribute enforces dependency order TestCompilationParticipant1 -->
		// TestCompilationParticipant3 --> TestCompilationParticipant2
		// that implies call order 2 -> 3 -> 1
		//
		// Compilation participants post-processing methods combine incoming class
		// content with some new class content to verify that changes are propagated
		// between participants. The final class content on disk is verified that
		// it contains the modifications from all participants.
		AtomicInteger nrOfCalledParticipants = new AtomicInteger();
		ByteArrayOutputStream combinedOutput = new ByteArrayOutputStream();

		TestCompilationParticipant2.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				assertEquals(0, nrOfCalledParticipants.getAndIncrement());

				byte[] newBytes = "Participant1".getBytes();
				try {
					combinedOutput.write(newBytes);
				} catch (IOException e) {
					fail("Could not write combined output byte array: " + e.getMessage());
				}
				return Optional.of(newBytes);
			}
		};

		TestCompilationParticipant1.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				assertEquals(2, nrOfCalledParticipants.get());

				byte[] originalBytes = bytes.readAllBytes();
				assertTrue(Arrays.equals(combinedOutput.toByteArray(), originalBytes));
				byte[] toBeAppendedBytes = "Participant2".getBytes();
				byte[] newBytes = new byte[originalBytes.length + toBeAppendedBytes.length];
				System.arraycopy(originalBytes, 0, newBytes, 0, originalBytes.length);
				System.arraycopy(toBeAppendedBytes, 0, newBytes, originalBytes.length, toBeAppendedBytes.length);

				try {
					combinedOutput.write(toBeAppendedBytes);
				} catch (IOException e) {
					fail("Could not write combined output byte array: " + e.getMessage());
				}
				return Optional.of(newBytes);
			}

			@Override
			public void buildFinished(IJavaProject project) {
				// Check that the modifications of all 3 post processors are visible in the
				// modified class on disk
				assertArrayEquals(combinedOutput.toByteArray(),
						getClassFileContent(testProject, "bin/" + TEST_CLASS_NAME + ".class"));
			}
		};

		TestCompilationParticipant3.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				assertEquals(1, nrOfCalledParticipants.getAndIncrement());

				byte[] originalBytes = bytes.readAllBytes();
				assertTrue(Arrays.equals(combinedOutput.toByteArray(), originalBytes));
				byte[] toBeAppendedBytes = "Participant3".getBytes();
				byte[] newBytes = new byte[originalBytes.length + toBeAppendedBytes.length];
				System.arraycopy(originalBytes, 0, newBytes, 0, originalBytes.length);
				System.arraycopy(toBeAppendedBytes, 0, newBytes, originalBytes.length, toBeAppendedBytes.length);

				try {
					combinedOutput.write(toBeAppendedBytes);
				} catch (IOException e) {
					fail("Could not write combined output byte array: " + e.getMessage());
				}
				return Optional.of(newBytes);
			}
		};

		incrementalBuild(projectPath);
		expectingNoProblems();
	}

	private byte[] getClassFileContent(IProject project, String classPath) {
		IFile classFile = (IFile) project.findMember(classPath);
		if(classFile == null) {
			return null;
		}
		byte[] classContent = null;
		try {
			classContent = classFile.getContents().readAllBytes();
		} catch (IOException | CoreException e) {
			fail("Could not read class file " + classFile.getFullPath());
		}
		return classContent;
	}

	public void testPostProcessDependenciesAndProblems() throws JavaModelException {
		final String PROJECT_NAME = "Project";
		IPath projectPath = env.addProject(PROJECT_NAME);
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, "");
		IPath root = env.addPackageFragmentRoot(projectPath, "src");
		env.setOutputFolder(projectPath, "bin");

		// add two classes and compile
		final String TEST_CLASS_NAME = "TestClass";
		env.addClass(root, "", TEST_CLASS_NAME, "public class " + TEST_CLASS_NAME + " {}\n");
		final String TEST_CLASS_2_NAME = "TestClass2";
		env.addClass(root, "", TEST_CLASS_2_NAME, "public class " + TEST_CLASS_2_NAME + " {}\n");

		TestCompilationParticipant1.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				if (file.getFile().getName().contains(TEST_CLASS_2_NAME)) {
					// record dependency from TestClass2 to TestClass
					file.recordDependencies(new String[] { TEST_CLASS_NAME });
				}
				return Optional.empty();
			}
		};

		incrementalBuild(projectPath);
		expectingNoProblems();

		// modify TestClass and recompile --> should compile and post-process TestClass
		// and dependent TestClass2
		env.addClass(root, "", TEST_CLASS_NAME, "public class " + TEST_CLASS_NAME + " {public int testInt;}\n");

		ArrayList<String> postProcessedClasses = new ArrayList<>();
		TestCompilationParticipant1.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				String fileName = file.getFile().getName();
				String className = fileName.substring(0, fileName.indexOf("."));
				postProcessedClasses.add(className);
				return Optional.empty();
			}
		};

		incrementalBuild(projectPath);
		expectingNoProblems();
		assertArrayEquals(new String[] { TEST_CLASS_NAME, TEST_CLASS_2_NAME }, postProcessedClasses.toArray());

		// modify TestClass2 and recompile --> should only compile and post-process TestClass2
		IPath testClass2Path = env.addClass(root, "", TEST_CLASS_2_NAME,
				"public class " + TEST_CLASS_2_NAME + " {public int testInt;}\n");

		final String PROBLEM_MSG = "TestProblem";
		postProcessedClasses.clear();
		TestCompilationParticipant1.PARTICIPANT = new PostProcessingParticipant() {
			public Optional<byte[]> postProcess(BuildContext file, ByteArrayInputStream bytes) {
				String fileName = file.getFile().getName();
				String className = fileName.substring(0, fileName.indexOf("."));
				postProcessedClasses.add(className);
				// create problem marker on TestClass2
				file.recordNewProblems(new CategorizedProblem[] {
						new ParticipantProblem(PROBLEM_MSG, file.getFile().getFullPath().toString()) });
				return Optional.empty();
			}
		};

		incrementalBuild(projectPath);
		assertArrayEquals(new String[] { TEST_CLASS_2_NAME }, postProcessedClasses.toArray());

		Problem[] reportedProblems = env.getProblemsFor(projectPath, "org.eclipse.jdt.core.tests.compile.problem");
		assertEquals(1, reportedProblems.length);
		assertEquals(testClass2Path, reportedProblems[0].getResourcePath());
		assertEquals(PROBLEM_MSG, reportedProblems[0].getMessage());

		// recompile TestClass2 --> error marker should be deleted
		env.addClass(root, "", TEST_CLASS_2_NAME, "public class " + TEST_CLASS_2_NAME + " {}\n");
		TestCompilationParticipant1.PARTICIPANT = null;
		incrementalBuild(projectPath);

		reportedProblems = env.getProblemsFor(projectPath, "org.eclipse.jdt.core.tests.compile.problem");
		assertEquals(0, reportedProblems.length);
	}

	public void testResolvedMethod() throws JavaModelException {
		IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion()); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "", "Try", //$NON-NLS-1$ //$NON-NLS-2$
			"@SuppressWarnings(\"all\")\n" + //$NON-NLS-1$
			"public class Try {}" //$NON-NLS-1$
			);

		// install compilationParticipant
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
			public boolean isAnnotationProcessor() {
				return true;
			}
			public void processAnnotations(BuildContext[] files) {
				for (int i = 0, total = files.length; i < total; i++) {
					IFile file = files[i].getFile();
					// Traversing the members of test.ClassWithNestedAnnotation through a reference in EntryPoint.java
					if (!"Try.java".equals(file.getName())) continue; //$NON-NLS-1$

					List problems = new ArrayList();
					CompilationUnit unit = buildCompilationUnit(files[i]);
					List types = unit.types();
					for (int t = 0, l = types.size(); t < l; t++) {
						AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) types.get(t);
						ITypeBinding typeBinding = typeDecl.resolveBinding();
						if (typeBinding == null) continue;
						IAnnotationBinding[] annotations = typeBinding.getAnnotations();
						if (annotations == null || annotations.length == 0) {
							throw new IllegalStateException(
									"Expected at least one annotation in binding " + typeBinding);
						}
						IAnnotationBinding targetValue = annotations[0];
						typeBinding = targetValue.getAnnotationType();

						IMemberValuePairBinding[] pairs = targetValue.getAllMemberValuePairs();
						if (pairs == null || pairs.length == 0) {
							throw new IllegalStateException(
									"Expected at least one member value pair in " + targetValue
									+ ", binding was: " + typeBinding);
						}
						IMethodBinding method = pairs[0].getMethodBinding();
						if (!"value".equals(method.getName()))
							problems.add(new ParticipantProblem("method " + method.getName() + " not found", file.getName()));
					}
					if (!problems.isEmpty()) {
						CategorizedProblem[] problemArray = new CategorizedProblem[problems.size()];
						problemArray = (CategorizedProblem[]) problems.toArray(problemArray);
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
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	env.addClass(root, "p", "X",
		"package p;\n" +
		"public class X { /* generate problem*/ }"
		);
		TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
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
	IPath projectPath = env.addProject("Project", CompilerOptions.getFirstSupportedJavaVersion());
	env.addExternalJars(projectPath, Util.getJavaClassLibs());
	env.removePackageFragmentRoot(projectPath, "");
	IPath root = env.addPackageFragmentRoot(projectPath, "src");
	env.setOutputFolder(projectPath, "bin");
	env.addClass(root, "p", "X",
		"package p;\n" +
		"public class X { /* generate problem*/ }"
		);
	final String specificGeneratedBy = "specific";
	TestCompilationParticipant1.PARTICIPANT = new CompilationParticipant() {
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
