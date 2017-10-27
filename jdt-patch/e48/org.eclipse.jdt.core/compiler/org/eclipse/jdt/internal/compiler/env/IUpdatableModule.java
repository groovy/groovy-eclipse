/*******************************************************************************
 * Copyright (c) 2017 GK Software AG, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.jdt.internal.compiler.util.SimpleSetOfCharArray;

/**
 * Interface to a module as needed to implement the updates for {@code --add-reads}
 * and {@code --add-exports} command line options (or corresponding classpath attributes).
 */
public interface IUpdatableModule {

	/**
	 * The compiler first wires modules only, before then wiring packages based on the module graph.
	 * This enum selects one of these phases when asking the environment to perform its updates.
	 */
	enum UpdateKind { MODULE, PACKAGE }

	/** Structure for update operations, sorted by {@link UpdateKind}. */
	class UpdatesByKind {
		List<Consumer<IUpdatableModule>> moduleUpdates = Collections.emptyList();
		List<Consumer<IUpdatableModule>> packageUpdates = Collections.emptyList();
		public List<Consumer<IUpdatableModule>> getList(UpdateKind kind, boolean create) {
			switch (kind) {
				case MODULE:
					if (this.moduleUpdates == Collections.EMPTY_LIST && create)
						this.moduleUpdates = new ArrayList<>();
					return this.moduleUpdates;
				case PACKAGE:
					if (this.packageUpdates == Collections.EMPTY_LIST && create)
						this.packageUpdates = new ArrayList<>();
					return this.packageUpdates;
				default:
					throw new IllegalArgumentException("Unknown enum value "+kind); //$NON-NLS-1$
			}
		}
	}

	/** Answer the name of the module to update. */
	char[] name();
	/** Perform an --add-reads update on the module. */
	void addReads(char[] moduleName);
	/** Perform an --add-exports update on the module. */
	void addExports(char[] packageName, char[][] targetModules);
	/** Define the ModuleMainClass to be recorded in the generated module-info.class. */
	void setMainClassName(char[] mainClassName);
	/** Passes names of packages to be recorded in the ModulePackages classfile attribute. */
	void setPackageNames(SimpleSetOfCharArray packageNames);
}
