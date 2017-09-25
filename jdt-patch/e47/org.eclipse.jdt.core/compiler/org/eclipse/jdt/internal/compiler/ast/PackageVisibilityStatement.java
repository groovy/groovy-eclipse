/*******************************************************************************
 * Copyright (c) 2016, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public abstract class PackageVisibilityStatement extends ModuleStatement {
	public ImportReference pkgRef;
	public ModuleReference[] targets;
	public char[] pkgName;
	public PackageBinding resolvedPackage;

	public PackageVisibilityStatement(ImportReference pkgRef, ModuleReference[] targets) {
		this.pkgRef = pkgRef;
		this.pkgName = CharOperation.concatWith(this.pkgRef.tokens, '.');
		this.targets = targets;
	}
	public boolean isQualified() {
		return this.targets != null && this.targets.length > 0;
	}
	
	public ModuleReference[] getTargetedModules() {
		return this.targets;
	}

	public boolean resolve(Scope scope) {
		boolean errorsExist = resolvePackageReference(scope) == null;
		if (this.isQualified()) {
			HashtableOfObject modules = new HashtableOfObject(this.targets.length);
			for (int i = 0; i < this.targets.length; i++) {
				ModuleReference ref = this.targets[i];
				if (modules.containsKey(ref.moduleName)) {
					scope.problemReporter().duplicateModuleReference(IProblem.DuplicateModuleRef, ref);
					errorsExist = true;
				} else {
					ref.resolve(scope.compilationUnitScope());
					modules.put(ref.moduleName, ref);
				}
			}
		}
		return !errorsExist;
	}
	protected int computeSeverity(int problemId) {
		return ProblemSeverities.Error;
	}
	protected PackageBinding resolvePackageReference(Scope scope) {
		if (this.resolvedPackage != null)
			return this.resolvedPackage;
		ModuleDeclaration exportingModule = scope.compilationUnitScope().referenceContext.moduleDeclaration;
		ModuleBinding src = exportingModule.binding;
		this.resolvedPackage = src != null ? src.getVisiblePackage(this.pkgRef.tokens) : null;
		int problemId = IProblem.PackageDoesNotExistOrIsEmpty;
		if (this.resolvedPackage == null) {
			// TODO: need a check for empty package as well
			scope.problemReporter().invalidPackageReference(problemId, this, computeSeverity(problemId));
		} else {
			if (!this.resolvedPackage.isDeclaredIn(src)) {
				this.resolvedPackage = null;
				// TODO(SHMOD): specific error?
				scope.problemReporter().invalidPackageReference(problemId, this, computeSeverity(problemId));
			}
		}
		
		return this.resolvedPackage;
	}

	@Override
	public StringBuffer print(int indent, StringBuffer output) {
		this.pkgRef.print(indent, output);
		if (this.isQualified()) {
			output.append(" to "); //$NON-NLS-1$
			for (int i = 0; i < this.targets.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				this.targets[i].print(0, output);
			}
		}
		return output;
	}
}
