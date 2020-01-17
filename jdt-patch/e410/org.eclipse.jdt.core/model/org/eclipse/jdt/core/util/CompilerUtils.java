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
package org.eclipse.jdt.core.util;

import java.util.Map;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Utility class, contains helpers for configuring the compiler options based
 * on the project. If the project is a groovy project it will set the right
 * options, and will also set the groovy classpath.
 */
@SuppressWarnings("nls")
public class CompilerUtils {

	public static final int IsGrails = 0x1;
	public static final int InvokeDynamic = 0x2;

	/**
	 * Configure a real compiler options object based on the project. If anything
	 * goes wrong it will configure the options to just build java.
	 */
	public static void configureOptionsBasedOnNature(CompilerOptions options, IJavaProject javaProject) {
		if (javaProject == null) {
			options.buildGroovyFiles = 1;
			options.groovyFlags = 0;
			return;
		}
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				options.groovyProjectName = project.getName();
				options.storeAnnotations = true;
				options.buildGroovyFiles = 2;
				if (isProbablyGrailsProject(project)) {
					options.groovyFlags = IsGrails;
				} else {
					options.groovyFlags = 0;
				}
				if (hasInvokeDynamicSupport(javaProject)) {
					options.groovyFlags |= InvokeDynamic;
				}
			} else {
				options.buildGroovyFiles = 1;
				options.groovyFlags = 0;
			}
		} catch (CoreException e) {
			options.buildGroovyFiles = 1;
			options.groovyFlags = 0;
		}
	}

	/**
	 * Configure an options map (usually retrieved from a CompilerOptions object)
	 * based on the project. If anything goes wrong it will configure the options
	 * to just build java.
	 */
	public static void configureOptionsBasedOnNature(Map<String, String> options, IJavaProject javaProject) {
		IProject project = javaProject.getProject();
		try {
			if (isGroovyNaturedProject(project)) {
				options.put(CompilerOptions.OPTIONG_GroovyProjectName, javaProject.getElementName());
				options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.ENABLED);
				if (isProbablyGrailsProject(project)) {
					options.put(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(IsGrails));
				} else {
					options.put(CompilerOptions.OPTIONG_GroovyFlags, "0");
				}
				if (hasInvokeDynamicSupport(javaProject)) {
					options.merge(CompilerOptions.OPTIONG_GroovyFlags, Integer.toString(CompilerUtils.InvokeDynamic), (String one, String two) -> {
						return Integer.toString(Integer.parseInt(one) | Integer.parseInt(two));
					});
				}
			} else {
				options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
				options.put(CompilerOptions.OPTIONG_GroovyFlags, "0");
			}
		} catch (CoreException e) {
			Util.log(e, "configureOptionsBasedOnNature failed");
			options.put(CompilerOptions.OPTIONG_BuildGroovyFiles, CompilerOptions.DISABLED);
			options.put(CompilerOptions.OPTIONG_GroovyFlags, "0");
		}
	}

	private static boolean hasInvokeDynamicSupport(IJavaProject javaProject) throws CoreException {
		for (IClasspathEntry unresolved : javaProject.getRawClasspath()) {
			if (unresolved.getEntryKind() == IClasspathEntry.CPE_CONTAINER &&
					unresolved.getPath().toString().startsWith("GROOVY_SUPPORT")) {
				IClasspathContainer container = JavaCore.getClasspathContainer(unresolved.getPath(), javaProject);
				for (IClasspathEntry resolved : container.getClasspathEntries()) {
					String[] tokens = resolved.getPath().lastSegment().toString().split("-");
					if (tokens.length == 3 && "groovy".equals(tokens[0]) &&
							Character.isDigit(tokens[1].charAt(0))) {
						return tokens[2].startsWith("indy");
					}
				}
				break;
			}
		}
		return false;
	}

	/**
	 * @return {@code true} if the project has the groovy nature
	 */
	private static boolean isGroovyNaturedProject(IProject project) throws CoreException {
		return project.hasNature("org.eclipse.jdt.groovy.core.groovyNature");
	}

	/**
	 * Crude way to determine it... check for a folder called 'grails-app'. The
	 * reason we need to know is because of the extra transform that will run if
	 * it is a grails-app (tagging domain classes).
	 */
	private static boolean isProbablyGrailsProject(IProject project) {
		try {
			IFolder folder = project.getFolder("grails-app");
			return folder.exists();
		} catch (Exception e) {
			return false;
		}
	}
}
