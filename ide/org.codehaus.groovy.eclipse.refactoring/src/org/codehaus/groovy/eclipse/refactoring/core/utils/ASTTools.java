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
package org.codehaus.groovy.eclipse.refactoring.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ASTNodeCompatibilityWrapper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode.VisitKind;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Various AST related helpers
 * @author mike klenk
 *
 */

public class ASTTools {

	public static final int SPACE = 1;
	public static final int TAB = 2;

    /**
     * Returns a selection from the start of the first statement
     * to the end of the last statement in the block statement
     *
     * @param block
     * @param doc
     * @return
     */
    public static Region getPositionOfBlockStatements(BlockStatement block) {
		int startPosition, endPosition;
        Region methodStatements = new Region(0, 0);

		if (block.getStatements().size() > 0) {
			// Set relative position of the new statement block
			Statement firstStmt = (Statement) block.getStatements().get(0);
			Statement lastStmt = (Statement) block.getStatements().get(
					block.getStatements().size() - 1);

			// solve false line information in non explicit
			// return statement
			if (firstStmt instanceof ReturnStatement
					&& firstStmt.getLineNumber() == -1) {
				Expression exp = ((ReturnStatement) firstStmt).getExpression();
				startPosition = exp.getStart();
			} else {
				startPosition = firstStmt.getStart();
			}
			if (lastStmt instanceof ReturnStatement
					&& lastStmt.getLineNumber() == -1) {
				Expression exp = ((ReturnStatement) lastStmt).getExpression();
				endPosition = exp.getEnd();
			} else {
				endPosition = lastStmt.getEnd();
			}
            methodStatements = new Region(startPosition, endPosition - startPosition);

		}
		return methodStatements;
	}

    /**
     * Return true if the source location has been set to a
     * valid value
     *
     * @param node
     * @return
     */
	public static boolean hasValidPosition(ASTNode node) {
        return node.getEnd() > 0;
	}

	/**
	 * Remove the leading space (tabs/spaces) in front of the first character
	 *
	 * @param text
	 * @return
	 */
	public static String trimLeadingGap(String text) {
		return text.replaceFirst("[ \t\f]*", "");
	}

	/**
	 * Returns the leading gap in front of the first character
	 *
	 * @param text
	 * @return
	 */
	public static String getLeadingGap(String text) {
		return text.replace(trimLeadingGap(text), "");
	}

	/**
	 * Returns the given String with a changed intentation. Existing intentation
	 * is replaced with space with the given modus.
	 *
	 * @param text
	 * @param intentation
	 *            given in "Tabs" <0 results in a movement on the left 0 has no
	 *            effect >0 moves the text to the right
	 * @param modus
	 *            fill the leading space with: ASTTools.SPACE or ASTTools.TAB.
	 *            Tab is the default behaviour.
	 * @return
	 */
	public static String setIndentationTo(String text, int intentation, int modus) {

		StringBuilder retString = new StringBuilder();

		// Compile the pattern
		String patternStr = ".+?(\n|\r\n|\r|\\z)";
		Pattern pattern = Pattern.compile(patternStr, Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);

		// Read the lines
		while (matcher.find()) {

			// Get the line with the line termination character sequence
			String line = matcher.group(0);

			int currentIntetnation = getCurrentIntentation(line);
			String space;
			switch (modus) {
				case SPACE:
					space = "    ";
					break;
				default:
					space = "\t";
					break;
			}
			for (int i = 0; i < (currentIntetnation + intentation); i++) {
				retString.append(space);
			}
			retString.append(trimLeadingGap(line));
		}
		return retString.toString();
	}

	/**
	 * Return the current Intentation of this string measured in "Tabs"
	 *
	 * @param line
	 * @return
	 */
	public static int getCurrentIntentation(String line) {
		String leadingGap = getLeadingGap(line);
		int tabs = 0, spaces = 0;
		for (int i = 0; i < leadingGap.length(); i++) {
			switch (leadingGap.charAt(i)) {
				case '\t':
					tabs++;
					break;
				case ' ':
					spaces++;
					break;
				default:
					break;
			}
		}
		int currentIntetnation = tabs + (spaces / 4);
		return currentIntetnation;
	}

	public static IDocument getDocumentWithSystemLineBreak(String text) {

		Document document = new Document();
		String linebreak = document.getDefaultLineDelimiter();
		document.set(text);

		try {
			int lineCount = document.getNumberOfLines();
			MultiTextEdit multiEdit = new MultiTextEdit();
			for (int i = 0; i < lineCount; i++) {
				final String delimiter = document.getLineDelimiter(i);
				if (	delimiter != null &&
						delimiter.length() > 0 &&
						!delimiter.equals(linebreak)) {
					IRegion region = document.getLineInformation(i);
					multiEdit.addChild(new ReplaceEdit(region.getOffset()
									+ region.getLength(), delimiter.length(),
									linebreak));
				}
			}
			multiEdit.apply(document);
		} catch (Exception e) {
		}
		return document;
	}

	public static ModuleNode getASTNodeFromSource(String source) {
		GroovySnippetParser parser = new GroovySnippetParser();
		ModuleNode node = parser.parse(source);
		return node;
	}

	public static boolean hasMultipleReturnStatements(Statement statement) {
		List<ReturnStatement> returns = new ArrayList<ReturnStatement>();
		statement.visit(new FindReturns(returns));
		return returns.size() > 1;
	}

	private static class FindReturns extends ASTVisitorDecorator<List<ReturnStatement>> {

		public FindReturns(List<ReturnStatement> container) {
			super(container);
		}

		@Override
        public void visitReturnStatement(ReturnStatement statement) {
			container.add(statement);
			super.visitReturnStatement(statement);
		}

	}

	public static String getTextofNode(ASTNode node, IDocument document) {
		TextSelection sel = new TextSelection(document, node.getStart(), node.getEnd()-node.getStart());
		try {
			return document.get(sel.getOffset(), sel.getLength());
		} catch (BadLocationException e) {
			return "";
		}
	}

    public static Set<Variable> getVariablesInScope(ModuleNode moduleNode, ASTNode node) {
        FindSurroundingNode find = new FindSurroundingNode(new Region(node), VisitKind.PARENT_STACK);
        find.doVisitSurroundingNode(moduleNode);
        List<IASTFragment> parentStack = new ArrayList<IASTFragment>(find.getParentStack());
        Collections.reverse(parentStack);

        Set<Variable> vars = new HashSet<Variable>();
        for (IASTFragment fragment : parentStack) {
            ASTNode astNode = fragment.getAssociatedNode();
            VariableScope scope;
            if (astNode instanceof BlockStatement) {
                scope = ((BlockStatement) astNode).getVariableScope();
            } else if (astNode instanceof MethodNode) {
                scope = ((MethodNode) astNode).getVariableScope();
            } else if (astNode instanceof ClosureExpression) {
                scope = ((ClosureExpression) astNode).getVariableScope();
            } else {
                scope = null;
            }
            if (scope != null) {
                Iterator<Variable> declaredVariables = scope.getDeclaredVariablesIterator();
                while (declaredVariables.hasNext()) {
                    vars.add(declaredVariables.next());
                }
            }
        }

        return vars;
    }

    public static ClassNode getContainingClassNode(ModuleNode moduleNode, int offset) {
        ClassNode containingClassNode = null;
        ClassNode scriptClass = null;
        List<ClassNode> classes = moduleNode.getClasses();
        for (ClassNode clazz : (Iterable<ClassNode>) classes) {
            if (clazz.isScript()) {
                scriptClass = clazz;
            } else {
                if (clazz.getStart() <= offset && clazz.getEnd() >= offset) {
                    containingClassNode = clazz;
                }
            }
        }
        if (containingClassNode == null) {
            if (scriptClass != null && scriptClass.getStart() <= offset && scriptClass.getEnd() >= offset) {
                containingClassNode = scriptClass;
            } else {
                // ensure this method never returns null
                containingClassNode = ASTNodeCompatibilityWrapper.getScriptClassDummy(moduleNode);
            }
        } else {
            // look for inner classes
            Iterator<InnerClassNode> innerClasses = ASTNodeCompatibilityWrapper.getInnerClasses(containingClassNode);
            while (innerClasses != null && innerClasses.hasNext()) {
                InnerClassNode inner = innerClasses.next();
                if (inner.getStart() <= offset && inner.getEnd() >= offset) {
                    containingClassNode = inner;
                    innerClasses = ASTNodeCompatibilityWrapper.getInnerClasses(inner);
                }
            }
        }
        return containingClassNode;
    }

    public static IASTFragment getSelectionFragment(ModuleNode moduleNode, int selectionStart, int selectionLength) {
        FindSurroundingNode finder = new FindSurroundingNode(new Region(selectionStart, selectionLength),
                VisitKind.SURROUNDING_NODE);

        IASTFragment selectionFragment = null;
        IASTFragment fragment = finder.doVisitSurroundingNode(moduleNode);
        if (ASTFragmentKind.isExpressionKind(fragment)) {
            selectionFragment = fragment;
        }
        return selectionFragment;
    }
}
