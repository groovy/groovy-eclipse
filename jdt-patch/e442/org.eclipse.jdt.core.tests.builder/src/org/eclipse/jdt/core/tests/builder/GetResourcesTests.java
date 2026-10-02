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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * Basic tests of {@link JavaCore#getGeneratedResources(IRegion, boolean)}.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GetResourcesTests extends BuilderTests {

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

	public GetResourcesTests(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 15 };
	}

	public static Test suite() {
		return buildTestSuite(GetResourcesTests.class);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test001() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 1, resources.length);//$NON-NLS-1$
		String actualOutput = getResourceOuput(resources);
		String expectedOutput = "/Project/bin/p1/Hello.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test002() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"	Object foo() {\n" + //$NON-NLS-1$
			"		return new Object() {};\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$

		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$1.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test003() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"	Object foo() {\n" +
			"		if(false) {\n" + //$NON-NLS-1$
			"			return new Object() {};\n" +
			"		}\n" +
			"		return null;\n" + //$NON-NLS-1$
			"	}\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 1, resources.length);//$NON-NLS-1$
		String actualOutput = getResourceOuput(resources);
		String expectedOutput = "/Project/bin/p1/Hello.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test004() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"	Object foo() {\n" +
			"		return new Object() {};\n" +
			"	}\n" + //$NON-NLS-1$
			"	Object foo2() {\n" +
			"		return new Object() {};\n" +
			"	}\n" + //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 3, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$1.class\n" +
			"/Project/bin/p1/Hello$2.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test005() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "a", "Anon", //$NON-NLS-1$ //$NON-NLS-2$
			"package a;\n" +
			"\n" +
			"public class Anon {\n" +
			"\n" +
			"    Anon() {\n" +
			"        Object o1 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"1\"; // a/Anon$3 in 1.5,  a/Anon$11 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"        Object o2 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"2\"; // a/Anon$4 in 1.5,  a/Anon$12 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    void hello() {\n" +
			"        Object o3 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"3\"; // a/Anon$5 in 1.5,  a/Anon$13 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"        Object o4 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"4\"; // a/Anon$6 in 1.5,  a/Anon$14 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    static void hello2() {\n" +
			"        Object o5 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"5\"; // a/Anon$7 in 1.5,  a/Anon$15 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"        Object o6 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"6\"; // a/Anon$8 in 1.5,  a/Anon$16 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    static {\n" +
			"        Object o7 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"7\"; // a/Anon$1 in 1.5,  a/Anon$1 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"\n" +
			"        Object o8 = new Object() {\n" +
			"            public String toString() {\n" +
			"                return \"8\"; // a/Anon$2 in 1.5,  a/Anon$2 in 1.4\n" +
			"            }\n" +
			"        };\n" +
			"    }\n" +
			"\n" +
			"    static class Anon2 {\n" +
			"        // it\'s an object init block which has different prio as constructor!\n" +
			"        {\n" +
			"            Object o1 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"1\"; // a/Anon$Anon2$1 in 1.5,  a/Anon$3 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"            Object o2 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"2\"; // a/Anon$Anon2$2 in 1.5,  a/Anon$4 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"        }\n" +
			"\n" +
			"        void hello() {\n" +
			"            Object o3 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"3\"; // a/Anon$Anon2$5 in 1.5,  a/Anon$7 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"            Object o4 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"4\"; // a/Anon$Anon2$6 in 1.5,  a/Anon$8 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"        }\n" +
			"\n" +
			"        static void hello2() {\n" +
			"            Object o5 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"5\"; // a/Anon$Anon2$7 in 1.5,  a/Anon$9 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"            Object o6 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"6\"; //  a/Anon$Anon2$8 in 1.5,  a/Anon$10 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"        }\n" +
			"\n" +
			"        static {\n" +
			"            Object o7 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"7\"; // a/Anon$Anon2$3 in 1.5,  a/Anon$5 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"\n" +
			"            Object o8 = new Object() {\n" +
			"                public String toString() {\n" +
			"                    return \"8\"; // a/Anon$Anon2$4 in 1.5,  a/Anon$6 in 1.4\n" +
			"                }\n" +
			"            };\n" +
			"        }\n" +
			"    }\n" +
			"}");

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("a");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Anon.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 18, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/a/Anon.class\n" +
			"/Project/bin/a/Anon$1.class\n" +
			"/Project/bin/a/Anon$2.class\n" +
			"/Project/bin/a/Anon$3.class\n" +
			"/Project/bin/a/Anon$4.class\n" +
			"/Project/bin/a/Anon$5.class\n" +
			"/Project/bin/a/Anon$6.class\n" +
    		"/Project/bin/a/Anon$7.class\n" +
    		"/Project/bin/a/Anon$8.class\n" +
    		"/Project/bin/a/Anon$Anon2.class\n" +
    		"/Project/bin/a/Anon$Anon2$1.class\n" +
			"/Project/bin/a/Anon$Anon2$2.class\n" +
			"/Project/bin/a/Anon$Anon2$3.class\n" +
			"/Project/bin/a/Anon$Anon2$4.class\n" +
			"/Project/bin/a/Anon$Anon2$5.class\n" +
			"/Project/bin/a/Anon$Anon2$6.class\n" +
			"/Project/bin/a/Anon$Anon2$7.class\n" +
			"/Project/bin/a/Anon$Anon2$8.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
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

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test007() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" + //$NON-NLS-1$
			"class Foo {\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
			);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test008() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test009() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(projectPath, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getUnderlyingResource());
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		ICompilationUnit compilationUnit = packageFragment.getCompilationUnit("Hello.java");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(compilationUnit);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 1, resources.length);//$NON-NLS-1$
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);
		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test010() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		env.addFile(root, "p1/Test.txt", "This is a non-java resource");
		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, false);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 3, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Test.txt\n" +
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test011() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.addFile(root, "p1/Test.txt", "This is a non-java resource");
		incrementalBuild(projectPath);

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 3, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Test.txt\n" +
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test012() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.addFile(root, "p1/Test.txt", "This is a non-java resource");

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test013() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src", new Path[] {new Path("**/*.txt")}, null); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.addFile(root, "p1/Test.txt", "This is a non-java resource");
		incrementalBuild(projectPath);

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test014() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src", new Path[] {new Path("**/*.txt")}, null); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IPackageFragmentRoot root2 = project.getPackageFragmentRoot(project.getProject().getWorkspace().getRoot().findMember(root.makeAbsolute()));
		IPackageFragment packageFragment = root2.getPackageFragment("p1");//$NON-NLS-1$
		IRegion region = JavaCore.newRegion();
		region.add(packageFragment);
		IResource[] resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.addFile(root, "p1/Test.txt", "This is an excluded non-java resource");
		env.addFile(root, "p1/Test.log", "This is an included non-java resource");
		incrementalBuild(projectPath);

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 3, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Test.log\n" +
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test015() throws JavaModelException {
		IPath projectPath = env.addProject("Project"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());
		fullBuild(projectPath);

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$

		IPath root = env.addPackageFragmentRoot(projectPath, "src", new Path[] {new Path("**/*.txt")}, null); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		env.addClass(root, "p1", "Hello", //$NON-NLS-1$ //$NON-NLS-2$
			"package p1;\n"+ //$NON-NLS-1$
			"public class Hello {\n" +
			"   public class Z {}\n"+ //$NON-NLS-1$
			"   public static void main(String args[]) {\n"+ //$NON-NLS-1$
			"      System.out.println(\"Hello world\");\n"+ //$NON-NLS-1$
			"   }\n"+ //$NON-NLS-1$
			"}\n" //$NON-NLS-1$
		);

		incrementalBuild(projectPath);

		IJavaProject project = env.getJavaProject(projectPath);
		IRegion region = JavaCore.newRegion();
		region.add(project);
		IResource[] resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 2, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		String actualOutput = getResourceOuput(resources);
		String expectedOutput =
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.addFile(root, "p1/Test.txt", "This is an excluded non-java resource");
		env.addFile(root, "p1/Test.log", "This is an included non-java resource");
		incrementalBuild(projectPath);

		resources = JavaCore.getGeneratedResources(region, true);
		assertEquals("Wrong size", 3, resources.length);//$NON-NLS-1$
		Arrays.sort(resources, COMPARATOR);
		actualOutput = getResourceOuput(resources);
		expectedOutput =
			"/Project/bin/p1/Test.log\n" +
			"/Project/bin/p1/Hello.class\n" +
			"/Project/bin/p1/Hello$Z.class\n";
		assertEquals("Wrong names", Util.convertToIndependantLineDelimiter(expectedOutput), actualOutput);

		env.removeProject(projectPath);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=6584
	public void test016() throws JavaModelException {
		try {
			JavaCore.getGeneratedResources(null, true);
			assertTrue("Region cannot be null", false);
		} catch(IllegalArgumentException e) {
			// ignore: expected exception
		}
	}
}
