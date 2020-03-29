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

import org.codehaus.groovy.eclipse.ui.utils.GroovyResourceUtil
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.compiler.IProblem
import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

@CompileStatic
class MakeJavaGroovyProposal implements IJavaCompletionProposal {

    final String displayString = 'Convert to Groovy file and open in Groovy editor'
    final int relevance = 100
    IResource resource

    static boolean appliesTo(IProblemLocation location) {
        switch (location.problemId) {
        case IProblem.ParsingErrorInsertTokenAfter:
            return true
        case IProblem.ParsingErrorInsertToComplete:
            return location.problemArguments[0] == ';' || location.problemArguments[0] == '}'
        case IProblem.ParsingError:
            return location.problemArguments[0] == 'in' || location.problemArguments[0] == 'trait'
        case IProblem.UndefinedType:
            return location.problemArguments[0] == 'as' || location.problemArguments[0] == 'def' || location.problemArguments[0] == 'trait'
        }
    }

    @Override
    void apply(IDocument document) {
        GroovyResourceUtil.renameFile(GroovyResourceUtil.GROOVY, Collections.singletonList(resource))
    }

    @Override
    String getAdditionalProposalInfo() {
    }

    @Override
    IContextInformation getContextInformation() {
    }

    @Override
    Image getImage() {
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE)
    }

    @Override
    Point getSelection(IDocument document) {
    }
}
