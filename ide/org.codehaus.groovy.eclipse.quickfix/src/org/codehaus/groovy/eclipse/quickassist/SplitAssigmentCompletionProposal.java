/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.refactoring.formatter.GroovyIndentationService;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Split declarations
 * 
 * @author Nick Sawadsky nsawadsky@gmail.com
 * @created Oct 24, 2011
 */
public class SplitAssigmentCompletionProposal extends
        AbstractGroovyCompletionProposal {
	
	private final GroovyCompilationUnit unit;
    private final int length;
    private final int offset;
    
	private DeclarationExpression expr;
	
    public SplitAssigmentCompletionProposal(IInvocationContext context) {
        super(context);
        ICompilationUnit compUnit = context.getCompilationUnit();
        if (compUnit instanceof GroovyCompilationUnit) {
            this.unit = (GroovyCompilationUnit) compUnit;
        } else {
            this.unit = null;
        }
        length = context.getSelectionLength();
        offset = context.getSelectionOffset();
        
    }

    public int getRelevance() {
        return 0;
    }

    public void apply(IDocument document) {
    	
    	Expression left = expr.getLeftExpression();
    	int insertAt = left.getEnd();
    	
        try {
        	
        	int lineNr = document.getLineOfOffset(insertAt);
        	String space = GroovyIndentationService.getLineLeadingWhiteSpace(document, lineNr);
        	TextEdit edits = (new ReplaceEdit(insertAt, 0, "\n" + space +left.getText()));

            if (edits != null) {
                edits.apply(document);
            }
        } catch (Exception e) {
            GroovyCore.logException("Oops.", e);
        }
    }

    public Point getSelection(IDocument document) {
        // this is not right.  We should be updating the position based on the text changes
        return new Point(offset, length+offset);
    }

    public String getAdditionalProposalInfo() {
        return getDisplayString();
    }

    public String getDisplayString() {
        return "Split declaration";
    }

    public IContextInformation getContextInformation() {
        return new ContextInformation(getImage(), getDisplayString(), getDisplayString());
    }

    @Override
    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_CHANGE;
    }

    @Override
    public boolean hasProposals() {
        if (unit == null) {
            return false;
        }
        Region region = new Region(offset, length);
        ASTNodeFinder finder = new ASTNodeFinder(region);
        ModuleNode moduleNode = unit.getModuleNode();
        
        ASTNode node = finder.doVisit(moduleNode);
        
        if (node instanceof DeclarationExpression) {
        	expr = (DeclarationExpression)node;
        	return expr.getRightExpression() != null && !(expr.getLeftExpression() instanceof TupleExpression);            
        }
        return false;
    }
}
