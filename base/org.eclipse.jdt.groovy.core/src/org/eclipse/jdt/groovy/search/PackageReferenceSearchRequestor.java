/*
 * Copyright 2009-2022 the original author or authors.
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
package org.eclipse.jdt.groovy.search;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.search.PackageReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.search.matching.PackageReferencePattern;
import org.eclipse.jdt.internal.core.util.Util;

public class PackageReferenceSearchRequestor implements ITypeRequestor {

    private final String packageName;
    private final SearchRequestor requestor;
    private final SearchParticipant participant;

    public PackageReferenceSearchRequestor(PackageReferencePattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;
        this.packageName = String.valueOf((char[]) ReflectionUtils.getPrivateField(PackageReferencePattern.class, "pkgName", pattern)) + '.';
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        try {
            if (node instanceof PackageNode) {
                String name = ((PackageNode) node).getName(); // "java.util."

                if (name.startsWith(packageName)) {
                    notifyRequestor(enclosingElement, node.getStart());
                }
            } else if (node instanceof ImportNode) {
                ImportNode i = ((ImportNode) node);
                String name = i.getClassName(); // "java.util.Map$Entry" or null
                if (i.isStar() && !i.isStatic()) name = i.getPackageName(); // "java.util."

                if (name.startsWith(packageName)) {
                    notifyRequestor(enclosingElement, i.getNameStart());
                }
            } else if (node instanceof ClassNode && enclosingElement.getElementType() != IJavaElement.IMPORT_DECLARATION) {
                String name = ((ClassNode) node).getName(); // "java.util.Map$Entry"

                if (name.startsWith(packageName) && name.length() <= node.getLength()) {
                    // beware of class declarations and inlined constant references
                    if (enclosingElement instanceof ISourceReference) {
                        ISourceRange range = ((ISourceReference) enclosingElement).getSourceRange();
                        if (node.getStart() >= range.getOffset() && node.getLength() < range.getLength()) {
                            notifyRequestor(enclosingElement, node.getStart());
                        }
                    }
                }
            }
        } catch (CoreException e) {
            Util.log(e, "Error accepting " + node.getClass().getSimpleName() + " for " + enclosingElement);
        }
        return VisitStatus.CONTINUE;
    }

    protected void notifyRequestor(IJavaElement element, int offset) throws CoreException {
        if (element.getOpenable() instanceof GroovyClassFileWorkingCopy)
            element = ((GroovyClassFileWorkingCopy) element.getOpenable()).convertToBinary(element);

        SearchMatch match = new PackageReferenceMatch(
            element, SearchMatch.A_ACCURATE, offset, packageName.length() - 1, false, participant, element.getResource());

        requestor.acceptSearchMatch(match);
    }
}
