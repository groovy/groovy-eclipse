/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals

import groovy.transform.CompileStatic

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer
import org.codehaus.groovy.eclipse.core.model.GroovyRuntime
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

@CompileStatic
class AddGroovyLibrariesProposal implements IJavaCompletionProposal {

    final int relevance = 100
    IJavaProject project

    static boolean appliesTo(IProblemLocation location) {
        location.problemId == IProblem.IsClassPathCorrect && location.problemArguments[0] ==~ /groovy.lang.(GroovyObject|MetaClass)/
    }

    @Override
    void apply(IDocument document) {
        GroovyRuntime.addGroovyClasspathContainer(project)
    }

    @Override
    String getAdditionalProposalInfo() {
    }

    @Override
    IContextInformation getContextInformation() {
    }

    @Override
    String getDisplayString() {
        "Add ${GroovyClasspathContainer.NAME} to ${project.moduleDescription ? 'modulepath' : 'classpath'}"
    }

    @Override
    Image getImage() {
        JavaPluginImages.get(JavaPluginImages.IMG_OBJS_LIBRARY)
    }

    @Override
    Point getSelection(IDocument document) {
    }
}
