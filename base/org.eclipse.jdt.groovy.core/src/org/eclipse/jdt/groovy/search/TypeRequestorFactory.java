/*
 * Copyright 2009-2023 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.Activator;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.search.matching.FieldPattern;
import org.eclipse.jdt.internal.core.search.matching.LocalVariablePattern;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.OrPattern;
import org.eclipse.jdt.internal.core.search.matching.PackageReferencePattern;
import org.eclipse.jdt.internal.core.search.matching.PossibleMatch;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;
import org.eclipse.jdt.internal.core.util.Util;

public class TypeRequestorFactory {

    public ITypeRequestor createRequestor(PossibleMatch possibleMatch, SearchPattern pattern, SearchRequestor requestor) {
        // TODO: AndPattern, MultiTypeDeclarationPattern, PackageDeclarationPattern, SuperTypeReferencePattern, TypeParameterPattern
        if (pattern instanceof TypeReferencePattern) {
            return new TypeReferenceSearchRequestor((TypeReferencePattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof TypeDeclarationPattern) {
            return new TypeDeclarationSearchRequestor((TypeDeclarationPattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof FieldPattern) {
            return new FieldReferenceSearchRequestor((FieldPattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof MethodPattern) {
            return new MethodReferenceSearchRequestor((MethodPattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof ConstructorPattern) {
            return new ConstructorReferenceSearchRequestor((ConstructorPattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof PackageReferencePattern) {
            return new PackageReferenceSearchRequestor((PackageReferencePattern) pattern, requestor, possibleMatch.document.getParticipant());
        } else if (pattern instanceof LocalVariablePattern) {
            ILocalVariable localVar = ReflectionUtils.getPrivateField(LocalVariablePattern.class, "localVariable", pattern);
            int start;
            try {
                start = localVar.getSourceRange().getOffset();
            } catch (JavaModelException e) {
                Util.log(e);
                start = -1;
            }
            return new LocalVariableReferenceRequestor(localVar.getElementName(),
                localVar.getParent(), requestor, possibleMatch.document.getParticipant(), start);
        } else if (pattern instanceof OrPattern) {
            SearchPattern[] patterns = ReflectionUtils.getPrivateField(OrPattern.class, "patterns", pattern);
            List<ITypeRequestor> requestors = new ArrayList<>(patterns.length);
            for (SearchPattern orPattern : patterns) {
                if (orPattern != null) {
                    ITypeRequestor maybeRequestor = createRequestor(possibleMatch, orPattern, requestor);
                    if (maybeRequestor != null) {
                        requestors.add(maybeRequestor);
                    }
                }
            }
            return new OrPatternRequestor(requestors);
        }
        Util.log(new Status(0, Activator.PLUGIN_ID, "Unhandled search pattern type: " + pattern.getClass().getSimpleName()));
        return null;
    }
}
