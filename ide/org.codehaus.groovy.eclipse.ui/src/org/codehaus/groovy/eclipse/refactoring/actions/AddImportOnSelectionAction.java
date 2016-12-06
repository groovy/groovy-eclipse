/*
 * Copyright 2009-2016 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.actions;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.ASTNodeFinder;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.ModuleNodeMapper.ModuleNodeInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.internal.corext.codemanipulation.AddImportsOperation.IChooseImportQuery;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationMessages;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.TypeNameMatchCollector;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.CodeStyleConfiguration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

public class AddImportOnSelectionAction extends AddImportOnSelectionAdapter {

    public AddImportOnSelectionAction(CompilationUnitEditor editor) {
        super(editor);
    }

    protected AddImportOperation newAddImportOperation(final GroovyCompilationUnit compilationUnit,
                            final ITextSelection textSelection, final IChooseImportQuery typeQuery) {
        return new AddImportOperation() {
            private IStatus fStatus = Status.OK_STATUS;

            public IStatus getStatus() {
                return fStatus;
            }

            public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
                if (monitor == null) {
                    monitor = new NullProgressMonitor();
                }
                try {
                    monitor.beginTask(CodeGenerationMessages.AddImportsOperation_description, 4);

                    ModuleNodeInfo info = compilationUnit.getModuleInfo(true);
                    if (info.isEmpty()) {
                        return;
                    }
                    monitor.worked(1);

                    ImportRewrite importRewrite = CodeStyleConfiguration.createImportRewrite(compilationUnit, true);
                    TextEdit edit = evaluateEdits(info.module, importRewrite, new SubProgressMonitor(monitor, 1));
                    if (edit == null) {
                        return;
                    }

                    MultiTextEdit result = new MultiTextEdit();
                    result.addChild(edit);
                    result.addChild(importRewrite.rewriteImports(new SubProgressMonitor(monitor, 1)));
                    JavaModelUtil.applyEdit(compilationUnit, result, true, new SubProgressMonitor(monitor, 1));
                } finally {
                    monitor.done();
                }
            }

            private TextEdit evaluateEdits(ModuleNode moduleNode, ImportRewrite importRewrite, IProgressMonitor monitor) throws JavaModelException {
                ASTNodeFinder nodeFinder = new ASTNodeFinder(new Region(textSelection.getOffset(), textSelection.getLength()));
                ASTNode node = nodeFinder.doVisit(moduleNode);
                if (node != null) {
                    if (node instanceof VariableExpression) {
                        TypeNameMatch choice = findCandidateTypes(((VariableExpression) node).getName(), monitor);
                        if (choice != null) {
                            importRewrite.addImport(choice.getFullyQualifiedName());
                            return new ReplaceEdit(node.getStart(), node.getLength(), choice.getSimpleTypeName());
                        }
                    }
                }
                return null;
            }

            private TypeNameMatch findCandidateTypes(String prefix, IProgressMonitor monitor) throws JavaModelException {
                int searchFor = IJavaSearchConstants.TYPE,
                    matchRule = SearchPattern.R_PREFIX_MATCH;
                List<TypeNameMatch> typesFound = new ArrayList<TypeNameMatch>();
                new SearchEngine().searchAllTypeNames(null, 0, prefix.toCharArray(), matchRule, searchFor,
                    SearchEngine.createJavaSearchScope(new IJavaElement[] {compilationUnit.getJavaProject()}),
                    new TypeNameMatchCollector(typesFound), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);

                return typeQuery.chooseImport(typesFound.toArray(new TypeNameMatch[typesFound.size()]), prefix);
            }
        };
    }
}
