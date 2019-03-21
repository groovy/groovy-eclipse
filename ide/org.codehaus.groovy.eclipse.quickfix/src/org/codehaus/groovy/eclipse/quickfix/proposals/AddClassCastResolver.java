/*
 * Copyright 2009-2019 the original author or authors.
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
package org.codehaus.groovy.eclipse.quickfix.proposals;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.eclipse.quickfix.GroovyQuickFixPlugin;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.InsertEdit;

/**
 * Generates quick fix proposals for assignment problems during static type checking.
 * These proposals add class casting to expression to make it assignable to variable.
 */
public class AddClassCastResolver extends AbstractQuickFixResolver {

    private static final Set<String> DEFAULT_IMPORTS = Stream.concat(
        Stream.of(ResolveVisitor.DEFAULT_IMPORTS).map(p -> p + "*"),
        Stream.of("java.math.BigDecimal", "java.math.BigInteger")
    ).collect(Collectors.toSet());

    protected AddClassCastResolver(QuickFixProblemContext problem) {
        super(problem);
    }

    @Override
    public List<IJavaCompletionProposal> getQuickFixProposals() {
        List<IJavaCompletionProposal> proposals = new ArrayList<>();
        proposals.add(new AddClassCastProposal(getQuickFixProblem(), (GroovyCompilationUnit) getQuickFixProblem().getCompilationUnit()));
        return proposals;
    }

    @Override
    protected ProblemType[] getTypes() {
        return new ProblemType[] {ProblemType.STATIC_TYPE_CHECKING_CANNOT_ASSIGN};
    }

    public static class AddClassCastProposal extends AbstractGroovyQuickFixProposal {

        private GroovyCompilationUnit unit;
        private String typeName;

        public AddClassCastProposal(QuickFixProblemContext problem, GroovyCompilationUnit unit) {
            super(problem);
            this.unit = unit;
            typeName = calculateTypeName();
        }

        @Override
        public void apply(IDocument document) {
            QuickFixProblemContext problemContext = getQuickFixProblemContext();
            int offset = problemContext.getOffset();
            InsertEdit insertEdit = new InsertEdit(offset, "(" + typeName + ") ");
            try {
                unit.applyTextEdit(insertEdit, null);
            } catch (JavaModelException e) {
                GroovyQuickFixPlugin.log(e);
            }
        }

        @Override
        public String getDisplayString() {
            return "Add cast to " + typeName;
        }

        @Override
        protected String getImageBundleLocation() {
            return JavaPluginImages.IMG_CORRECTION_CAST;
        }

        private String calculateTypeName() {
            String message = getQuickFixProblemContext().getProblemDescriptor().getMarkerMessages()[0];
            String name = message.substring(message.lastIndexOf(" ") + 1);
            int lastDotPosition = name.lastIndexOf(".");
            if (DEFAULT_IMPORTS.contains(name)) {
                return name.substring(lastDotPosition + 1);
            }
            String starPackageName = lastDotPosition < 0 ? name : name.substring(0, lastDotPosition) + ".*";
            if (DEFAULT_IMPORTS.contains(starPackageName)) {
                return name.substring(lastDotPosition + 1);
            }
            try {
                for (IImportDeclaration i : unit.getImports()) {
                    if (i.getElementName().equals(name) || i.getElementName().equals(starPackageName)) {
                        return name.substring(lastDotPosition + 1);
                    }
                }
            } catch (JavaModelException e) {
                GroovyQuickFixPlugin.log(e);
            }
            return name;
        }
    }
}
