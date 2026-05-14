/*
 * Copyright 2009-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.refactoring.core.utils.ASTTools;
import org.codehaus.groovy.eclipse.refactoring.core.utils.astScanner.predicates.IASTNodePredicate;
import org.eclipse.jface.text.IDocument;

public class ASTScanner extends ASTScannerPredicate {

    private final Map<ASTNode, ASTNodeInfo> astMap = new LinkedHashMap<>();
    private final Deque<ASTNode> nodeStack = new ArrayDeque<>();
    protected IDocument document;

    public ASTScanner(final ModuleNode rootNode, final IASTNodePredicate predicate, final IDocument document) {
        super(rootNode, predicate);
        this.document = document;
    }

    public ASTNodeInfo getInfo(final ASTNode node) throws NodeNotFoundException {
        ASTNodeInfo ret = astMap.get(node);
        if (ret != null) {
            return ret;
        }
        if (node != null) {
            throw new NodeNotFoundException(node.getText());
        }
        throw new NodeNotFoundException("node was NULL");
    }

    public Map<ASTNode, ASTNodeInfo> getMatchedNodes() {
        return astMap;
    }

    public boolean hasMatches() {
        return !astMap.isEmpty();
    }

    public void startASTscan() {
        if (astMap.isEmpty()) {
            scanAST();
        }
    }

    //--------------------------------------------------------------------------

    @Override
    protected void doOnPredicate(final ASTNode node) {
        ASTNodeInfo info = new ASTNodeInfo();
        if (!nodeStack.isEmpty() && nodeStack.peek() != node) {
            info.setParent(nodeStack.peek());
        }
        if (ASTTools.hasValidPosition(node)) {
            info.setOffset(node.getStart());
            info.setLength(node.getEnd() - node.getStart());
        }
        astMap.put(node, info);
        nodeStack.push(node);
    }

    @Override
    protected void clear(final ASTNode node) {
        if (!nodeStack.isEmpty() && nodeStack.peek() == node) {
            nodeStack.pop();
        }
    }
}
