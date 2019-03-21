/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;
import org.eclipse.jdt.internal.antadapter.AntAdapterMessages;

/**
 * <p>An Ant task to find out if a class file or a jar contains debug attributes. If this is the case,
 * the property contains the value "has debug" after the call.
 * </p>
 * <p>
 * <code>&lt;eclipse.checkDebugAttributes property="hasDebug" file="${basedir}/bin/p/A.class"/&gt;</code>
 * </p>
 * <p>
 * For more information on Ant check out the website at https://jakarta.apache.org/ant/ .
 * </p>
 *
 * This is not intended to be subclassed by users.
 * @since 2.0
 */
@SuppressWarnings("rawtypes")
public final class CheckDebugAttributes extends Task {

	private String file;
	private String property;

	@Override
	public void execute() throws BuildException {
		if (this.file == null) {
			throw new BuildException(AntAdapterMessages.getString("checkDebugAttributes.file.argument.cannot.be.null")); //$NON-NLS-1$
		}
		if (this.property == null) {
			throw new BuildException(AntAdapterMessages.getString("checkDebugAttributes.property.argument.cannot.be.null")); //$NON-NLS-1$
		}
		try {
			boolean hasDebugAttributes = false;
			if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(this.file)) {
				IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(this.file, IClassFileReader.ALL);
				hasDebugAttributes = checkClassFile(classFileReader);
			} else {
				ZipFile jarFile = null;
				try {
					jarFile = new ZipFile(this.file);
				} catch (ZipException e) {
					throw new BuildException(AntAdapterMessages.getString("checkDebugAttributes.file.argument.must.be.a.classfile.or.a.jarfile"), e); //$NON-NLS-1$
				} finally {
					if (jarFile != null) {
						jarFile.close();
					}
				}
				for (Enumeration entries = jarFile.entries(); !hasDebugAttributes && entries.hasMoreElements(); ) {
					ZipEntry entry = (ZipEntry) entries.nextElement();
					if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(entry.getName())) {
						IClassFileReader classFileReader = ToolFactory.createDefaultClassFileReader(this.file, entry.getName(), IClassFileReader.ALL);
						hasDebugAttributes = checkClassFile(classFileReader);
					}
				}
			}
			if (hasDebugAttributes) {
				getProject().setUserProperty(this.property, "has debug"); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new BuildException(AntAdapterMessages.getString("checkDebugAttributes.ioexception.occured") + this.file, e); //$NON-NLS-1$
		}
	}

	private boolean checkClassFile(IClassFileReader classFileReader) {
		IMethodInfo[] methodInfos = classFileReader.getMethodInfos();
		for (int i = 0, max = methodInfos.length; i < max; i++) {
			ICodeAttribute codeAttribute = methodInfos[i].getCodeAttribute();
			if (codeAttribute != null && codeAttribute.getLineNumberAttribute() != null) {
				return true;
			}
		}
		return false;
	}

	public void setFile(String value) {
		this.file = value;
	}

	public void setProperty(String value) {
		this.property = value;
	}
}
