/*
 * Copyright 2009-2021 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickassist.proposals;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.eclipse.quickassist.GroovyQuickAssistProposal2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.groovy.core.util.GroovyUtils;
import org.eclipse.jdt.groovy.search.ITypeRequestor;
import org.eclipse.jdt.groovy.search.TypeLookupResult;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.JavaWordFinder;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.text.edits.ReplaceEdit;

public class ReplaceDefWithStaticTypeProposal extends GroovyQuickAssistProposal2 {

    @Override
    public String getDisplayString() {
        return "Replace dynamic type with inferred type";
    }

    @Override
    public Image getImage() {
        return JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_CHANGE);
    }

    private IRegion sloc;
    private ClassNode type;

    @Override
    public int getRelevance() {
        if (sloc == null) {
            ASTNode node = context.getCoveredNode();
            if (node instanceof ClassNode && ClassHelper.isDynamicTyped((ClassNode) node)) {
                IDocument d = context.newTempDocument();
                sloc = JavaWordFinder.findWord(d, context.getSelectionOffset());
                if (sloc != null && sloc.getLength() == 0)
                    sloc = JavaWordFinder.findWord(d, context.getSelectionOffset() - 1);
                if (sloc != null && sloc.getLength() == 3 && String.valueOf(context.getCompilationUnit().getContents(), sloc.getOffset(), sloc.getLength()).matches("def|var")) {
                    // find variable declaration that contains this occurrence of the 'def' keyword
                    IRegion r = JavaWordFinder.findWord(d, sloc.getOffset() + sloc.getLength() + 1);
                    if (r != null) {
                        context.visitCompilationUnit((ASTNode n, TypeLookupResult tlr, IJavaElement e) -> {
                            if (n instanceof DeclarationExpression && !((DeclarationExpression) n).isMultipleAssignmentDeclaration()) {
                                VariableExpression v = ((DeclarationExpression) n).getVariableExpression();
                                if (v.getStart() == r.getOffset() && v.getLength() == r.getLength()) {
                                    type = tlr.type;

                                    return ITypeRequestor.VisitStatus.STOP_VISIT;
                                }
                            }
                            return ITypeRequestor.VisitStatus.CONTINUE;
                        });
                    }
                }
            }
        }
        return (type != null ? 10 : 0);
    }

    @Override
    protected TextChange getTextChange(IProgressMonitor monitor) throws CoreException, BadLocationException {
        String typeName = Signature.toString(GroovyUtils.getTypeSignature(type, false, false));
        return toTextChange(new ReplaceEdit(sloc.getOffset(), sloc.getLength(), typeName));
    }
}
