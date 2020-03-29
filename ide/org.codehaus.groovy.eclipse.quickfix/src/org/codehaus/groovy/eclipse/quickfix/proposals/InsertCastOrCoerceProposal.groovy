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

import org.codehaus.groovy.control.ResolveVisitor
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jdt.ui.text.java.IProblemLocation
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.eclipse.text.edits.InsertEdit

class InsertCastOrCoerceProposal implements IJavaCompletionProposal {

    int offset
    String string
    ICompilationUnit unit
    final int relevance = 1
    @Lazy String typeName = calculateTypeName()

    static boolean appliesTo(IProblemLocation location) {
        location.problemId == 0 && location.problemArguments[0] =~ /^Groovy:\[Static type checking\] - Cannot assign|return value of type /
    }

    @Override
    void apply(IDocument document) {
        try {
            unit.applyTextEdit(new InsertEdit(offset, "($typeName) "), null)
        } catch (e) {
            GroovyQuickFixPlugin.log(e)
        }
    }

    @Override
    String getAdditionalProposalInfo() {
    }

    @Override
    IContextInformation getContextInformation() {
    }

    @Override
    String getDisplayString() {
        "Add cast to $typeName"
    }

    @Override
    Image getImage() {
        JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CAST)
    }

    @Override
    Point getSelection(IDocument document) {
    }

    //--------------------------------------------------------------------------

    private String calculateTypeName() {
        // TODO: Handle generics, which contain space between type and type arguments
        String name = string.substring(string.lastIndexOf(' ') + 1)
        int lastDotPosition = name.lastIndexOf('.')
        if (name in DEFAULT_IMPORTS) {
            return name.substring(lastDotPosition + 1)
        }
        String starPackageName = lastDotPosition < 0 ? name : name.substring(0, lastDotPosition) + '.*'
        if (starPackageName in DEFAULT_IMPORTS) {
            return name.substring(lastDotPosition + 1)
        }
        try {
            for (imp in unit.imports) {
                if (imp.elementName == name || imp.elementName == starPackageName) {
                    return name.substring(lastDotPosition + 1)
                }
            }
        } catch (e) {
            GroovyQuickFixPlugin.log(e)
        }
        return name
    }

    private static final Set<String> DEFAULT_IMPORTS = ResolveVisitor.DEFAULT_IMPORTS.collect { it + '*' } + ['java.math.BigDecimal', 'java.math.BigInteger']
}
