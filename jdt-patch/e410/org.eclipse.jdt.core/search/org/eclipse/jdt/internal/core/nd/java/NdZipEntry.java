/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Stores a (non-class) file within a .jar file.
 */
public class NdZipEntry extends NdStruct {
	public static final FieldString FILE_NAME;

	@SuppressWarnings("hiding")
	public static final StructDef<NdZipEntry> type;

	static {
		type = StructDef.create(NdZipEntry.class, NdStruct.type);
		FILE_NAME = type.addString();

		type.done();
	}

	public NdZipEntry(Nd nd, long address) {
		super(nd, address);
	}

	public void setFilename(String filename) {
		FILE_NAME.put(this.nd, this.address, filename);
	}

	public IString getFileName() {
		return FILE_NAME.get(this.nd, this.address);
	}
}
