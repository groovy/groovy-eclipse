/*******************************************************************************
 * Copyright (c) 2017, 2018 GK Software SE, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.provisional;

import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Provisional API for use by JDT/UI or JDT/Debug, which may possibly be removed in a future version.
 * See <a href="https://bugs.eclipse.org/522391">Bug 522391</a>. 
 */
public class JavaModelAccess {
	/**
	 * Answer the names of all modules directly required from the given module.
	 * @param module the module whose "requires" directives are queried
	 * @return a non-null array of module names
	 * @deprecated this provisional API has been promoted to {@link IModuleDescription#getRequiredModuleNames()}
	 */
	@Deprecated
	public static String[] getRequiredModules(IModuleDescription module) throws JavaModelException {
		return module.getRequiredModuleNames();
	}
}
