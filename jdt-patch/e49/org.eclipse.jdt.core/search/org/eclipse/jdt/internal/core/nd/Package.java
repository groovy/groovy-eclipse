/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
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
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;

/* package */ class Package {
	public static String PLUGIN_ID = JavaCore.PLUGIN_ID;

	public static void log(Throwable e) {
		String msg = e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	public static void logInfo(String message) {
		log(createStatus(IStatus.INFO, message, null));
	}

	public static IStatus createStatus(int statusCode, String msg, Throwable e) {
		return new Status(statusCode, PLUGIN_ID, msg, e);
	}

	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	public static IStatus createStatus(String msg) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg);
	}

	public static void log(IStatus status) {
		JavaCore.getPlugin().getLog().log(status);
	}
}
