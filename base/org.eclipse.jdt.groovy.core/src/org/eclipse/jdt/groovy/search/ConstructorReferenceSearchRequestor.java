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
package org.eclipse.jdt.groovy.search;

import java.util.Optional;
import java.util.function.Function;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.jdt.groovy.model.GroovyClassFileWorkingCopy;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.groovy.core.util.ReflectionUtils;
import org.eclipse.jdt.internal.core.search.matching.ConstructorPattern;
import org.eclipse.jdt.internal.core.util.Util;

public class ConstructorReferenceSearchRequestor implements ITypeRequestor {

    protected final SearchRequestor requestor;
    protected final SearchParticipant participant;
    protected final String declaringQualifiedName;
    protected final String[] parameterQualifiedNames;
    protected final boolean findDeclarations, findReferences;

    public ConstructorReferenceSearchRequestor(ConstructorPattern pattern, SearchRequestor requestor, SearchParticipant participant) {
        this.requestor = requestor;
        this.participant = participant;

        IType declaringType = ((IMethod) pattern.focus).getDeclaringType();
        char[] declaringQualifier = Optional.ofNullable(pattern.declaringQualification)
            .orElse(declaringType.getPackageFragment().getElementName().toCharArray());
        this.declaringQualifiedName = String.valueOf(CharOperation.concat(declaringQualifier, pattern.declaringSimpleName, '.'));

        this.parameterQualifiedNames = new String[pattern.parameterCount];
        for (int i = 0; i < pattern.parameterCount; i += 1) {
            if (pattern.parameterQualifications[i] != null || MethodReferenceSearchRequestor.isPrimitiveType(pattern.parameterSimpleNames[i])) {
                parameterQualifiedNames[i] = String.valueOf(CharOperation.concat(pattern.parameterQualifications[i], pattern.parameterSimpleNames[i], '.'));
            } else {
                int arrayCount = 0, j = pattern.parameterSimpleNames[i].length - 1;
                while (pattern.parameterSimpleNames[i][j] == ']') {
                    arrayCount += 1;
                    j -= 2;
                }
                try {
                    String[][] resolved = declaringType.resolveType(String.valueOf(pattern.parameterSimpleNames[i], 0, j + 1));
                    parameterQualifiedNames[i] = Signature.toQualifiedName(resolved[0]);
                    if (parameterQualifiedNames[i].charAt(0) == '.') { // default package
                        parameterQualifiedNames[i] = parameterQualifiedNames[i].substring(1);
                    }
                    while (arrayCount-- > 0) {
                        parameterQualifiedNames[i] += "[]";
                    }
                } catch (Exception e) {
                    Util.log(e);
                }
            }
        }

        this.findDeclarations = (Boolean) ReflectionUtils.getPrivateField(ConstructorPattern.class, "findDeclarations", pattern);
        this.findReferences = (Boolean) ReflectionUtils.getPrivateField(ConstructorPattern.class, "findReferences", pattern);
    }

    @Override
    public VisitStatus acceptASTNode(ASTNode node, TypeLookupResult result, IJavaElement enclosingElement) {
        if (node instanceof AnnotatedNode && ((AnnotatedNode) node).getNameEnd() > 0 && result.declaration instanceof ConstructorNode) {

            if (findDeclarations && node instanceof ConstructorNode) {
                ConstructorNode decl = (ConstructorNode) node;
                String typeName = result.declaringType.getName().replace('$', '.');
                if (typeName.equals(declaringQualifiedName) && hasMatchingParameters(decl.getParameters())) {
                    reportSearchMatch(enclosingElement, element -> new MethodDeclarationMatch(element, SearchMatch.A_ACCURATE,
                        decl.getNameStart(), decl.getNameEnd() + 1 - decl.getNameStart(), participant, element.getResource()));
                }
            }

            if (findReferences && node instanceof ConstructorCallExpression) {
                ConstructorCallExpression call = (ConstructorCallExpression) node;
                String typeName = result.declaringType.getName().replace('$', '.');
                Parameter[] parameters = ((ConstructorNode) result.declaration).getParameters();
                if (typeName.equals(declaringQualifiedName) && hasMatchingParameters(parameters)) {
                    reportSearchMatch(enclosingElement, element -> {
                        boolean isConstructor = true, isSynthetic = false, isSuperInvocation = call.isSuperCall(), isWithinComment = false;
                        return new MethodReferenceMatch(
                            element, SearchMatch.A_ACCURATE, call.getNameStart(), call.getNameEnd() + 1 - call.getNameStart(),
                            isConstructor, isSynthetic, isSuperInvocation, isWithinComment, participant, element.getResource());
                    });
                }
            }
        }
        return VisitStatus.CONTINUE;
    }

    protected void reportSearchMatch(IJavaElement element, Function<IJavaElement, SearchMatch> producer) {
        if (element.getOpenable() instanceof GroovyClassFileWorkingCopy) {
            element = ((GroovyClassFileWorkingCopy) element.getOpenable()).convertToBinary(element);
        }
        SearchMatch match = producer.apply(element);
        try {
            requestor.acceptSearchMatch(match);
        } catch (CoreException e) {
            Util.log(e, "Error reporting search match inside of " + element + " in resource " + element.getResource());
        }
    }

    protected boolean hasMatchingParameters(Parameter[] declarationParameters) {
        if (declarationParameters.length == parameterQualifiedNames.length) {
            for (int i = 0; i < parameterQualifiedNames.length; i += 1) {
                ClassNode candidate = declarationParameters[i].getType();
                ClassNode pattern = makeType(parameterQualifiedNames[i]);
                if (!candidate.equals(pattern)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    protected static ClassNode makeType(String typeName) {
        int i = typeName.indexOf('[');
        if (i < 0) {
            return ClassHelper.make(typeName);
        }
        return makeType(typeName.substring(0, i)).makeArray();
    }
}
