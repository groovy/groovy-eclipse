/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;

/**
 * Quick fix to add the groovy classpath container
 * @author Andrew Eisenberg
 * @created Oct 14, 2011
 */
public class AddGroovyRuntimeResolver extends AbstractQuickFixResolver {
    public static class AddGroovyRuntimeProposal extends
        AbstractGroovyQuickFixProposal {

        private final IJavaProject project;

        public AddGroovyRuntimeProposal(IJavaProject project, QuickFixProblemContext problem,
                int relevance) {
            super(problem, relevance);
            this.project = project;
        }

        public void apply(IDocument document) {
            GroovyRuntime.addGroovyClasspathContainer(project);
        }

        public String getDisplayString() {
            return "Add Groovy Runtime to classpath";
        }

        @Override
        protected String getImageBundleLocation() {
            return JavaPluginImages.IMG_OBJS_LIBRARY;
        }
    }
    protected AddGroovyRuntimeResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    public List<IJavaCompletionProposal> getQuickFixProposals() {
        IJavaProject project = getQuickFixProblem().getCompilationUnit().getJavaProject();
        List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>(2);
        try {
            if (!GroovyRuntime.hasGroovyClasspathContainer(project)) {
                proposals.add(new AddGroovyRuntimeProposal(project, getQuickFixProblem(), 100));
                return proposals;
            }
        } catch (CoreException e) {
            GroovyCore.logWarning("Problem calculating quickfixes", e);
        }
        
        return null;
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] { ProblemType.MISSING_CLASSPATH_CONTAINER_TYPE };
    }

}
