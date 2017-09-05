/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.codehaus.groovy.eclipse.refactoring.formatter.SemicolonRemover;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;

/**
 * Removes the unnecessary semicolons from the selection or compilation unit.
 */
public class RemoveSpuriousSemicolonsProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Remove unnecessary semicolons";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_REMOVE);
    }

    private Boolean semis;

    @Override
    public int getRelevance() {
        if (semis == null) {
            char[] contents = context.getCompilationUnit().getContents();
            if (context.getSelectionLength() > 0) {
                int start = context.getSelectionOffset(),
                    until = start + context.getSelectionLength();
                contents = CharOperation.subarray(contents, start, until);
            }
            if (contents == null) {
                contents = CharOperation.NO_CHAR;
            }
            semis = Boolean.valueOf(CharOperation.contains(';', contents));
        }

        return semis ? 1 : 0;
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) {
        ITextSelection selection = new TextSelection(context.getSelectionOffset(), context.getSelectionLength());
        SemicolonRemover remover = new SemicolonRemover(selection, context.newTempDocument());
        return toTextChange(remover.format());
    }
}
