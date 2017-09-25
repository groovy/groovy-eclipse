/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

/**
 * Abstract base class of AST nodes that represent module directives (added in JLS9 API).
 *
 * <pre>
 * ModuleDirective:
 *    {@link RequiresDirective}
 *    {@link ExportsDirective}
 *    {@link OpensDirective}
 *    {@link UsesDirective}
 *    {@link ProvidesDirective}
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @since 3.14
 */
public abstract class ModuleDirective extends ASTNode {

	ModuleDirective(AST ast) {
		super(ast);
		unsupportedBelow9();
	}
}
