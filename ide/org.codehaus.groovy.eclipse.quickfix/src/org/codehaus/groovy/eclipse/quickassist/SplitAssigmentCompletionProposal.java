/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist;

import static org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService.getLineLeadingWhiteSpace;
import static org.eclipse.jdt.internal.corext.codemanipulation.StubUtility.getLineDelimiterPreference;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Splits a variable declaration with an initializer into separate declaration and initialization statements.
 * <p>
 * Ex: "def a = b + c" becomes "def a\na = b + c"
 */
public class SplitAssigmentCompletionProposal extends AbstractGroovyTextCompletionProposal {

    private VariableExpression var;

    public SplitAssigmentCompletionProposal(IInvocationContext context) {
        super(context);
    }

    public String getDisplayString() {
        return "Split variable declaration";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_LOCAL;
    }

    public boolean hasProposals() {
        Region region = new Region(context.getSelectionOffset(), context.getSelectionLength());
        GroovyCompilationUnit unit = getGroovyCompilationUnit();
        ASTNode node = new ASTNodeFinder(region).doVisit(unit.getModuleNode());

        if (node instanceof DeclarationExpression) {
            DeclarationExpression expr = (DeclarationExpression) node;
            if (expr.getLeftExpression() instanceof VariableExpression && expr.getRightExpression() != null) {
                var = (VariableExpression) expr.getLeftExpression();
                return true;
            }
        } else if (node instanceof VariableExpression) {
            var = (VariableExpression) node;
            if (var == var.getAccessedVariable() && hasInitialExpression(var, getGroovyCompilationUnit())) {
                return true;
            }
        }
        return false;
    }

    protected boolean hasInitialExpression(VariableExpression expr, GroovyCompilationUnit unit) {
        char[] contents = unit.getContents();
        int offset = expr.getEnd();
        char c = ' ';
        while (offset < contents.length && CharOperation.isWhitespace(c = contents[offset])) {
            offset += 1;
        }
        return (c == '=');
    }

    protected String getLineDelimiter(IDocument document, int line) throws BadLocationException {
        String nl;
        while ((nl = document.getLineDelimiter(line)) == null && line > 1) {
            line -= 1;
        }
        if (nl == null) {
            nl = getLineDelimiterPreference(getProject());
        }
        return nl;
    }

    @Override
    protected TextEdit getTextEdit(IDocument document) throws BadLocationException {
        int offset = var.getEnd();
        int lineNo = document.getLineOfOffset(offset);
        String insertion = getLineDelimiter(document, lineNo) + getLineLeadingWhiteSpace(document, lineNo) + var.getText();

        return new InsertEdit(offset, insertion);
    }
}
