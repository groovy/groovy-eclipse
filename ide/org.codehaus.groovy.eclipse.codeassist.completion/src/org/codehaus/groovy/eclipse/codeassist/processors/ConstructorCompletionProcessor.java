/*
 * Copyright 2009-2018 the original author or authors.
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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.groovy.search.ITypeResolver;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.AbstractJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ConstructorCompletionProcessor extends AbstractGroovyCompletionProcessor implements ITypeResolver {

    protected ModuleNode module;
    protected JDTResolver resolver;

    public ConstructorCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
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
        char[] completionChars; int completionStart;
        switch (context.location) {
        case CONSTRUCTOR:
            context.extend(getJavaContext().getCoreContext(), null);
            completionStart = context.completionNode.getStart(); // skip "new "
            completionChars = context.getQualifiedCompletionExpression().toCharArray();
            break;
        case METHOD_CONTEXT:
            completionStart = ((Expression) context.completionNode).getNameStart();
            completionChars = ((Expression) context.completionNode).getType().getName().replace('$', '.').toCharArray();
            break;
        default:
            throw new IllegalStateException("Invalid constructor completion location: " + context.location.name());
        }

        SearchableEnvironment environment = getNameEnvironment();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), completionStart, -1, environment.nameLookup, monitor);

        int lastDotIndex = CharOperation.lastIndexOf('.', completionChars);
        // check for unqualified or fully-qualified (by packages) expression
        if (lastDotIndex < 0 || environment.nameLookup.isPackage(CharOperation.toStrings(CharOperation.splitOn('.', completionChars, 0, lastDotIndex)))) {

            environment.findConstructorDeclarations(completionChars, requestor.options.camelCaseMatch, requestor, monitor);
        } else {
            // qualified expression; requires manual inner types checking

            String qualifier = String.valueOf(completionChars, 0, lastDotIndex);
            String pattern   = String.valueOf(completionChars, lastDotIndex + 1, completionChars.length - lastDotIndex - 1);

            Consumer<IType> checker = (IType outerType) -> {
                if (outerType != null && outerType.exists() && qualifier.endsWith(outerType.getElementName()))
                try {
                    for (IType innerType : outerType.getTypes()) {
                        if (matches(pattern, innerType.getElementName(), requestor.options.camelCaseMatch)) {
                            int extraFlags = 0; //ConstructorPattern.decodeExtraFlags(innerType.getFlags())

                            boolean hasConstructor = false;
                            for (IMethod m : innerType.getMethods()) { hasConstructor = hasConstructor || m.isConstructor();
                                if (!m.isConstructor() || Flags.isStatic(m.getFlags()) || Flags.isSynthetic(m.getFlags())) {
                                    continue;
                                }
                                char[][] parameterNames = CharOperation.toCharArrays(Arrays.asList(m.getParameterNames()));
                                char[][] parameterTypes = null; //CharOperation.toCharArrays(Arrays.asList(m.getParameterTypes()));

                                requestor.acceptConstructor(m.getFlags(), innerType.getTypeQualifiedName().toCharArray(),
                                    m.getNumberOfParameters(), m.getSignature().toCharArray(), parameterTypes, parameterNames, innerType.getFlags(),
                                    innerType.getPackageFragment().getElementName().toCharArray(), extraFlags, innerType.getPath().toString(), ProposalUtils.getTypeAccessibility(innerType));
                            }
                            if (!hasConstructor) { // adapted from BinarySearchEngine (circa line 772)
                                requestor.acceptConstructor(Flags.AccPublic, innerType.getTypeQualifiedName().toCharArray(),
                                    -1, null, CharOperation.NO_CHAR_CHAR, CharOperation.NO_CHAR_CHAR, innerType.getFlags(),
                                    innerType.getPackageFragment().getElementName().toCharArray(), extraFlags, innerType.getPath().toString(), ProposalUtils.getTypeAccessibility(innerType));
                            }
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

        return requestor.processAcceptedConstructors(findUsedParameters(context), resolver);
    }

    protected static Set<String> findUsedParameters(ContentAssistContext context) {
        if (context.location != ContentAssistLocation.METHOD_CONTEXT) {
            return Collections.emptySet();
        }
        Set<String> usedParams = new HashSet<>();
        ASTNode completionNode = context.completionNode;
        if (completionNode instanceof ConstructorCallExpression) {
            // next find out if there are any existing named args
            ConstructorCallExpression call = (ConstructorCallExpression) completionNode;
            Expression arguments = call.getArguments();
            if (arguments instanceof TupleExpression) {
                for (Expression maybeArg : ((TupleExpression) arguments).getExpressions()) {
                    if (maybeArg instanceof MapExpression) {
                        arguments = maybeArg;
                        break;
                    }
                }
            }
            // now remove the arguments that are already written
            if (arguments instanceof MapExpression) {
                // do extra filtering to determine what parameters are still available
                for (MapEntryExpression entry : ((MapExpression) arguments).getMapEntryExpressions()) {
                    usedParams.add(entry.getKeyExpression().getText());
                }
            }
        }
        return usedParams;
    }
}
