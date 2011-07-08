/*
 * Copyright 2010-2011 the original author or authors.
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

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.editor.GroovyEditor;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.codehaus.groovy.eclipse.refactoring.formatter.FormatterPreferences;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.actions.SelectionDispatchAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchSite;

/**
 * @author Andrew Eisenberg
 * @created Sep 24, 2010
 */
public class FormatGroovyAction extends SelectionDispatchAction {

    private final FormatKind kind;

    public FormatGroovyAction(IWorkbenchSite site, FormatKind kind) {
        super(site);
        this.kind = kind;

        if (kind == FormatKind.INDENT_ONLY) {
            setText("Indent");
            setToolTipText("Indent Groovy file");
            setDescription("Indent selection in Groovy file");
        } else if (kind == FormatKind.FORMAT) {
            setText("Format");
            setToolTipText("Format Groovy file");
            setDescription("Format selection in Groovy file");
        }
    }

    @Override
    public void run(ITextSelection selection) {
        if (!(getSite() instanceof IEditorSite)) {
            return;
        }

        IWorkbenchPart part = ((IEditorSite) getSite()).getPart();
        if (!(part instanceof GroovyEditor)) {
            return;
        }

        GroovyCompilationUnit unit = (GroovyCompilationUnit) part.getAdapter(GroovyCompilationUnit.class);
        GroovyEditor groovyEditor = (GroovyEditor) part;
        IDocument doc = groovyEditor.getDocumentProvider().getDocument(groovyEditor.getEditorInput());

        if (doc != null && unit != null) {
            boolean isIndentOnly = kind == FormatKind.INDENT_ONLY;
            FormatterPreferences preferences = new FormatterPreferences(unit);
            DefaultGroovyFormatter formatter = new DefaultGroovyFormatter(selection, doc, preferences, isIndentOnly);
            TextEdit edit = formatter.format();

            try {
                unit.applyTextEdit(edit, new NullProgressMonitor());
            } catch (MalformedTreeException e) {
                GroovyCore.logException("Exception when formatting", e);
            } catch (JavaModelException e) {
                GroovyCore.logException("Exception when formatting", e);
            }
        }
    }
}
