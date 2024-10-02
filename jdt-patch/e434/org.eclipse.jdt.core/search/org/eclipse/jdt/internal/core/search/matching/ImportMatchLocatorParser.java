// GROOVY PATCHED
/*******************************************************************************
 * Copyright (c) 2000, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

// GROOVY package->public
public class ImportMatchLocatorParser extends MatchLocatorParser {

	boolean reportImportMatch;

// GROOVY protected->public
public ImportMatchLocatorParser(ProblemReporter problemReporter, MatchLocator locator) {
	super(problemReporter, locator);
	this.reportImportMatch = this.patternFineGrain == 0 || (this.patternFineGrain & IJavaSearchConstants.IMPORT_DECLARATION_TYPE_REFERENCE) != 0;
}
@Override
protected void consumeStaticImportOnDemandDeclarationName() {
	super.consumeStaticImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
@Override
protected void consumeSingleModifierImportDeclarationName(int modifier) {
	super.consumeSingleModifierImportDeclarationName(modifier);
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
@Override
protected void consumeSingleTypeImportDeclarationName() {
	super.consumeSingleTypeImportDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}
@Override
protected void consumeTypeImportOnDemandDeclarationName() {
	super.consumeTypeImportOnDemandDeclarationName();
	if (this.reportImportMatch) {
		this.patternLocator.match(this.astStack[this.astPtr], this.nodeSet);
	}
}

}
