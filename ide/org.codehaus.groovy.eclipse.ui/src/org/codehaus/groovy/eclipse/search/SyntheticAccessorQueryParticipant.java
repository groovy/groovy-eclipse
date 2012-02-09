/*
 * Copyright 2003-2011 the original author or authors.
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
package org.codehaus.groovy.eclipse.search;

import org.codehaus.groovy.eclipse.core.search.SyntheticAccessorSearchRequestor;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.search.FieldDeclarationMatch;
import org.eclipse.jdt.core.search.FieldReferenceMatch;
import org.eclipse.jdt.core.search.LocalVariableDeclarationMatch;
import org.eclipse.jdt.core.search.LocalVariableReferenceMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.ui.search.JavaElementMatch;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.search.ElementQuerySpecification;
import org.eclipse.jdt.ui.search.IMatchPresentation;
import org.eclipse.jdt.ui.search.IQueryParticipant;
import org.eclipse.jdt.ui.search.ISearchRequestor;
import org.eclipse.jdt.ui.search.QuerySpecification;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.PartInitException;

/**
 * This class handles searching for accessing synthetic getters, setters, and
 * properties (ie- fields that don't exist, but have an associated getter or
 * setter).
 *
 * @author andrew
 * @created Oct 3, 2011
 */
public class SyntheticAccessorQueryParticipant implements IQueryParticipant {

    class UISearchRequestor implements org.codehaus.groovy.eclipse.core.search.ISearchRequestor {
        private final ISearchRequestor requestor;
        private final boolean targetIsGroovy;

        public UISearchRequestor(ISearchRequestor requestor, boolean targetIsGroovy) {
            this.requestor = requestor;
            this.targetIsGroovy = targetIsGroovy;
        }

        /**
         * Rules for when to match and when not to match.  See GRECLIPSE-1369
         * 
         * Target is:
         *      Groovy property: match field and getter references in Java and Groovy.  Field reference will be error in Java</br>
         *      Groovy getter: match field and getter references in Groovy.  Getter references only in Java</br>
         *      Java field: match field and getter references in Groovy.  Field references only in Java</br>
         *      Java method: match field and getter references in Groovy.  Getter references only in Java</br>
         * 
         * If Java target, then ignore extra references in Java
         * If Groovy target, then ignore
         */
        public void acceptMatch(SearchMatch match) {
            IJavaElement enclosingElement = (IJavaElement) match.getElement();
            if (enclosingElement != null) {
                if (!(enclosingElement.getOpenable() instanceof GroovyCompilationUnit) && !targetIsGroovy) {
                    return;
                }
                boolean isWriteAccess = false;
                boolean isReadAccess = false;
                if (match instanceof FieldReferenceMatch) {
                    FieldReferenceMatch fieldRef = ((FieldReferenceMatch) match);
                    isWriteAccess = fieldRef.isWriteAccess();
                    isReadAccess = fieldRef.isReadAccess();
                } else if (match instanceof FieldDeclarationMatch) {
                    isWriteAccess = true;
                } else if (match instanceof LocalVariableReferenceMatch) {
                    LocalVariableReferenceMatch localVarRef = ((LocalVariableReferenceMatch) match);
                    isWriteAccess = localVarRef.isWriteAccess();
                    isReadAccess = localVarRef.isReadAccess();
                } else if (match instanceof LocalVariableDeclarationMatch) {
                    isWriteAccess = true;
                }
                boolean isSuperInvocation = false;
                if (match instanceof MethodReferenceMatch) {
                    MethodReferenceMatch methodRef = (MethodReferenceMatch) match;
                    isSuperInvocation = methodRef.isSuperInvocation();
                }

                requestor.reportMatch(createJavaElementMatch(enclosingElement, match.getRule(), match.getOffset(),
                        match.getLength(), match.getAccuracy(), isReadAccess, isWriteAccess, match.isInsideDocComment(),
                        isSuperInvocation));
            }
        }

        private Match createJavaElementMatch(IJavaElement enclosingElement, int rule, int offset, int length, int accuracy,
                boolean isReadAccess, boolean isWriteAccess, boolean insideDocComment, boolean isSuperInvocation) {
            return ReflectionUtils.executePrivateConstructor(JavaElementMatch.class, new Class<?>[] { Object.class,
                int.class,
                int.class, int.class, int.class, boolean.class, boolean.class, boolean.class, boolean.class }, new Object[] {
                enclosingElement, rule, offset, length, accuracy, isReadAccess, isWriteAccess, insideDocComment,
                isSuperInvocation });
        }
    }

    SyntheticAccessorSearchRequestor accessorRequestor;

    public void search(ISearchRequestor requestor, QuerySpecification querySpecification, IProgressMonitor monitor)
            throws CoreException {
        if (querySpecification instanceof ElementQuerySpecification) {
            accessorRequestor = new SyntheticAccessorSearchRequestor();
            IJavaElement element = ((ElementQuerySpecification) querySpecification).getElement();
            accessorRequestor.findSyntheticMatches(element,
                    querySpecification.getLimitTo(), getSearchParticipants(), querySpecification.getScope(), new UISearchRequestor(
                            requestor, element.getOpenable() instanceof GroovyCompilationUnit), monitor);
        }
    }

    private SearchParticipant[] getSearchParticipants() {
        return new SearchParticipant[] { new JavaSearchParticipant() };
    }

    public int estimateTicks(QuerySpecification specification) {
        if (!(specification instanceof ElementQuerySpecification)) {
            return 0;
        }
        return 3;
    }

    public IMatchPresentation getUIParticipant() {
        return new IMatchPresentation() {

            public ILabelProvider createLabelProvider() {
                return new JavaElementLabelProvider();
            }

            public void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException {
                // nop
            }

        };
    }

}
