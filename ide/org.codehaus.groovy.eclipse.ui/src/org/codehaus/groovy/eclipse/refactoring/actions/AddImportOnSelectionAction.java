/*
 * Copyright 2009-2020 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.ASTFragmentKind;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.refactoring.actions.TypeSearch.UnresolvedTypeData;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeInferencingVisitorFactory;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.internal.corext.CorextMessages;
import org.eclipse.jdt.internal.corext.codemanipulation.AddImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.internal.ui.JavaUIStatus;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.TextEdit;

public class AddImportOnSelectionAction extends AddImportOnSelectionAdapter {

    public AddImportOnSelectionAction(final CompilationUnitEditor editor) {
        super(editor);
    }

    @Override
    protected AddImportOperation newAddImportOperation(final GroovyCompilationUnit compilationUnit, final ITextSelection textSelection, final IChooseImportQuery typeQuery) {
        return new AddImportOperation() {
            private IStatus fStatus = Status.OK_STATUS;

            @Override
            public IStatus getStatus() {
                return fStatus;
            }

            @Override
            public void run(final IProgressMonitor monitor) {
                SubMonitor submon = SubMonitor.convert(monitor, CodeGenerationMessages.AddImportsOperation_description, 4);
                try {
                    ModuleNodeInfo info = compilationUnit.getModuleInfo(true);
                    if (info.isEmpty()) {
                        fStatus = Status.CANCEL_STATUS;
                        return;
                    }
                    submon.worked(1);

                    ImportRewrite importRewrite = CodeStyleConfiguration.createImportRewrite(compilationUnit, true);
                    TextEdit edit = evaluateEdits(info.module, importRewrite, submon.split(1));
                    if (edit == null) {
                        return;
                    }

                    MultiTextEdit result = new MultiTextEdit();
                    result.addChild(edit);
                    result.addChild(importRewrite.rewriteImports(submon.split(1)));
                    applyEdit(compilationUnit, result, true, submon.split(1));
                } catch (CoreException e) {
                    fStatus = e.getStatus();
                } catch (OperationCanceledException cancel) {
                    if (fStatus == Status.OK_STATUS)
                        fStatus = Status.CANCEL_STATUS;
                }
            }

            private TextEdit evaluateEdits(final ModuleNode moduleNode, final ImportRewrite importRewrite, final IProgressMonitor monitor)
                    throws CoreException {
                Region selectRegion = new Region(textSelection.getOffset(), textSelection.getLength());
                ASTNodeFinder nodeFinder = new ASTNodeFinder(selectRegion);
                ASTNode node = nodeFinder.doVisit(moduleNode);
                if (node != null) {
                    if (node instanceof VariableExpression) {
                        // part of object expression "Pattern p = ..." or part of init expression "def p = Pattern.compile(...)"
                        TypeNameMatch choice = findCandidateTypes(((VariableExpression) node).getName(), node.getStart(), monitor);
                        importRewrite.addImport(choice.getFullyQualifiedName());
                        return new RangeMarker(node.getStart(), node.getLength());
                    }
                    if (node instanceof ClassNode || node instanceof ClassExpression || node instanceof ConstructorCallExpression) {
                        // simple name like "List", partially-qualified name like "Map.Entry", or fully-qualified name like "java.util.regex.Pattern"
                        ClassNode type = componentType(node);
                        int typeStart = startOffset(!(node instanceof ConstructorCallExpression) ? node : type, nodeFinder);
                        if (moduleNode.getClasses().contains(type)) {
                            return null; // skip type in same unit
                        }

                        // check for unknown and unqualified type name
                        if (type.getName().equals(type.getNameWithoutPackage())) {
                            TypeNameMatch choice = findCandidateTypes(type.getName(), typeStart, monitor);
                            importRewrite.addImport(choice.getFullyQualifiedName());
                            return new RangeMarker(typeStart, type.getName().length());
                        }

                        // check for known but unqualified type name -- could be imported (explicit, on-demand, default, alias) or in the same package
                        String simple = type.getNameWithoutPackage().substring(type.getNameWithoutPackage().lastIndexOf('$') + 1);
                        String prefix = compilationUnit.getSource().substring(typeStart, selectRegion.getEnd());
                        if (simple.startsWith(prefix) && prefix.length() > 0) {
                            importRewrite.addImport(type.getName()); // redundant but user requested
                            return new RangeMarker(typeStart, type.getNameWithoutPackage().length());
                        }

                        // check for selection on the type's name or qualifier string
                        String source = compilationUnit.getSource().substring(typeStart, endOffset(node, nodeFinder));
                        int nameStart = typeStart + source.indexOf(GroovyUtils.splitName(type)[1]);
                        if (nameStart > typeStart) {
                            if (nameStart <= selectRegion.getEnd()) {
                                String result = importRewrite.addImport(type.getName().replace('$', '.'));
                                // result is fully-qualified name in case of conflict with another import
                                if (result.indexOf('.') > 0) {
                                    fStatus = JavaUIStatus.createError(IStatus.ERROR,
                                        CodeGenerationMessages.AddImportsOperation_error_importclash, null);
                                    return null;
                                }
                                return new DeleteEdit(typeStart, nameStart - typeStart);
                            }

                            Pattern pattern;
                            Matcher matcher;
                            String qualifier = GroovyUtils.splitName(type)[0].replace('$', '.');

                            if (prefix.length() > 0) {
                                // check for selection in fully-qualified name like 'java.lang.String' or 'java.util.Map.Entry'
                                pattern = Pattern.compile("^" + Pattern.quote(prefix) + "\\p{javaJavaIdentifierPart}*");
                                matcher = pattern.matcher(qualifier);
                                if (matcher.find()) {
                                    IType it = compilationUnit.getJavaProject().findType(matcher.group());
                                    if (it == null) return null; // selected 'java.lang' or whatever

                                    // selected 'java.util.Map' or similar
                                    importRewrite.addImport(matcher.group());
                                    return new DeleteEdit(typeStart, endOffsetMinus(selectRegion.getEnd()) - typeStart);
                                }
                            }

                            // expand prefix to include the complete identifier segment
                            prefix = compilationUnit.getSource().substring(typeStart, endOffsetPlus(selectRegion.getEnd()));

                            // check for selection in partially-qualified name like 'Map.Entry'
                            pattern = Pattern.compile("\\b" + Pattern.quote(prefix) + "$");
                            matcher = pattern.matcher(qualifier);
                            if (matcher.find()) {
                                importRewrite.addImport(qualifier);
                                // TODO: Is there ever a reason to delete anything?
                                return new RangeMarker(typeStart, nameStart - typeStart);
                            }
                        }
                    }
                    if (node instanceof ConstantExpression) {
                        // static references like "TimeUnit.SECONDS" or "Pattern.compile(...)"
                        IASTFragment fragment = new FindSurroundingNode(new Region(node)).doVisitSurroundingNode(moduleNode);
                        if (fragment.kind() == ASTFragmentKind.PROPERTY) {
                            Expression expr = fragment.getAssociatedExpression();
                            if (expr instanceof ClassExpression) {
                                importRewrite.addStaticImport(expr.getType().getName().replace('$', '.'), propertyName(node), true);
                                return new DeleteEdit(expr.getStart(), expr.getLength() + 1);
                            }
                            if (expr instanceof VariableExpression) {
                                TypeNameMatch choice = findCandidateTypes(((VariableExpression) expr).getName(), expr.getStart(), monitor);
                                importRewrite.addStaticImport(choice.getFullyQualifiedName(), node.getText(), true);
                                return new DeleteEdit(expr.getStart(), expr.getLength() + 1);
                            }
                        }
                        if (fragment.kind() == ASTFragmentKind.METHOD_CALL) {
                            MethodCallExpression call = (MethodCallExpression) fragment.getAssociatedNode();
                            if (call != null && !call.isUsingGenerics()) {
                                Expression expr = call.getObjectExpression();
                                if (expr instanceof ClassExpression) {
                                    importRewrite.addStaticImport(expr.getType().getName().replace('$', '.'), call.getMethodAsString(), false);
                                    return new DeleteEdit(expr.getStart(), call.getMethod().getStart() - expr.getStart());
                                }
                                if (expr instanceof VariableExpression) {
                                    TypeNameMatch choice = findCandidateTypes(((VariableExpression) expr).getName(), expr.getStart(), monitor);
                                    importRewrite.addStaticImport(choice.getFullyQualifiedName(), call.getMethodAsString(), false);
                                    return new DeleteEdit(expr.getStart(), call.getMethod().getStart() - expr.getStart());
                                }
                            }
                        }
                    }
                }
                return null;
            }

            private TypeNameMatch findCandidateTypes(final String typeName, final int typeStart, final IProgressMonitor monitor) throws CoreException {
                boolean isAnnotation = ('@' == compilationUnit.getContents()[Math.max(0, typeStart - 1)]);
                UnresolvedTypeData typeData = new UnresolvedTypeData(typeName, isAnnotation, new SourceRange(typeStart, typeName.length()));

                new TypeSearch().searchForTypes(compilationUnit, Collections.singletonMap(typeName, typeData), monitor);
                List<TypeNameMatch> typesFound = typeData.getFoundInfos();

                if (typesFound.isEmpty()) {
                    fStatus = JavaUIStatus.createError(IStatus.ERROR, CodeGenerationMessages.bind(
                            CodeGenerationMessages.AddImportsOperation_error_notresolved_message,
                            BasicElementLabels.getJavaElementName(typeName)
                        ), null);
                    throw new OperationCanceledException();
                }

                TypeNameMatch choice = typeQuery.chooseImport(typesFound.toArray(new TypeNameMatch[typesFound.size()]), typeName);
                if (choice == null) throw new OperationCanceledException();
                return choice;
            }

            private int startOffset(final ASTNode node, final ASTNodeFinder nodeFinder) throws CoreException {
                int start = node.getStart();
                if (node.getEnd() < 1) {
                    Region nodeRegion = ReflectionUtils.getPrivateField(ASTNodeFinder.class, "sloc", nodeFinder);
                    if (nodeRegion != null) {
                        start = nodeRegion.getOffset(); // may be approximate
                        while (!Character.isJavaIdentifierStart(compilationUnit.getSource().charAt(start))) {
                            start += 1;
                        }
                    }
                }
                return start;
            }

            private int endOffset(final ASTNode node, final ASTNodeFinder nodeFinder) throws CoreException {
                int end = node.getEnd();
                if (end < 1) {
                    Region nodeRegion = ReflectionUtils.getPrivateField(ASTNodeFinder.class, "sloc", nodeFinder);
                    if (nodeRegion != null) {
                        end = nodeRegion.getEnd(); // may be approximate
                        while (end > 0 && !Character.isJavaIdentifierPart(compilationUnit.getSource().charAt(end - 1))) {
                            end -= 1;
                        }
                    }
                }
                return end;
            }

            private int endOffsetPlus(int end) throws CoreException {
                while (Character.isJavaIdentifierPart(compilationUnit.getSource().charAt(end))) {
                    end += 1;
                }
                return end;
            }

            private int endOffsetMinus(int end) throws CoreException {
                while (end > 0 && Character.isJavaIdentifierPart(compilationUnit.getSource().charAt(end - 1))) {
                    end -= 1;
                }
                return end;
            }

            private ClassNode componentType(final ASTNode node) {
                ClassNode type = (node instanceof ClassNode ? (ClassNode) node : ((Expression) node).getType());
                return type.getComponentType() != null ? componentType(type.getComponentType()) : type;
            }

            private String propertyName(final ASTNode node) {
                final TypeLookupResult[] result = new TypeLookupResult[1];
                new TypeInferencingVisitorFactory().createVisitor(compilationUnit).visitCompilationUnit((ASTNode n, TypeLookupResult r, IJavaElement e) -> {
                    if (n == node) {
                        result[0] = r;
                        return ITypeRequestor.VisitStatus.STOP_VISIT;
                    }
                    return ITypeRequestor.VisitStatus.CONTINUE;
                });

                if (result[0] != null && result[0].declaration instanceof MethodNode) {
                    return ((MethodNode) result[0].declaration).getName();
                }

                return node.getText();
            }
        };
    }

    /**
     * Applies a text edit to a compilation unit.
     *
     * @param cu the compilation unit to apply the edit to
     * @param edit the edit to apply
     * @param save if set, save the CU after the edit has been applied
     */
    // copied from JavaModelUtil (moved to JavaElementUtil circa Eclipse 4.7 and back to JavaModelUtil in Eclipse 4.8m6)
    protected static void applyEdit(final ICompilationUnit cu, final TextEdit edit, final boolean save, final IProgressMonitor monitor) throws CoreException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, CorextMessages.JavaModelUtil_applyedit_operation, 2);
        IFile file = (IFile) cu.getResource();
        if (!save || !file.exists()) {
            cu.applyTextEdit(edit, subMonitor.split(2));
        } else {
            IStatus status = Resources.makeCommittable(file, null);
            if (!status.isOK()) {
                throw new CoreException(status);
            }
            cu.applyTextEdit(edit, subMonitor.split(1));
            cu.save(subMonitor.split(1), true);
        }
    }
}
