/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

public class PackageExportImpl implements IModule.IPackageExport {
	public char[] pack;
	public char[][] exportedTo;
	@Override
	public char[] name() {
		return this.pack;
	}

	@Override
	public char[][] targets() {
		return this.exportedTo;
	}
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.pack);
		buffer.append(" to "); //$NON-NLS-1$
		if (this.exportedTo != null) {
			for (int i = 0; i < this.exportedTo.length; i++) {
				if (i > 0) {
					buffer.append(", "); //$NON-NLS-1$
				}
				char[] cs = this.exportedTo[i];
				buffer.append(cs);
			}
		}
		buffer.append(';');
		return buffer.toString();
	}
}