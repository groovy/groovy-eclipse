/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.util.JRTUtil;

public class DefaultJavaRuntimeEnvironment extends FileSystem {

	private DefaultJavaRuntimeEnvironment(String[] jreClasspaths, String release) {
		super(jreClasspaths, new String[] {} /* ignore initial file names */, null, release);
	}
	private DefaultJavaRuntimeEnvironment(Classpath[] jreClasspaths) {
		super(jreClasspaths, new String[] {} /* ignore initial file names */, false);
	}

	private static INameEnvironment[] defaultJreClassLibs;

	public static INameEnvironment[] create(String[] jreClasspaths) {
		return create(jreClasspaths, null);
	}
	public static INameEnvironment[] create(String[] jreClasspaths, String release) {
		if (defaultJreClassLibs == null) {
			if (release != null && !release.equals("")) {
				defaultJreClassLibs = new INameEnvironment[1];
				Classpath[] classpath = new Classpath[jreClasspaths.length];
				for(int i = 0; i < classpath.length; i++) {
					if (jreClasspaths[i].endsWith(JRTUtil.JRT_FS_JAR)) {
						File file = new File(jreClasspaths[0]);
						classpath[i] = FileSystem.getOlderSystemRelease(file.getParentFile().getParent(), release, null);

					} else {
						classpath[i] = FileSystem.getClasspath(jreClasspaths[i], null, null);
					}
				}
				defaultJreClassLibs[0] = new DefaultJavaRuntimeEnvironment(classpath);
			}
		}
		if (defaultJreClassLibs == null) {
			defaultJreClassLibs = new INameEnvironment[1];
			defaultJreClassLibs[0] = new DefaultJavaRuntimeEnvironment(jreClasspaths, release);
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
