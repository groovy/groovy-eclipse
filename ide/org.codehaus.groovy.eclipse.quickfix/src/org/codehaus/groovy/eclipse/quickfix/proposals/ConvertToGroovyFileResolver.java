/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.eclipse.ui.utils.GroovyResourceUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;

/**
 * Converts a Java resource to a Groovy resource if certain problems are
 * encountered, like ';' at the end of a statement.
 */
public class ConvertToGroovyFileResolver extends AbstractQuickFixResolver {

    public ConvertToGroovyFileResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] {ProblemType.MISSING_SEMI_COLON_TYPE, ProblemType.MISSING_SEMI_COLON_TYPE_VARIANT};
    }

    @Override
    public List<IJavaCompletionProposal> getQuickFixProposals() {
        return Collections.singletonList(new ConvertToGroovyQuickFix(getQuickFixProblem()));
    }

    public static class ConvertToGroovyQuickFix extends AbstractGroovyQuickFixProposal {
        public ConvertToGroovyQuickFix(QuickFixProblemContext problem) {
            super(problem);
        }

        @Override
        public String getDisplayString() {
            return "Convert to Groovy file and open in Groovy editor";
        }

        @Override
        protected String getImageBundleLocation() {
            return JavaPluginImages.IMG_CORRECTION_CHANGE;
        }

        @Override
        public void apply(IDocument document) {
            IResource resource = getQuickFixProblemContext().getResource();
            GroovyResourceUtil.renameFile(GroovyResourceUtil.GROOVY, Collections.singletonList(resource));
        }
    }
}
