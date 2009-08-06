/* 
 * Copyright (C) 2007, 2009 Martin Kempf, Reto Kleeb, Michael Klenk
 *
 * IFS Institute for Software, HSR Rapperswil, Switzerland
 * http://ifs.hsr.ch/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IASTNodePredicate;

public abstract class ASTScannerPredicate extends RefactoringCodeVisitorSupport {

	protected IASTNodePredicate predicate;

	public ASTScannerPredicate(ModuleNode rootNode, IASTNodePredicate predicate) {
		super(rootNode);
		this.predicate = predicate;
	}

	@Override
    protected void analyzeNode(ASTNode node) {
		ASTNode evaluatedNode;
		if((evaluatedNode = predicate.evaluate(node)) != null) {
			doOnPredicate(evaluatedNode);
		}
	}
	
	@Override
    public void visitStaticFieldImport(StaticFieldImport staticAliasImport) {
		analyzeNode(staticAliasImport);
	}
	
	@Override
    public void visitStaticClassImport(StaticClassImport staticClassImport) {
		analyzeNode(staticClassImport);
	}
	
	@Override
    public void visitClassImport(ClassImport classImport) {
		analyzeNode(classImport);
	}

	abstract void doOnPredicate(ASTNode node);
}