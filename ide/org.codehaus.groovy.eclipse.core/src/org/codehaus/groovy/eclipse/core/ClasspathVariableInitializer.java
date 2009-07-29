/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Andrew Eisenberg
 * Classpath variable that initializes based on 
 * the user's GROOVY_HOME setting in the environment variables
 */
public class ClasspathVariableInitializer extends
		org.eclipse.jdt.core.ClasspathVariableInitializer {
	public static final String VARIABLE_ID = "GROOVY_HOME";

	public static IPath getEmbeddedJar(final IPath dir) {
		if (dir == null || !dir.toFile().exists()
				|| !dir.toFile().isDirectory())
			return null;
		final Collection jars = FileUtils.listFiles(dir.toFile(),
				new String[] { "jar" }, false);
		if (jars == null || jars.isEmpty())
			return null;
		// TODO -- fix me.  This is ugly
		for (final Iterator iterator = jars.iterator(); iterator.hasNext();) {
			final File jar = (File) iterator.next();
			if (!jar.getName().startsWith("groovy-")
			        || jar.getName().equals("groovy-eclipse.jar")
					|| jar.getName().endsWith("-javadoc.jar")
					|| jar.getName().endsWith("-sources.jar"))
				continue;
			return dir.append(jar.getName());
		}
		return null;
	}

	public static IPath getCPVariable() {
		try {
			final IPath path = JavaCore.getClasspathVariable(VARIABLE_ID);
			if (path == null)
				return path;
			if (ObjectUtils.equals(path, GroovyCore
					.getEmbeddedGroovyRuntimeHome()))
				return null;
			return path;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static IPath getCPVarEmbeddablePath() {
		try {
			final IPath path = JavaCore.getClasspathVariable(VARIABLE_ID);
			if (path == null)
				return path;
			if (ObjectUtils.equals(path, GroovyCore
					.getEmbeddedGroovyRuntimeHome())
					|| StringUtils.equalsIgnoreCase("embeddable", path
							.lastSegment()))
				return path;
			if (path.append("embeddable").toFile().exists())
				return path.append("embeddable");
			return path;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ClasspathVariableInitializer() {
	}

	public void initialize(final String variable) {
		if (!StringUtils.equals(variable, VARIABLE_ID))
			return;
		try {
			// First checking to see if the variable is already set, if so....
			// skip the rest
			if (JavaCore.getClasspathVariable(VARIABLE_ID) != null) {
				// No the author is not on drugs... without setting the
				// classpath variable again we got strange
				// "VariableBlock: Classpath Variable with null value: GROOVY_HOME"
				// which is not good....
				JavaCore.setClasspathVariable(VARIABLE_ID, JavaCore
						.getClasspathVariable(VARIABLE_ID), null);
				return;
			}
			// If null, check for GROOVY_HOME in system properties and use that
			// value instead
			final String property = System.getProperty(VARIABLE_ID);
			if (StringUtils.isNotBlank(property)) {
				JavaCore.setClasspathVariable(VARIABLE_ID, new Path(property),
						null);
				return;
			}
			// As a final choice, we choose the embedded groovy runtime included
			// with the plugin.
			final IPath pluginHome = GroovyCore.getEmbeddedGroovyRuntimeHome();
			JavaCore.setClasspathVariable(VARIABLE_ID, pluginHome, null);
		} catch (final JavaModelException e) {
			GroovyCore.logException(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}
}
