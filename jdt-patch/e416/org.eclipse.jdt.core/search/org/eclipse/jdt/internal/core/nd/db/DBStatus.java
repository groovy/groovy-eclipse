/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DBStatus extends Status {
	/**
	 * @param exception
	 */
	public DBStatus(IOException exception) {
		super(IStatus.ERROR, Package.PLUGIN_ID, 0, "IOException", exception); //$NON-NLS-1$
	}

	public DBStatus(String msg) {
		super(IStatus.ERROR, Package.PLUGIN_ID, 0, "Error", null); //$NON-NLS-1$
	}
}
