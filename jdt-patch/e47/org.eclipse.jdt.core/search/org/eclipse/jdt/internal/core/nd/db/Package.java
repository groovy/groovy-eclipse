/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.db;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

/**
 * This class is not intended to be referenced by clients
 */
/* package */ class Package {
	public static String PLUGIN_ID = JavaCore.PLUGIN_ID;

	/**
	 * Status code for core exception that is thrown if a database grew larger than the supported limit.
	 */
	public static final int STATUS_DATABASE_TOO_LARGE = 4;

	public static void log(Throwable e) {
		String msg= e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	public static void log(IStatus status) {
		JavaCore.getPlugin().getLog().log(status);
	}
}
