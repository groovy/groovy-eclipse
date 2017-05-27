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

import org.codehaus.groovy.eclipse.refactoring.core.extract.ExtractGroovyLocalRefactoring;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;

/**
 * Extracts an expression to a local variable. Delegates to {@link ExtractGroovyLocalRefactoring}.
 */
public class ExtractToLocalProposal extends TextRefactoringProposal {

    public ExtractToLocalProposal(IInvocationContext context) {
        super(context, new ExtractGroovyLocalRefactoring((GroovyCompilationUnit) context.getCompilationUnit(), context.getSelectionOffset(), context.getSelectionLength()));
        getDelegate().setLocalName(getDelegate().guessLocalNames()[0]);
    }

    public ExtractToLocalProposal(IInvocationContext context, boolean all) {
        this(context);
        getDelegate().setReplaceAllOccurrences(all);
    }

    protected ExtractGroovyLocalRefactoring getDelegate() {
        return (ExtractGroovyLocalRefactoring) delegate;
    }

    protected String getImageBundleLocation() {
        return JavaPluginImages.IMG_CORRECTION_LOCAL;
    }

    public int getRelevance() {
        return super.getRelevance() - (getDelegate().isReplaceAllOccurrences() ? 0 : 1);
    }
}
