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

import org.codehaus.groovy.eclipse.codeassist.relevance.IRelevanceRule
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.jdt.core.ICompilationUnit
import org.eclipse.jdt.core.IType
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite
import org.eclipse.jdt.internal.ui.JavaPluginImages
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal
import org.eclipse.jface.text.IDocument
import org.eclipse.jface.text.contentassist.IContextInformation
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point

@CompileStatic
class ImportResolvedTypeProposal implements IJavaCompletionProposal {

    IType type
    ICompilationUnit unit

    @Override
    void apply(IDocument document) {
        try {
            ImportRewrite.create(unit, true).with {
                addImport(type.getFullyQualifiedName('.' as char))
                def edit = rewriteImports(null)
                if (edit != null) {
                    unit.applyTextEdit(edit, null)
                }
            }
        } catch (CoreException e) {
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
        // for inner types, display the fully qualified top-level type as the declaration for the suggested type
        String declaration = type.declaringType == null ? type.packageFragment.elementName
            : type.declaringType.fullyQualifiedName.replace('$', '.')

        "Import '${type.elementName}' ($declaration)"
    }

    @Override
    Image getImage() {
        JavaPluginImages.get(JavaPluginImages.IMG_OBJS_IMPDECL)
    }

    @Override
    int getRelevance() {
        IRelevanceRule.DEFAULT.getRelevance(type, unit.allTypes)
    }

    @Override
    Point getSelection(IDocument document) {
    }
}
