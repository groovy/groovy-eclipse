/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.eclipse.editor.actions;

import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codebrowsing.fragments.IASTFragment;
import org.codehaus.groovy.eclipse.codebrowsing.requestor.Region;
import org.codehaus.groovy.eclipse.codebrowsing.selection.FindSurroundingNode;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.SourceRange;
import org.eclipse.jdt.internal.ui.javaeditor.selectionactions.SelectionHistory;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;

/**
 * Expands the current TextSelection to include the containing AST node
 *
 * @author Andrew Eisenberg
 * @created May 7, 2010
 */
public class ExpandSelectionAction extends SelectionDispatchAction {
    private GroovyEditor editor;
    private SelectionHistory history;

    public ExpandSelectionAction(GroovyEditor editor, SelectionHistory history) {
        super(editor.getEditorSite());
        this.editor = editor;
        this.history = history;
        setText("Enclosing Element");
    }

    @Override
    public void run(ITextSelection currentSelection) {
        if (currentSelection != null && editor != null) {
            GroovyCompilationUnit unit = editor.getGroovyCompilationUnit();
            if (unit != null) {
                ModuleNode node = unit.getModuleNode();
                if (node != null) {
                    FindSurroundingNode finder = new FindSurroundingNode(new Region(currentSelection.getOffset(), currentSelection.getLength()));
                    IASTFragment result = finder.doVisitSurroundingNode(node);
                    if (result != null) {
                        TextSelection newSelection = new TextSelection(result.getStart(), result.getLength());
                        if (!newSelection.equals(currentSelection)) {
                            history.remember(new SourceRange(currentSelection.getOffset(), currentSelection.getLength()));
                            try {
                                history.ignoreSelectionChanges();
                                editor.selectAndReveal(result.getStart(), result.getLength());
                            } finally {
                                history.listenToSelectionChanges();
                            }

                        }
                        editor.getSelectionProvider().setSelection(newSelection);
                    }
                }
            }
        }
    }
}
