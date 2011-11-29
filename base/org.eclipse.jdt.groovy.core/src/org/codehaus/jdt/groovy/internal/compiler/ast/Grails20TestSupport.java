/*******************************************************************************
 * Copyright (c) 2009-2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Kris De Volder - Initial api + implementation
 *******************************************************************************/
package org.codehaus.jdt.groovy.internal.compiler.ast;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovySystem;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.codehaus.jdt.groovy.control.EclipseSourceUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Utility class, containing methods related to Grails 2.0 test support. This code should really be part of Grails support, but
 * lives here for now.
 * 
 * @author Kris De Volder
 */
public class Grails20TestSupport {

	private static final String GRAILS_UTIL_BUILD_SETTINGS = "grails.util.BuildSettings";
	private static final String GRAILS_UTIL_BUILD_SETTINGS_HOLDER = "grails.util.BuildSettingsHolder";
	CompilerOptions options;
	GroovyClassLoader gcl;

	public Grails20TestSupport(CompilerOptions options, GroovyClassLoader gcl) {
		this.options = options;
		this.gcl = gcl;
	}

	/**
	 * Grails 2.0 adds automatic imports to classes in test/unit folder. See
	 * org.codehaus.groovy.grails.test.compiler.GrailsTestCompiler and the _TestApp.groovy script.
	 */
	public void addGrailsTestCompilerCustomizers(CompilationUnit groovyCompilationUnit) {
		if (GroovySystem.getVersion().startsWith("1.8")) {
			// The assumption is that only Grails 2.0 projects will be affected, because 1.3.7 projects require 1.7 compiler.
			ImportCustomizer importCustomizer = new ImportCustomizer() {
				@Override
				public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
						throws CompilationFailedException {
					if (isInGrailsUnitTestSourceFolder(source)) {
						super.call(source, context, classNode);
					}
				}
			};
			importCustomizer.addStarImports("grails.test.mixin");
			importCustomizer.addStarImports("org.junit");
			importCustomizer.addStaticStars("org.junit.Assert");
			groovyCompilationUnit.addPhaseOperation(importCustomizer, importCustomizer.getPhase().getPhaseNumber());

			@SuppressWarnings("rawtypes")
			Class testForClass = null;
			try {
				testForClass = Class.forName("grails.test.mixin.TestFor", false, gcl);
				if (testForClass != null) {
					ASTTransformationCustomizer astTransformationCustomizer = new ASTTransformationCustomizer(testForClass) {
						@Override
						public void call(SourceUnit source, GeneratorContext context, ClassNode classNode)
								throws CompilationFailedException {
							if (isInGrailsUnitTestSourceFolder(source)) {
								super.call(source, context, classNode);
							}
						}
					};
					groovyCompilationUnit.addPhaseOperation(astTransformationCustomizer, astTransformationCustomizer.getPhase()
							.getPhaseNumber());
					ensureGrailsBuildSettings();
				}
			} catch (LinkageError e) {
				// Somewhat expected... if there's some issue with the project's classpath or its not really a Grails 2.0 project
				// so silently ignore.
				// e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// Somewhat expected... if there's some issue with the project's classpath or its not really a Grails 2.0 project
				// so silently ignore.
				// e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * Attempts to create and initialise a BuildSettings instance for
	 */
	@SuppressWarnings("rawtypes")
	void ensureGrailsBuildSettings() {
		try {
			String projectName = options.groovyProjectName;
			if (projectName != null) {
				Class buildSettingsHolder = gcl.loadClass(GRAILS_UTIL_BUILD_SETTINGS_HOLDER);
				Object buildSettings = getBuildSettings(buildSettingsHolder);
				if (buildSettings == null) {
					buildSettings = createBuildSettings();
					setBuildSettings(buildSettingsHolder, buildSettings);
					Object checkit = getBuildSettings(buildSettingsHolder);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// ignore ... classpath doesn't have what we expect.
		}
	}

	@SuppressWarnings("rawtypes")
	private Object createBuildSettings() throws ClassNotFoundException, SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class buildSettingsClass = gcl.loadClass(GRAILS_UTIL_BUILD_SETTINGS);
		Constructor constructor = buildSettingsClass.getConstructor(File.class, File.class);
		return constructor.newInstance(getGrailsHome(), getProjectHome());
	}

	private Object getGrailsHome() {
		return null; // not computed for now... for the current use case it doesn't seem needed so why bother.
	}

	private File getProjectHome() {
		String projectName = options.groovyProjectName;
		if (projectName != null) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IPath location = project.getLocation();
			if (location != null) {
				return location.toFile();
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private static Object getBuildSettings(Class buildSettingsHolder) throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method m = buildSettingsHolder.getMethod("getSettings");
		return m.invoke(null);
	}

	@SuppressWarnings("rawtypes")
	private static void setBuildSettings(Class buildSettingsHolder, Object buildSettings) throws SecurityException,
			NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Method m = buildSettingsHolder.getMethod("setSettings",
				buildSettingsHolder.getClassLoader().loadClass(GRAILS_UTIL_BUILD_SETTINGS));
		m.invoke(null, buildSettings);
		Assert.isTrue(getBuildSettings(buildSettingsHolder) == buildSettings);
	}

	static boolean isInGrailsUnitTestSourceFolder(SourceUnit source) {
		if (source instanceof EclipseSourceUnit) {
			EclipseSourceUnit eclipseSource = (EclipseSourceUnit) source;
			IFile file = eclipseSource.getEclipseFile();
			if (file != null) {
				IPath path = file.getProjectRelativePath();
				return new Path("test/unit").isPrefixOf(path);
			}
		}
		return false;
	}

}
