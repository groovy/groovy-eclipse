/*
 * Copyright 2009-2023 the original author or authors.
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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode.VisitKind;
import org.codehaus.groovy.eclipse.core.compiler.GroovySnippetParser;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * Various AST related helpers.
 */
public class ASTTools {

    public static String getLineDelimeter(CompilationUnit unit) {
        try {
            return unit.findRecommendedLineSeparator();
        } catch (JavaModelException e) {
            return Util.LINE_SEPARATOR;
        }
    }

    /**
     * Returns a selection from the start of the first statement
     * to the end of the last statement in the block statement.
     */
    public static Region getPositionOfBlockStatements(BlockStatement block) {
        if (!block.getStatements().isEmpty()) {
            int start = getPositionOfStatement(block.getStatements().get(0)).getOffset(),
                until = getPositionOfStatement(block.getStatements().get(block.getStatements().size() - 1)).getEnd();
            if (start >= until) {
                throw new IllegalStateException(String.format(
                    "Block statement start offset (%d) >= end offset (%d)%nfirst statement: %s%nlast statement: %s",
                    start, until, block.getStatements().get(0),
                    block.getStatements().get(block.getStatements().size() - 1)));
            }
            return new Region(start, until - start);
        }
        return new Region(0, 0);
    }

    public static Region getPositionOfStatement(Statement statement) {
        if (!hasValidPosition(statement) && statement instanceof ReturnStatement) {
            ASTNode expression = ((ReturnStatement) statement).getExpression();
            return new Region(expression.getStart(), expression.getLength());
        }
        return new Region(statement.getStart(), statement.getLength());
    }

    /**
     * Returns true if the source location has been set to a valid value.
     */
    public static boolean hasValidPosition(ASTNode node) {
        return node.getEnd() > 0;
    }

    /**
     * Removes the leading space (tabs/spaces) in front of the first character.
     */
    public static String trimLeadingGap(String text) {
        return text.replaceFirst("[ \t\f]*", "");
    }

    /**
     * Returns the leading gap in front of the first character
     */
    public static String getLeadingGap(String text) {
        return text.replace(trimLeadingGap(text), "");
    }

    /**
     * Returns the given String with a changed intentation. Existing intentation
     * is replaced with space with the given modus.
     *
     * @param intentation
     *            given in "Tabs" <0 results in a movement on the left 0 has no
     *            effect >0 moves the text to the right
     * @param modus
     *            fill the leading space with: ASTTools.SPACE or ASTTools.TAB.
     *            Tab is the default behaviour.
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

    public static final int SPACE = 1;
    public static final int TAB = 2;

    /**
     * Returns the current Intentation of this string measured in "Tabs".
     */
    public static int getCurrentIntentation(String line) {
        String leadingGap = getLeadingGap(line);
        int tabs = 0, spaces = 0;
        for (int i = 0, n = leadingGap.length(); i < n; i += 1) {
            switch (leadingGap.charAt(i)) {
            case '\t':
                tabs += 1;
                break;
            case ' ':
                spaces += 1;
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
                if (delimiter != null && delimiter.length() > 0 && !delimiter.equals(linebreak)) {
                    IRegion region = document.getLineInformation(i);
                    multiEdit.addChild(
                        new ReplaceEdit(region.getOffset() + region.getLength(), delimiter.length(), linebreak));
                }
            }
            multiEdit.apply(document);
        } catch (Exception ignore) {
        }
        return document;
    }

    public static ModuleNode getASTNodeFromSource(String source) {
        GroovySnippetParser parser = new GroovySnippetParser();
        ModuleNode node = parser.parse(source);
        return node;
    }

    public static boolean hasMultipleReturnStatements(Statement statement) {
        List<ReturnStatement> returns = new ArrayList<>();
        statement.visit(new FindReturns(returns));
        return returns.size() > 1;
    }

    public static String getTextofNode(ASTNode node, IDocument document) {
        TextSelection sel = new TextSelection(document, node.getStart(), node.getEnd() - node.getStart());
        try {
            return document.get(sel.getOffset(), sel.getLength());
        } catch (BadLocationException e) {
            return "";
        }
    }

    public static Set<Variable> getVariablesInScope(ModuleNode moduleNode, ASTNode node) {
        FindSurroundingNode find = new FindSurroundingNode(new Region(node), VisitKind.PARENT_STACK);
        find.doVisitSurroundingNode(moduleNode);
        List<IASTFragment> parentStack = new ArrayList<>(find.getParentStack());
        Collections.reverse(parentStack);

        Set<Variable> vars = new HashSet<>();
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
        ClassNode containingClassNode = null, scriptClass = null;
        for (ClassNode classNode : moduleNode.getClasses()) {
            if (GroovyUtils.isScript(classNode)) {
                scriptClass = classNode;
            } else if (classNode.getStart() <= offset && classNode.getEnd() >= offset) {
                containingClassNode = classNode;
            }
        }
        if (containingClassNode == null) {
            if (scriptClass != null && scriptClass.getStart() <= offset && scriptClass.getEnd() >= offset) {
                containingClassNode = scriptClass;
            } else {
                // ensure this method never returns null
                containingClassNode = moduleNode.getScriptClassDummy();
            }
        } else {
            // look for inner classes
            for (Iterator<InnerClassNode> it = containingClassNode.getInnerClasses(); it.hasNext();) {
                InnerClassNode innerClass = it.next();
                if (innerClass.getStart() <= offset && innerClass.getEnd() >= offset && !innerClass.isSynthetic()) {
                    containingClassNode = innerClass;
                    it = innerClass.getInnerClasses();
                }
            }
        }
        return containingClassNode;
    }

    public static IASTFragment getSelectionFragment(ModuleNode moduleNode, int selectionStart, int selectionLength) {
        FindSurroundingNode finder =
            new FindSurroundingNode(new Region(selectionStart, selectionLength), VisitKind.SURROUNDING_NODE);

        IASTFragment selectionFragment = null;
        IASTFragment fragment = finder.doVisitSurroundingNode(moduleNode);
        if (ASTFragmentKind.isExpressionKind(fragment)) {
            selectionFragment = fragment;
        }
        return selectionFragment;
    }

    private static class FindReturns extends ASTVisitorDecorator<List<ReturnStatement>> {
        FindReturns(List<ReturnStatement> container) {
            super(container);
        }

        @Override
        public void visitReturnStatement(ReturnStatement statement) {
            container.add(statement);
            super.visitReturnStatement(statement);
        }
    }
}
