/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.java;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

/**
 * Holds a location in the Eclipse workspace where a given resource was found. Note that a given
 * resource might be mapped to multiple locations in the workspace.
 */
public class NdWorkspaceLocation extends NdNode {
	public static final FieldManyToOne<NdResourceFile> RESOURCE;
	public static final FieldString PATH;

	@SuppressWarnings("hiding")
	public static final StructDef<NdWorkspaceLocation> type;

	static {
		type = StructDef.create(NdWorkspaceLocation.class, NdNode.type);
		RESOURCE = FieldManyToOne.createOwner(type, NdResourceFile.WORKSPACE_MAPPINGS);
		PATH = type.addString();
		type.done();
	}

	public NdWorkspaceLocation(Nd nd, long address) {
		super(nd, address);
	}

	public NdWorkspaceLocation(Nd nd, NdResourceFile resource, char[] path) {
		super(nd);

		RESOURCE.put(getNd(), this.address, resource);
		PATH.put(getNd(), this.address, path);
	}

	public IString getPath() {
		return PATH.get(getNd(), this.address);
	}

	public NdResourceFile getResourceFile() {
		return RESOURCE.get(getNd(), this.address);
	}

	@Override
	public String toString() {
		try {
			return getPath().toString();
		} catch (RuntimeException e) {
			// This is called most often from the debugger, so we want to return something meaningful even
			// if the code is buggy, the database is corrupt, or we don't have a read lock.
			return super.toString();
		}
	}
}
