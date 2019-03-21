/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import static java.util.regex.Pattern.compile;

import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR;
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR;

import java.util.Map;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.core.utils.StringUtils;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.actions.ActionUtil;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class ConvertToPropertyAction extends Action {

    private final GroovyEditor editor;

    public ConvertToPropertyAction(GroovyEditor editor) {
        this.editor = editor;
        setText("Replace Accessor call with Property read/write");
        setActionDefinitionId("org.codehaus.groovy.eclipse.ui.convertToProperty");
    }

    public void run() {
        if (!ActionUtil.isEditable(editor))
            return;
        ISelection selection = editor.getSelectionProvider().getSelection();
        if (!(selection instanceof ITextSelection))
            return;
        GroovyCompilationUnit gcu = editor.getGroovyCompilationUnit();
        if (!ElementValidator.checkValidateEdit(gcu, editor.getSite().getShell(), "Convert to Property"))
            return;
        try {
            TextEdit edit = createEdit(gcu, ((ITextSelection) selection).getOffset(), ((ITextSelection) selection).getLength());
            if (edit != null) {
                gcu.applyTextEdit(edit, null);
            }
        } catch (Exception e) {
            GroovyPlugin.getDefault().logError("Failure in convert to property", e);
        }
    }

    public static TextEdit createEdit(GroovyCompilationUnit gcu, int pos, int len) {
        ModuleNodeInfo info = gcu.getModuleInfo(true);
        if (!info.isEmpty()) {
            ASTNode node = new ASTNodeFinder(new Region(pos, len)).doVisit(info.module);
            if (node instanceof ConstantExpression) {
                IASTFragment fragment = new FindSurroundingNode(new Region(node)).doVisitSurroundingNode(info.module);
                if (fragment.kind() == ASTFragmentKind.METHOD_CALL) {
                    MethodCallExpression call = (MethodCallExpression) fragment.getAssociatedNode();
                    if (call != null && !call.isUsingGenerics() && call.getArguments() instanceof ArgumentListExpression) {
                        ArgumentListExpression args = (ArgumentListExpression) call.getArguments();

                        Matcher match; // check for accessor or mutator
                        if (args.getExpressions().isEmpty() && (match = compile("(?:get|is)(\\p{javaJavaIdentifierPart}+)").matcher(call.getMethodAsString())).matches()) {
                            int offset = node.getStart(),
                                length = (args.getEnd() + 1) - offset;
                            String propertyName = match.group(1);

                            // replace "getPropertyName()" with "propertyName"
                            return new ReplaceEdit(offset, length, StringUtils.uncapitalize(propertyName));

                        } else if (args.getExpressions().size() == 1 && (match = compile("set(\\p{javaJavaIdentifierPart}+)").matcher(call.getMethodAsString())).matches()) {
                            int offset = node.getStart(),
                                length = args.getStart() - offset;
                            String propertyName = match.group(1);

                            // replace "setPropertyName(value_expression)" or "setPropertyName value_expression"
                            // with "propertyName = value_expression" (check prefs for spaces around assignment)
                            MultiTextEdit edits = new MultiTextEdit();
                            Map<String, String> options = gcu.getJavaProject().getOptions(true);
                            StringBuilder replacement = new StringBuilder(StringUtils.uncapitalize(propertyName));
                            if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR)))
                                replacement.append(' ');
                            replacement.append('=');
                            if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR)))
                                replacement.append(' ');

                            edits.addChild(new ReplaceEdit(offset, length, replacement.toString()));
                            if (gcu.getContents()[args.getEnd()] == ')') edits.addChild(new DeleteEdit(args.getEnd(), 1));

                            return edits;
                        }
                    }
                }
            }
        }
        return null;
    }
}
