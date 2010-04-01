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
package org.codehaus.groovy.eclipse.refactoring.core.rename;

import java.util.Map.Entry;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.eclipse.refactoring.core.UserSelection;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.IGroovyFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceDocumentProvider;
import org.codehaus.groovy.eclipse.refactoring.core.documentProvider.WorkspaceFileProvider;
import org.codehaus.groovy.eclipse.refactoring.core.utils.SourceCodePoint;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTNodeInfo;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.ASTScanner;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.NodeNotFoundException;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.RenameSelectionInfoPredicate;

/**
 * dispatches a rename refactoring request
 * @author martin, reto
 *
 */
public class RenameDispatcher {
	
	protected IGroovyDocumentProvider docProvider;
	protected UserSelection selection;

	public RenameDispatcher(IGroovyDocumentProvider docProvider, UserSelection selection) {
		this.docProvider = docProvider;
		this.selection = selection;
	}
	

	public ASTNode getSelectedNode() {
		RenameSelectionInfoPredicate renameSelectionInfoPredicate = new RenameSelectionInfoPredicate(selection,docProvider.getDocument());
		ASTScanner infoBuilder = new ASTScanner(docProvider.getRootNode(), renameSelectionInfoPredicate,docProvider.getDocument());
		infoBuilder.startASTscan();
		
		//get the tightest selection, inner most node 
		ASTNode currentCandidate = null;
		for (Entry<ASTNode, ASTNodeInfo> entry : infoBuilder.getMatchedNodes().entrySet()){
			ASTNode key = entry.getKey();
			if(currentCandidate == null){
				currentCandidate = key;
			} else { 
				//test if node's startpoint is after selectedNode's startpoint
				SourceCodePoint nodeStartPoint = new SourceCodePoint(key,SourceCodePoint.BEGIN);
				SourceCodePoint selectedNodeStartPoint = new SourceCodePoint(currentCandidate,SourceCodePoint.BEGIN);
				if (nodeStartPoint.isAfter(selectedNodeStartPoint) || key instanceof FieldExpression) {
						currentCandidate = key;
				}
			}
		}
		//Dispatch on a ConstantExpression is not possible -> the parent can be a PropertyExpression,
		//AttributeExpression or MethodCallExpression
		if(currentCandidate instanceof ConstantExpression){
			currentCandidate = infoBuilder.getInfo(currentCandidate).getParent();
		}
		
		//A FieldExpression has just a reference on the accessed field. To be able to dispatch we must
		//separate the expression into a ClassNode or simply leave it as FieldExpression
		if(currentCandidate instanceof FieldExpression){
			FieldExpression fieldExpr = (FieldExpression) currentCandidate;
			String className = fieldExpr.getField().getDeclaringClass().getNameWithoutPackage();
			SourceCodePoint start = new SourceCodePoint(currentCandidate,SourceCodePoint.BEGIN);
			int offsetToClass = start.getOffset(docProvider.getDocument());
			int selectionEndOffset = selection.getOffset() + selection.getLength();
			int classEndOffset = offsetToClass + className.length();
			if (selection.getOffset() >= offsetToClass && (selectionEndOffset  <= classEndOffset)) {
				currentCandidate = fieldExpr.getField().getDeclaringClass();
			}
		}
		return currentCandidate;
	}
}
