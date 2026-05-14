/*
 * Copyright 2009-2024 the original author or authors.
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
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.codehaus.jdt.groovy.internal.compiler.ast.JDTResolver;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;
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
        char[] completionChars;
        switch (context.location) {
        case CONSTRUCTOR:
            context.extend(getJavaContext().getCoreContext(), null);
            completionChars = context.getQualifiedCompletionExpression().toCharArray();
            break;
        case STATEMENT:
            context = findCtorCallContext(context);
            if (context == null) {
                return Collections.emptyList();
            } else if (context == getContext()) {
                String typeName;
                if ("this".startsWith(context.completionExpression)) {
                    typeName = context.getEnclosingGroovyType().getName();
                } else { assert "super".startsWith(context.completionExpression);
                    typeName = context.getEnclosingGroovyType().getSuperClass().getName();
                }
                completionChars = typeName.replace('$', '.').toCharArray();
                break;
            }
            // fall through
        case METHOD_CONTEXT:
            completionChars = context.getPerceivedCompletionNode().getText().replace('$', '.').toCharArray();
            break;
        default:
            throw new IllegalStateException("Invalid constructor completion location: " + context.location.name());
        }

        int replacementStart = getReplacementStartOffset();
        SearchableEnvironment environment = getNameEnvironment();
        GroovyProposalTypeSearchRequestor requestor = new GroovyProposalTypeSearchRequestor(
            context, getJavaContext(), replacementStart, -1, environment.nameLookup, monitor);

        int lastDotIndex = CharOperation.lastIndexOf('.', completionChars);
        // check for unqualified or fully-qualified (by packages) expression
        if (lastDotIndex < 0 || environment.nameLookup.isPackage(CharOperation.toStrings(CharOperation.splitOn('.', completionChars, 0, lastDotIndex)))) {
            int matchRule = SearchPattern.R_PREFIX_MATCH;
            if (requestor.options.camelCaseMatch) matchRule |= SearchPattern.R_CAMELCASE_MATCH;
          //if (requestor.options.substringMatch) matchRule |= SearchPattern.R_SUBSTRING_MATCH;
            if (requestor.options.subwordMatch)   matchRule |= SearchPattern.R_SUBWORD_MATCH;
            environment.findConstructorDeclarations(completionChars, matchRule, false, requestor, monitor);
        } else {
            // qualified expression; requires manual inner types checking

            String qualifier = String.valueOf(completionChars, 0, lastDotIndex);
            String pattern   = String.valueOf(completionChars, lastDotIndex + 1, completionChars.length - lastDotIndex - 1);

            Consumer<IType> checker = (IType outerType) -> {
                if (outerType != null && outerType.exists() && qualifier.endsWith(outerType.getElementName()))
                try {
                    for (IType innerType : outerType.getTypes()) {
                        if (ProposalUtils.matches(pattern, innerType.getElementName(), requestor.options.camelCaseMatch, requestor.options.subwordMatch)) {
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
            } else if (qualifier.indexOf('.') < 0) { // unknown qualifier; search for types with exact matching
                environment.findTypes(qualifier.toCharArray(), true, SearchPattern.R_PREFIX_MATCH, 0, requestor, monitor);
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

    protected static ContentAssistContext findCtorCallContext(ContentAssistContext context) {
        // check for constructor declaration with prefix of "this" or "super" as first statement/expression
        if (context.containingDeclaration instanceof ConstructorNode && context.completionNode instanceof VariableExpression &&
                !context.completionExpression.isEmpty() && ("this".startsWith(context.completionExpression) ||
                                                            "super".startsWith(context.completionExpression))) {
            Statement code = ((ConstructorNode) context.containingDeclaration).getCode();
            if (code instanceof BlockStatement) {
                for (Statement stmt : ((BlockStatement) code).getStatements()) {
                    if (stmt.getStart() > 0) {
                        if (stmt instanceof ExpressionStatement && ((ExpressionStatement) stmt).getExpression() == context.completionNode) {
                            return context;
                        }
                        break;
                    }
                }
            }
        }

        if (context.completionNode instanceof VariableExpression || context.completionNode instanceof ConstantExpression) {
            ASTNode containingCode = context.containingCodeBlock;
            if (containingCode instanceof MethodNode) {
                containingCode = ((MethodNode) containingCode).getCode();
            } else if (containingCode instanceof Variable) {
                containingCode = ((Variable) containingCode).getInitialExpression();
            } else if (containingCode instanceof AnnotationNode) {
                containingCode = null; // https://github.com/groovy/groovy-eclipse/issues/761
            }

            ConstructorCallExpression[] enclosingCall = new ConstructorCallExpression[1];

            if (containingCode != null) containingCode.visit(new CodeVisitorSupport() {
                @Override
                public void visitConstructorCallExpression(ConstructorCallExpression call) {
                    if (context.completionLocation > call.getNameStart() && context.completionLocation < call.getEnd()) {
                        Expression args = call.getArguments();
                        if (args instanceof TupleExpression) {
                            for (Expression expr : ((TupleExpression) args).getExpressions()) {
                                if (expr == context.completionNode) {
                                    enclosingCall[0] = call;
                                    return;
                                }
                                if (expr instanceof NamedArgumentListExpression) {
                                    args = expr;
                                }
                            }
                        }
                        if (args instanceof NamedArgumentListExpression) {
                            for (MapEntryExpression entry : ((NamedArgumentListExpression) args).getMapEntryExpressions()) {
                                if (entry.getKeyExpression() == context.completionNode) {
                                    enclosingCall[0] = call;
                                    return;
                                }
                            }
                        }
                    }
                    super.visitConstructorCallExpression(call);
                }
            });

            if (enclosingCall[0] != null) {
                ConstructorCallExpression call = enclosingCall[0];

                ClassNode type = call.getType();
                if (call.isUsingAnonymousInnerClass()) {
                    type = call.getType().getUnresolvedSuperClass(false);
                    if (type == ClassHelper.OBJECT_TYPE)
                        type = call.getType().getUnresolvedInterfaces(false)[0];
                }

                return new MethodInfoContentAssistContext(
                    context.completionLocation,
                    context.completionExpression,
                    context.fullCompletionExpression,
                    call, // completionNode
                    context.containingCodeBlock,
                    context.lhsNode,
                    context.unit,
                    context.containingDeclaration,
                    context.completionEnd,
                    type, // methodExpression
                    type.getNameWithoutPackage(),
                    call.getNameEnd() + 1);
            }
        }
        return null;
    }

    protected static Set<String> findUsedParameters(ContentAssistContext context) {
        Set<String> usedParams = Collections.emptySet();

        if (context.location == ContentAssistLocation.METHOD_CONTEXT && context.completionNode instanceof ConstructorCallExpression) {
            ConstructorCallExpression call = (ConstructorCallExpression) context.completionNode;
            Expression args = call.getArguments();
            if (args instanceof TupleExpression) {
                for (Expression expr : ((TupleExpression) args).getExpressions()) {
                    if (expr instanceof NamedArgumentListExpression) {
                        args = expr;
                        break;
                    }
                }
            }
            if (args instanceof NamedArgumentListExpression) {
                usedParams = new HashSet<>();
                // do extra filtering to determine what parameters are still available
                for (MapEntryExpression entry : ((NamedArgumentListExpression) args).getMapEntryExpressions()) {
                    usedParams.add(entry.getKeyExpression().getText());
                }
            }
        }

        return usedParams;
    }
}
