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
package org.codehaus.groovy.eclipse.codeassist.processors;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.eclipse.codeassist.CharArraySourceBuffer;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder.NameAndLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class TypeCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    protected ModuleNode module;
    protected JDTResolver resolver;

    public TypeCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    @Override
    public void setResolverInformation(ModuleNode module, JDTResolver resolver) {
        this.module = module;
        this.resolver = resolver;
    }

    @Override
    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        ContentAssistContext context = getContext();

        String expression = context.getQualifiedCompletionExpression();
        if (!doTypeCompletion(context, expression)) {
            return Collections.emptyList();
        }

        int replacementStart = getReplacementStartOffset();
        SearchableEnvironment environment = getNameEnvironment();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), replacementStart, -1, environment.nameLookup, monitor);

        int lastDotIndex = expression.lastIndexOf('.');
        // check for free variable or fully-qualified (by packages) expression
        if (lastDotIndex < 0 || environment.nameLookup.isPackage(expression.substring(0, lastDotIndex).split("\\."))) {
            boolean findMembers = true; // not sure about findMembers; javadoc says method does not find member types
            environment.findTypes(expression.toCharArray(), findMembers, requestor.options.camelCaseMatch, getSearchFor(), requestor, monitor);
        } else if (Character.isJavaIdentifierStart(expression.charAt(0)) && expression.chars().allMatch(c -> c == '.' || Character.isJavaIdentifierPart(c))) {
            // qualified expression; requires manual inner types checking

            String qualifier = expression.substring(0, lastDotIndex);
            String pattern   = expression.substring(lastDotIndex + 1, expression.length());

            Consumer<IType> checker = (IType outerType) -> {
                if (outerType != null && outerType.exists() && qualifier.endsWith(outerType.getElementName()))
                try {
                    for (IType innerType : outerType.getTypes()) {
                        if (isAcceptable(innerType, getSearchFor()) && ProposalUtils.matches(pattern, innerType.getElementName(), requestor.options.camelCaseMatch, requestor.options.substringMatch)) {
                            requestor.acceptType(innerType.getPackageFragment().getElementName().toCharArray(), innerType.getElementName().toCharArray(),
                                CharOperation.splitOn('$', outerType.getTypeQualifiedName().toCharArray()), innerType.getFlags(), ProposalUtils.getTypeAccessibility(innerType));
                        }
                    }
                } catch (JavaModelException e) {
                    GroovyContentAssist.logError(e);
                }
            };

            ClassNode outerTypeNode = resolver.resolve(qualifier);
            if (!ClassHelper.DYNAMIC_TYPE.equals(outerTypeNode)) {
                checker.accept(environment.nameLookup.findType(outerTypeNode.getName(), false, 0));
            } else if (qualifier.indexOf('.') < 0) {
                // unknown qualifier; search for types with exact matching
                environment.findTypes(qualifier.toCharArray(), true, false, 0, requestor, monitor);
                List<ICompletionProposal> proposals = requestor.processAcceptedTypes(resolver);
                for (ICompletionProposal proposal : proposals) {
                    if (proposal instanceof AbstractJavaCompletionProposal) {
                        checker.accept((IType) ((AbstractJavaCompletionProposal) proposal).getJavaElement());
                    }
                }
            }
        }

        return requestor.processAcceptedTypes(resolver);
    }

    /**
     * Don't show types...
     * <ul>
     * <li>if there is no previous text (except for imports or annotations)
     * <li>if completing on generics wildcard, placeholder, "extends" or "super"
     * <li>if completing a constructor, method, for loop or catch parameter name
     * <li>when in a class body and there is a type declaration immediately before
     * </ul>
     */
    protected boolean doTypeCompletion(ContentAssistContext context, String expression) {
        if (expression != null && expression.isEmpty()) {
            return (context.location == ContentAssistLocation.ANNOTATION || context.location == ContentAssistLocation.IMPORT);
        }
        if (context.location == ContentAssistLocation.GENERICS && context.completionNode instanceof GenericsType) {
            return false;
        }
        // check for parameter name completion
        if (context.location == ContentAssistLocation.PARAMETER && context.completionNode != null) {
            AnnotatedNode completionNode = (AnnotatedNode) context.completionNode;
            if (completionNode.getStart() < completionNode.getNameStart() &&
                    context.completionLocation >= completionNode.getNameStart()) {
                return false;
            }
        }
        return !isBeforeTypeName(context);
    }

    protected int getSearchFor() {
        switch (getContext().location) {
        case EXTENDS:
            return IJavaSearchConstants.CLASS;
        case IMPLEMENTS:
            return IJavaSearchConstants.INTERFACE;
        case EXCEPTIONS:
            return IJavaSearchConstants.CLASS;
        case ANNOTATION:
            return IJavaSearchConstants.ANNOTATION_TYPE;
        default:
            return IJavaSearchConstants.TYPE;
        }
    }

    protected boolean isAcceptable(IType type, int searchFor) throws JavaModelException {
        if (searchFor == IJavaSearchConstants.TYPE) {
            return true;
        }
        int kind = TypeDeclaration.kind(type.getFlags());
        switch (kind) {
        case TypeDeclaration.ENUM_DECL:
        case TypeDeclaration.CLASS_DECL:
        case TypeDeclaration.ANNOTATION_TYPE_DECL:
            return (searchFor == IJavaSearchConstants.CLASS);
        case TypeDeclaration.INTERFACE_DECL:
            return (searchFor == IJavaSearchConstants.INTERFACE);
        }
        return false;
    }

    protected static boolean isBeforeTypeName(ContentAssistContext context) {
        if (context.location != ContentAssistLocation.CLASS_BODY) {
            return false;
        }
        NameAndLocation nameAndLocation = new ExpressionFinder().findPreviousTypeNameToken(
            new CharArraySourceBuffer(context.unit.getContents()), context.completionLocation);
        if (nameAndLocation == null) {
            return false;
        }
        if (!(context.completionNode instanceof FieldNode)) {
            return false;
        }
        return !FIELD_MODIFIERS.contains(nameAndLocation.name.trim());
    }

    protected static final Set<String> FIELD_MODIFIERS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList("private", "protected", "public", "static", "final")));
}
