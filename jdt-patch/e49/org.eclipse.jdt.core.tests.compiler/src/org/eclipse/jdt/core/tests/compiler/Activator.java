/*******************************************************************************
 * Copyright (c) 2013 Stephan Herrmann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * Make the PackageAdmin service accessible to tests.
 * 
 * @deprecated uses deprecated class PackageAdmin.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Activator extends Plugin {

	private static final String PLUGIN_ID = "org.eclipse.jdt.core.tests.compiler";

	static org.osgi.service.packageadmin.PackageAdmin packageAdmin = null;


	public void start(BundleContext context) throws Exception {
		
		ServiceReference ref= context.getServiceReference(org.osgi.service.packageadmin.PackageAdmin.class.getName());
		if (ref!=null)
			packageAdmin = (org.osgi.service.packageadmin.PackageAdmin)context.getService(ref);
		else
			getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to load PackageAdmin service. Will not be able to access bundles org.eclipse.jdt.annotation."));
	}

	public void stop(BundleContext context) throws Exception {
		// nothing
	}

	public static org.osgi.service.packageadmin.PackageAdmin getPackageAdmin() {
		return packageAdmin;
	}
	
}
