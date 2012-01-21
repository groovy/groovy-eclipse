/*
 * Copyright 2012 SpringSource, a division of VMware, Inc
 * 
 * Daniel and Stephanie - Initial API and implementation
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
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.CharOperation;
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
 * Exchanges left and right binary infix operands. eg. (a && b) becomes (b && a)
 * 
 * @author Daniel Phan d3phan@uwaterloo.ca
 * @author Stephanie Van Dyk sevandyk@gmail.com
 * @created Jan 20, 2012
 */
public class SwapOperandsCompletionProposal extends
		AbstractGroovyCompletionProposal {

	private final GroovyCompilationUnit unit;
	private final int length;
	private final int offset;

	private BinaryExpression binaryExpression;

	public SwapOperandsCompletionProposal(IInvocationContext context) {
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
		return new Point(offset, 0);
	}

	public String getAdditionalProposalInfo() {
		return getDisplayString();
	}

	public String getDisplayString() {
		return "Swap infix operands.";
	}

	public IContextInformation getContextInformation() {
		return new ContextInformation(getImage(), getDisplayString(),
				getDisplayString());
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
		ASTNodeFinder finder = new ASTNodeFinder(region);
		ModuleNode moduleNode = unit.getModuleNode();

		ASTNode node = finder.doVisit(moduleNode);

		if (node instanceof BinaryExpression) {
			BinaryExpression expr = (BinaryExpression) node;
			Token operation = expr.getOperation();

			if (isApplicableInfixOperator(operation.getText())) {
				binaryExpression = expr;
				result = true;
			}
		}

		return result;
	}

	private TextEdit findReplacement(IDocument doc) {
		try {
			return createEdit(doc, binaryExpression.getLeftExpression(),
					binaryExpression.getRightExpression());
		} catch (Exception e) {
			GroovyCore.logException(
					"Exception during swapping infix operands.", e);
			return null;
		}
	}

	private TextEdit createEdit(IDocument doc, Expression left, Expression right)
			throws BadLocationException {
		TextEdit edit = new MultiTextEdit();

		int leftStart = left.getStart();
		int rightStart = right.getStart();

		char[] contents = unit.getContents();
		char[] leftChars = CharOperation.subarray(contents, leftStart,
				left.getEnd());
		char[] rightChars = CharOperation.subarray(contents, rightStart,
				right.getEnd());

		String leftText = new String(leftChars).trim();
		String rightText = new String(rightChars).trim();

		edit.addChild(new ReplaceEdit(rightStart, rightText.length(), leftText));
		edit.addChild(new ReplaceEdit(leftStart, leftText.length(), rightText));

		return edit;
	}

	private boolean isApplicableInfixOperator(String test) {
		return test.equals("*") || test.equals("/") || test.equals("%")
				|| test.equals("+") || test.equals("-") || test.equals("<<")
				|| test.equals(">>") || test.equals(">>>") || test.equals("<")
				|| test.equals(">") || test.equals("<=") || test.equals(">=")
				|| test.equals("==") || test.equals("!=") || test.equals("&")
				|| test.equals("^") || test.equals("|") || test.equals("&&")
				|| test.equals("||");
	}
}
