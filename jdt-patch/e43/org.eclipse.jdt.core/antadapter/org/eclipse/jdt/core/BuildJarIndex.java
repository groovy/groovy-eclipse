/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jdt.core.index.JavaIndexer;
import org.eclipse.jdt.internal.antadapter.AntAdapterMessages;

/**
 * <p>
 * An Ant task to generate the index file for the given jar path.
 * </p>
 * <p>
 * <code>&lt;eclipse.buildJarIndex jarPath="Test.jar" indexPath="Test.index"/&gt;</code>
 * </p>
 * <p>
 * For more information on Ant check out the website at http://jakarta.apache.org/ant/ .
 * </p>
 * <p>
 * This is not intended to be subclassed by users.
 * </p>
 * @since 3.8
 */
public class BuildJarIndex extends Task {

	private String jarPath;
	private String indexPath;

	public void execute() throws BuildException {
		if (this.jarPath == null) {
			throw new BuildException(AntAdapterMessages.getString("buildJarIndex.jarFile.cannot.be.null")); //$NON-NLS-1$
		}
		if (this.indexPath == null) {
			throw new BuildException(AntAdapterMessages.getString("buildJarIndex.indexFile.cannot.be.null")); //$NON-NLS-1$
		}

		try {
			JavaIndexer.generateIndexForJar(this.jarPath, this.indexPath);
		} catch (IOException e) {
			throw new BuildException(AntAdapterMessages.getString("buildJarIndex.ioexception.occured", e.getLocalizedMessage())); //$NON-NLS-1$
		}
	}

	public void setJarPath(String path) {
		this.jarPath = path;
	}

	public void setIndexPath(String path) {
		this.indexPath = path;
	}
}
