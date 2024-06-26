/*
 * Copyright 2009-2024 the original author or authors.
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

import static java.beans.Introspector.decapitalize;
import static java.util.regex.Pattern.compile;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.IMPLICIT_RECEIVER;
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR;
import static org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCall;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.internal.ui.util.ElementValidator;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class ConvertToPropertyAction extends Action {

    private final GroovyEditor editor;

    public ConvertToPropertyAction(final GroovyEditor editor) {
        this.editor = editor;
        setText("Replace Accessor call with Property read/write");
        setActionDefinitionId("org.codehaus.groovy.eclipse.ui.convertToProperty");
    }

    @Override
    public void run() {
        if (!editor.validateEditorInputState())
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

    public static TextEdit createEdit(final GroovyCompilationUnit gcu, final int idx, final int len) {
        ModuleNodeInfo info = gcu.getModuleInfo(true);
        if (!info.isEmpty()) {
            MethodCall call = null;

            ASTNode node = new ASTNodeFinder(new Region(idx, len)).doVisit(info.module);
            if (node instanceof ConstantExpression) {
                IASTFragment fragment = new FindSurroundingNode(new Region(node)).doVisitSurroundingNode(info.module);
                if (fragment.kind() == ASTFragmentKind.METHOD_CALL) {
                    MethodCallExpression expr = (MethodCallExpression) fragment.getAssociatedNode();
                    if (expr != null && !expr.isUsingGenerics()) {
                        call = expr;
                    }
                }
            } else if (node instanceof StaticMethodCallExpression) {
                call = (StaticMethodCallExpression) node;
            }

            if (call != null && call.getArguments() instanceof TupleExpression) {
                TupleExpression args = (TupleExpression) call.getArguments();

                Matcher match; // check for accessor or mutator
                if (args.getExpressions().isEmpty() && (match = compile("(?:get|is)(\\p{javaJavaIdentifierPart}+)").matcher(call.getMethodAsString())).matches()) {
                    int offset = node.getStart(), length = (args.getEnd() + 1) - offset;
                    String propertyName = match.group(1);

                    // replace "getPropertyName()" with "propertyName"
                    TextEdit edit = new ReplaceEdit(offset, length, decapitalize(propertyName));

                    // implicit-this call may require qualifier to retain its semantics
                    if (call.getReceiver().getEnd() < 1 && isTypeChange(edit, node, gcu)) {
                        edit = new ReplaceEdit(offset, length, getReceiver(call) + "." + decapitalize(propertyName));
                    }

                    return edit;

                } else if (args.getExpressions().size() == 1 && (match = compile("set(\\p{javaJavaIdentifierPart}+)").matcher(call.getMethodAsString())).matches()) {
                    int offset = node.getStart(), length = args.getStart() - offset;
                    String propertyName = match.group(1);

                    // replace "setPropertyName(value_expression)" or "setPropertyName value_expression"
                    // with "propertyName = value_expression" (check prefs for spaces around assignment)
                    MultiTextEdit edit = new MultiTextEdit();
                    Map<String, String> options = gcu.getJavaProject().getOptions(true);
                    StringBuilder replacement = new StringBuilder(decapitalize(propertyName));
                    if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_BEFORE_ASSIGNMENT_OPERATOR))) {
                        replacement.append(' ');
                    }
                    replacement.append('=');
                    if (JavaCore.INSERT.equals(options.get(FORMATTER_INSERT_SPACE_AFTER_ASSIGNMENT_OPERATOR))) {
                        replacement.append(' ');
                    }
                    if (args.getExpression(0) instanceof NamedArgumentListExpression) {
                        replacement.append('[');
                    }
                    edit.addChild(new ReplaceEdit(offset, length, replacement.toString()));

                    boolean rparen = (args.getEnd() < gcu.getContents().length && gcu.getContents()[args.getEnd()] == ')');
                    if (args.getExpression(0) instanceof NamedArgumentListExpression) {
                        edit.addChild(rparen ? new ReplaceEdit(args.getEnd(), 1, "]") : new InsertEdit(args.getEnd(), "]"));
                    } else if (rparen) {
                        edit.addChild(new DeleteEdit(args.getEnd(), 1));
                    }

                    // implicit-this call may require qualifier to retain its semantics
                    if (call.getReceiver().getEnd() < 1 && isTypeChange(edit, node, gcu)) {
                        edit.removeChild(0);
                        // add qualifier to the property name
                        replacement.insert(0, getReceiver(call) + ".");
                        edit.addChild(new ReplaceEdit(offset, length, replacement.toString()));
                    }

                    return edit;
                }
            }
        }
        return null;
    }

    //--------------------------------------------------------------------------

    private static String getReceiver(final MethodCall call) {
        String receiver = ((ASTNode) call).getNodeMetaData(IMPLICIT_RECEIVER);
        if (receiver == null) {
            receiver = call.getReceiver().getText();
        }
        return receiver;
    }

    private static boolean isTypeChange(final TextEdit edit, final ASTNode node, final GroovyCompilationUnit unit) {
        try {
            TypeLookupResult before = lookupNodeType(node, unit);
            TextEdit undo = unit.applyTextEdit(edit.copy(), null);
            TypeLookupResult after = lookupNodeType(new ASTNodeFinder(new Region(node.getStart(), 0)).doVisit(unit.getModuleNode()), unit);
            unit.applyTextEdit(undo, null);

            if (!Objects.equals(before.declaringType, after.declaringType) || (after.declaration instanceof Variable &&
                                !(after.declaration instanceof FieldNode || after.declaration instanceof PropertyNode))) {
                if (before.declaringType == null || !before.scope.getThis().isDerivedFrom(before.declaringType)) { // "this" or "super"
                    ASTNode call = (node instanceof MethodCall ? node : before.scope.getEnclosingNode()); // TODO: refactor side-effect solution!
                    call.getNodeMetaData(IMPLICIT_RECEIVER, x -> before.scope.getDelegate().equals(before.declaringType) ? "delegate" : "owner");
                }
                return true;
            }
        } catch (Exception e) {
            GroovyPlugin.getDefault().logError("Failure in convert to property type inference", e);
        }
        return false;
    }

    private static TypeLookupResult lookupNodeType(/*     */ final ASTNode node, final GroovyCompilationUnit unit) {
        TypeLookupResult[] result = new TypeLookupResult[1];
        new TypeInferencingVisitorFactory().createVisitor(unit).visitCompilationUnit((n, r, x) -> {
            if (n == node) {
                result[0] = r;
                return ITypeRequestor.VisitStatus.STOP_VISIT;
            }
            return ITypeRequestor.VisitStatus.CONTINUE;
        });
        return result[0];
    }
}
