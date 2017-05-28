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
import org.codehaus.groovy.eclipse.refactoring.actions.ConvertToPropertyAction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.TextEdit;

/**
 * Replaces an accessor call with an equivalent property expression.
 * <p>
 * Ex: "foo.setBar(baz)" becomes "foo.bar = baz"
 */
public class ConvertAccessorToPropertyProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Replace method call with property expression";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    private TextEdit edit;

    @Override
    public int getRelevance() {
        if (edit == null) {
            edit = ConvertToPropertyAction.createEdit(
                context.getCompilationUnit(), context.getSelectionOffset(), context.getSelectionLength());
        }
        return (edit != null ? 10 : 0);
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) {
        return toTextChange(edit);
    }
}
