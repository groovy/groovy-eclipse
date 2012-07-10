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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Converts a single line string to a multiline string
 * 
 * @author Nick Sawadsky nsawadsky@gmail.com
 * @created Oct 24, 2011
 */
public class ConvertToMultiLineStringCompletionProposal extends
        AbstractGroovyCompletionProposal {
	
	private final GroovyCompilationUnit unit;
    private final int length;
    private final int offset;
    
    private Expression literal;
    public ConvertToMultiLineStringCompletionProposal(IInvocationContext context) {
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
        TextEdit thisEdit = findReplacement(document);
        try {
            if (thisEdit != null) {
                thisEdit.apply(document);
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
        return "Convert to multi-line string";
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
        boolean result = false;

        Region region = new Region(offset, length);
        ASTNodeFinder finder = new StringConstantFinder(region);
        ModuleNode moduleNode = unit.getModuleNode();
        
        ASTNode node = finder.doVisit(moduleNode);
        
        if ((node instanceof ConstantExpression && ((ConstantExpression) node).getValue() instanceof String) || node instanceof GStringExpression) {
        	Expression expr = (Expression) node;
    		char[] contents = unit.getContents();
    		int start = expr.getStart();
    		int end = expr.getEnd();
            char[] nodeText = new char[end - start];
    		System.arraycopy(contents, start, nodeText, 0, end - start);
    		
    		if (isStringLiteral(nodeText) && !isMultiLineString(String.valueOf(nodeText))) {
    			literal = expr;
    			result = true;
    		}
        }
        
        return result;
    }
    
    private boolean isStringLiteral(char[] nodeText) {
		return nodeText.length > 1 && 
				(nodeText[0] == '\'' || nodeText[0] == '"') && 
				(nodeText[nodeText.length-1] == '\'' || nodeText[nodeText.length-1] == '"');
	}

	private TextEdit findReplacement(IDocument doc) {
        try {
        	int startQuote = literal.getStart();
        	int endQuote = literal.getEnd()-1;
        	if (startQuote >= endQuote) {
        	    return null;
        	}
            return createEdit(doc, startQuote, endQuote);
        } catch (Exception e) {
            GroovyCore.logException("Exception during convert to multiline string.", e);
            return null;
        }
    }

    private TextEdit createEdit(IDocument doc, int startQuote, int endQuote) throws BadLocationException {
        if (startQuote < 0 || startQuote >= doc.getLength() || endQuote < 0 || endQuote >= doc.getLength()) {
            return null;
        }
        if (! (doc.getChar(startQuote) == '\'' || doc.getChar(startQuote) == '"' )) {
            return null;
        }
        if (! (doc.getChar(endQuote) == '\'' || doc.getChar(endQuote) == '"')) {
            return null;
        }
        char quoteChar = doc.getChar(startQuote);
        char skipChar = '\0';
        String replaceQuotes = new String(new char[] {quoteChar, quoteChar, quoteChar});
        TextEdit edit = new MultiTextEdit();
        edit.addChild(new ReplaceEdit(startQuote, 1, replaceQuotes));
        edit.addChild(new ReplaceEdit(endQuote, 1, replaceQuotes));
        
        // iterate through rest of list to unescape characters
        for (int i = startQuote+1; i < endQuote-1; i++ ) {
            if (doc.getChar(i) == '\\') {
                i++;
                if (doc.getChar(i) != skipChar) {
                    edit.addChild(new ReplaceEdit(i-1, 2, unescaped(doc.getChar(i))));
                }
            }
        }
        
        return edit;
    }
    
    private String unescaped(char escaped) {
        switch (escaped) {
            case 'u':
                // don't try to convert unicode characters
                return "u";
            case 't':
                return "\t";
            case 'b':
                return "\b";
            case 'n':
                return "\n";
            case 'r':
                return "\r";
            case 'f':
                // the \f character seems to cause errors in the editor
                return "\n";
            case '\'':
                return "'";
            case '"':
                return "\"";
            case '\\':
                return "\\";
        }
        
        // shouldn't get here
        return String.valueOf(escaped);
    }
    
    private boolean isMultiLineString(String test) {
    	return (test.startsWith("'''") && test.endsWith("'''")) || 
    	        (test.startsWith("\"\"\"") && test.endsWith("\"\"\""));
    }
    
}
