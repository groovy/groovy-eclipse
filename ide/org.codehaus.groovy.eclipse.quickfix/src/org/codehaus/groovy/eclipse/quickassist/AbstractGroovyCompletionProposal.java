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

import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public abstract class AbstractGroovyCompletionProposal implements IJavaCompletionProposal {

    protected final IInvocationContext context;

    public AbstractGroovyCompletionProposal(IInvocationContext context) {
        this.context = context;
    }

    public String getAdditionalProposalInfo() {
        return null;
    }

    public IContextInformation getContextInformation() {
        return null; //new ContextInformation(getImage(), getDisplayString(), getDisplayString());
    }

    public Image getImage() {
        String imageLocation = getImageBundleLocation();
        if (imageLocation != null) {
            return JavaPluginImages.get(imageLocation);
        }
        return null;
    }

    public int getRelevance() {
        return 10;
    }

    public Point getSelection(IDocument document) {
        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * @return (@code true} iff this completion proposal is valid in the current context
     */
    abstract public boolean hasProposals();

    abstract protected String getImageBundleLocation();

    protected final GroovyCompilationUnit getGroovyCompilationUnit() {
        return (GroovyCompilationUnit) context.getCompilationUnit();
    }

    protected final IProject getProject() {
        IResource resource = context.getCompilationUnit().getResource();
        return resource != null ? resource.getProject() : null;
    }
}
