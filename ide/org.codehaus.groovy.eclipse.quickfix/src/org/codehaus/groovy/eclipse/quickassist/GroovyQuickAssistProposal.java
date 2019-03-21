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
package org.codehaus.groovy.eclipse.quickassist;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public abstract class GroovyQuickAssistProposal implements IJavaCompletionProposal {

    protected GroovyQuickAssistContext context;

    protected GroovyQuickAssistProposal withContext(GroovyQuickAssistContext context) {
        Assert.isNotNull(context);
        this.context = context;
        return this;
    }

    //--------------------------------------------------------------------------

    public String getAdditionalProposalInfo() {
        return null;
    }

    public IContextInformation getContextInformation() {
        return null;
    }

    public abstract String getDisplayString();

    public abstract Image getImage();

    public abstract int getRelevance();

    //--------------------------------------------------------------------------

    public abstract void apply(IDocument document);

    public Point getSelection(IDocument document) {
        return null;
    }
}
