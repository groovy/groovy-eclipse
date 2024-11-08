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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AbstractMethodTests extends BuilderTests {
	private static final Comparator COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			IResource resource1 = (IResource) o1;
			IResource resource2 = (IResource) o2;
			String path1 = resource1.getFullPath().toString();
			String path2 = resource2.getFullPath().toString();
			int length1 = path1.length();
			int length2 = path2.length();

			if (length1 != length2) {
				return length1 - length2;
			}
			return path1.toString().compareTo(path2.toString());
		}
	};

	public AbstractMethodTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(AbstractMethodTests.class);
	}

	/**
	 * Check behavior in 1.2 target mode (NO generated default abstract method)
	 */
	public void test001() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$

		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$

		env.addClass(root1, "p1", "IX", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public interface IX {\n" + //$NON-NLS-1$
			"   public abstract void foo(IX x);\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		IPath classX = env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path, ""); //$NON-NLS-1$

		IPath root2 = env.addPackageFragmentRoot(project2Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project2Path, "bin"); //$NON-NLS-1$

		IPath classY =env.addClass(root2, "p3", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"import p2.*;\n" + //$NON-NLS-1$
			"public class Y extends X{\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(I__X x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved to a type", classX, 84, 88, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "The type Y must implement the inherited abstract method IX.foo(IX)", classY, 38, 39, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		expectingNoProblems();
	}

	/**
	 * Check behavior in 1.1 target mode (generated default abstract method)
	 */
	public void test002() throws JavaModelException {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		env.getJavaProject(project1Path).setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion()); // need default abstract method
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$

		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$

		env.addClass(root1, "p1", "IX", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public interface IX {\n" + //$NON-NLS-1$
			"   public abstract void foo(IX x);\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		IPath classX = env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

			//----------------------------
			//         Project2
			//----------------------------
		IPath project2Path = env.addProject("Project2"); //$NON-NLS-1$
		env.addExternalJars(project2Path, Util.getJavaClassLibs());
		env.addRequiredProject(project2Path, project1Path);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project2Path, ""); //$NON-NLS-1$

		IPath root2 = env.addPackageFragmentRoot(project2Path, "src"); //$NON-NLS-1$
		env.setOutputFolder(project2Path, "bin"); //$NON-NLS-1$

		IPath classY =env.addClass(root2, "p3", "Y", //$NON-NLS-1$ //$NON-NLS-2$
			"package p3;\n" + //$NON-NLS-1$
			"import p2.*;\n" + //$NON-NLS-1$
			"public class Y extends X{\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();

		//----------------------------
		//           Step 2
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(I__X x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		expectingOnlySpecificProblemFor(classX, new Problem("X.foo(I__X)", "I__X cannot be resolved to a type", classX, 84, 88, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$
		expectingOnlySpecificProblemFor(classY, new Problem("Y", "The type Y must implement the inherited abstract method IX.foo(IX)", classY, 38, 39, CategorizedProblem.CAT_MEMBER, IMarker.SEVERITY_ERROR)); //$NON-NLS-1$ //$NON-NLS-2$

		//----------------------------
		//           Step 3
		//----------------------------
		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"   public void foo(IX x){}\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild();
		expectingNoProblems();
	}

	/**
	 * Check behavior in 1.1 target mode (generated default abstract method)
	 */
	public void test003() throws Exception {
		//----------------------------
		//           Step 1
		//----------------------------
			//----------------------------
			//         Project1
			//----------------------------
		IPath project1Path = env.addProject("Project1"); //$NON-NLS-1$
		env.addExternalJars(project1Path, Util.getJavaClassLibs());
		env.getJavaProject(project1Path).setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, CompilerOptions.getFirstSupportedJavaVersion()); // need default abstract method
		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(project1Path, ""); //$NON-NLS-1$

		env.setOutputFolder(project1Path, "bin"); //$NON-NLS-1$
		IPath root1 = env.addPackageFragmentRoot(project1Path, "src"); //$NON-NLS-1$

		env.addClass(root1, "p1", "IX", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n" + //$NON-NLS-1$
			"public interface IX {\n" + //$NON-NLS-1$
			"   public abstract void foo(IX x);\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		env.addClass(root1, "p2", "X", //$NON-NLS-1$ //$NON-NLS-2$
			"package p2;\n" + //$NON-NLS-1$
			"import p1.*;\n" + //$NON-NLS-1$
			"public abstract class X implements IX {\n" + //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		fullBuild();
		expectingNoProblems();

		IJavaProject project = env.getJavaProject(project1Path);
		IRegion region = JavaCore.newRegion();
		region.add(project);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput = "/Project1/bin/p2/X.class\n" +
				"/Project1/bin/p1/IX.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		assertEquals("Wrong type", IResource.FILE, resources[0].getType());
		IFile classFile = (IFile) resources[0];
		IClassFileReader classFileReader = null;
		InputStream stream = null;
		try {
			stream = classFile.getContents();
			classFileReader = ToolFactory.createDefaultClassFileReader(stream, IClassFileReader.ALL);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
		assertNotNull("No class file reader", classFileReader);
		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		IMethodInfo found = null;
		loop: for (int i = 0, max = methodInfos.length; i < max; i++) {
			IMethodInfo methodInfo = methodInfos[i];
			if (CharOperation.equals(methodInfo.getName(), "foo".toCharArray())) {
				found = methodInfo;
				break loop;
			}
		}
		assertNull("Should not find a 'foo' method", found);
	}

	private String getResourceOuput(IResource[] resources) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		for (int i = 0, max = resources.length; i < max; i++) {
			writer.println(resources[i].getFullPath().toString());
		}
		writer.flush();
		writer.close();
		return Util.convertToIndependantLineDelimiter(String.valueOf(stringWriter));
	}
}
