/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents a Java module descriptor. The module description could either come from source or binary.
 * A simple module looks like the following:
 * <pre>
 * module my.module {
 * 		exports my.pack1;
 * 		exports my.pack2;
 * 		requires java.sql;
 * }
 * </pre>
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 3.14
 */
public interface IModuleDescription extends IMember, IAnnotatable {
	// empty block
}
