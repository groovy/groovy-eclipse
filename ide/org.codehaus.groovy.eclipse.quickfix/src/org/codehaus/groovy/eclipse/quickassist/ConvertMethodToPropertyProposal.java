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
package org.codehaus.groovy.eclipse.quickassist;

import org.codehaus.groovy.eclipse.refactoring.actions.ConvertToPropertyAction;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

public class ConvertMethodToPropertyProposal extends AbstractGroovyTextCompletionProposal {

    public ConvertMethodToPropertyProposal(IInvocationContext context) {
        super(context);
    }

    public String getDisplayString() {
        return "Replace method call with property expression";
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_CHANGE;
    }

    public boolean hasProposals() {
        return getTextEdit(null) != null;
    }

    @Override
    protected TextEdit getTextEdit(IDocument document) {
        return ConvertToPropertyAction.createEdit(getGroovyCompilationUnit(), context.getSelectionOffset(), context.getSelectionLength());
    }
}
