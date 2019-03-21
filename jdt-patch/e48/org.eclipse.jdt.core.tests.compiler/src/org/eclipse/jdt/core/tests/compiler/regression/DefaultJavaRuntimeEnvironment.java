/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;

public class DefaultJavaRuntimeEnvironment extends FileSystem {

	private DefaultJavaRuntimeEnvironment(String[] jreClasspaths) {
		super(jreClasspaths, new String[] {} /* ignore initial file names */, null);
	}

	private static INameEnvironment[] defaultJreClassLibs;

	public static INameEnvironment[] create(String[] jreClasspaths) {
		if (defaultJreClassLibs == null) {
			defaultJreClassLibs = new INameEnvironment[1];
			defaultJreClassLibs[0] = new DefaultJavaRuntimeEnvironment(jreClasspaths);
		}
		return defaultJreClassLibs;
	}

	public void cleanup() {
		// reset output folder only, which is the last entry on the classpath list
		// see #getDefaultClassPaths()
		Classpath outputFolder = this.classpaths[this.classpaths.length - 1];
		// and remove the path from cached module locations:
		for (Iterator<Entry<String, Classpath>> iterator = this.moduleLocations.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Classpath> entry = iterator.next();
			if (entry.getValue().equals(outputFolder))
				iterator.remove();
		}
		outputFolder.reset();
	}
	
	public static void cleanUpDefaultJreClassLibs() {
		if (defaultJreClassLibs != null && defaultJreClassLibs.length > 0) {
			if (defaultJreClassLibs[0] instanceof DefaultJavaRuntimeEnvironment)
				defaultJreClassLibs[0].cleanup();
		}
	}

	private static String[] javaLibsAndOutputDir;
	
	static String[] getDefaultClassPaths() {
		if (javaLibsAndOutputDir == null)
			javaLibsAndOutputDir = Util.concatWithClassLibs(AbstractRegressionTest.OUTPUT_DIR, false);
		return javaLibsAndOutputDir;
	}
}
