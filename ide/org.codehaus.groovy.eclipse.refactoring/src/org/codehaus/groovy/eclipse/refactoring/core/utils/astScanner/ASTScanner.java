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

import java.util.LinkedHashMap;
import java.util.Stack;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IASTNodePredicate;
import org.eclipse.jface.text.IDocument;

public class ASTScanner extends ASTScannerPredicate {

	protected IDocument document;
	private final LinkedHashMap<ASTNode, ASTNodeInfo> astMap;
	private final Stack<ASTNode> nodeStack;
	
	
	public ASTScanner(ModuleNode rootNode, IASTNodePredicate predicate, IDocument document) {
		super(rootNode, predicate);
		this.document = document;
		astMap = new LinkedHashMap<ASTNode, ASTNodeInfo>();
		nodeStack = new Stack<ASTNode>();
	}
	
	public void startASTscan() {
		if (astMap.isEmpty()) {
			scanAST();
		}
	}

	public ASTNodeInfo getInfo(ASTNode node) throws NodeNotFoundException {
		ASTNodeInfo ret = astMap.get(node);
		if(ret != null){
			return ret;
		}
        if(node != null){
        	throw new NodeNotFoundException(node.getText());
        }
        throw new NodeNotFoundException("node was NULL");
	}
	
	public LinkedHashMap<ASTNode, ASTNodeInfo> getMatchedNodes() {
		return astMap;
	}

	@Override
    protected void doOnPredicate(ASTNode node) {
		ASTNodeInfo info = new ASTNodeInfo();
		if(!nodeStack.isEmpty() && nodeStack.peek() != node)
			info.setParent(nodeStack.peek());
		if(ASTTools.hasValidPosition(node)) {
			info.setOffset(node.getStart());
			info.setLength(node.getEnd() - node.getStart());
		}
		
		astMap.put(node, info);
		nodeStack.push(node);
	}
	
	@Override
    protected void clear(ASTNode node) {
		if(!nodeStack.isEmpty() && nodeStack.peek() == node)
			nodeStack.pop();
	}

	public boolean hasMatches() {
		return !astMap.isEmpty();
	}	
}
