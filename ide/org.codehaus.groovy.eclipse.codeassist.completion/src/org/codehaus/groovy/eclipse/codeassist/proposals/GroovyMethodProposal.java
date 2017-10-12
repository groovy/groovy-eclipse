/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.codeassist.proposals;

import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createTypeSignature;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaGuessingCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.completions.GroovyJavaMethodCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.completions.NamedArgsMethodNode;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.codeassist.requestor.MethodInfoContentAssistContext;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.text.java.AnnotationAtttributeProposalInfo;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;

public class GroovyMethodProposal extends AbstractGroovyProposal {

    private final MethodNode method;

    private final String contributor;

    private ProposalFormattingOptions options;

    public GroovyMethodProposal(MethodNode method) {
        this(method, null);
    }

    public GroovyMethodProposal(MethodNode method, String contributor) {
        super();
        this.method = method;
        this.contributor = contributor;
    }

    @Override
    public AnnotatedNode getAssociatedNode() {
        return method;
    }

    public MethodNode getMethod() {
        return method;
    }

    public ProposalFormattingOptions getProposalFormattingOptions() {
        if (options == null) {
            options = ProposalFormattingOptions.newFromOptions();
        }
        return options;
    }

    public void setProposalFormattingOptions(ProposalFormattingOptions options) {
        this.options = options;
    }

    public IJavaCompletionProposal createJavaProposal(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        int kind = (context.location == ContentAssistLocation.ANNOTATION_BODY ? CompletionProposal.ANNOTATION_ATTRIBUTE_REF : CompletionProposal.METHOD_REF);
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(kind, context.completionLocation);

        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            // only show context information and only for methods that exactly match the name
            // this happens when we are at the start of an argument or an open paren
            MethodInfoContentAssistContext methodContext = (MethodInfoContentAssistContext) context;
            if (!methodContext.methodName.equals(method.getName())) {
                return null;
            }
            proposal.setReplaceRange(context.completionLocation, context.completionLocation);
            proposal.setCompletion(CharOperation.NO_CHAR);
        } else { // this is a normal method proposal
            boolean parens = (kind == CompletionProposal.ANNOTATION_ATTRIBUTE_REF ? false : !isParens(context, javaContext));
            proposal.setCompletion(completionName(parens));
            proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        }
        proposal.setDeclarationSignature(createTypeSignature(method.getDeclaringClass()));
        proposal.setName(method.getName().toCharArray());
        if (method instanceof NamedArgsMethodNode) {
            fillInExtraParameters((NamedArgsMethodNode) method, proposal);
        } else {
            proposal.setParameterNames(createAllParameterNames(context.unit));
            proposal.setParameterTypeNames(getParameterTypeNames(method.getParameters()));
        }
        proposal.setFlags(getModifiers());
        proposal.setAdditionalFlags(CompletionFlags.Default);
        proposal.setSignature(createMethodSignature());
        proposal.setKey(proposal.getSignature());
        proposal.setRelevance(computeRelevance(context));

        if (getRequiredStaticImport() != null) {
            GroovyCompletionProposal methodImportProposal = new GroovyCompletionProposal(CompletionProposal.METHOD_IMPORT, context.completionLocation);
            methodImportProposal.setAdditionalFlags(CompletionFlags.StaticImport);
            methodImportProposal.setCompletion(("import static " + getRequiredStaticImport() + "\n").toCharArray());
            methodImportProposal.setDeclarationSignature(proposal.getDeclarationSignature());
            methodImportProposal.setName(proposal.getName());

            /*
            methodImportProposal.setDeclarationPackageName(method.declaringClass.qualifiedPackageName());
            methodImportProposal.setDeclarationTypeName(method.declaringClass.qualifiedSourceName());
            methodImportProposal.setFlags(method.modifiers);
            if (original != method) proposal.setOriginalSignature(getSignature(original));
            if(parameterNames != null) methodImportProposal.setParameterNames(parameterNames);
            methodImportProposal.setParameterPackageNames(parameterPackageNames);
            methodImportProposal.setParameterTypeNames(parameterTypeNames);
            methodImportProposal.setPackageName(method.returnType.qualifiedPackageName());
            methodImportProposal.setReplaceRange(importStart - this.offset, importEnd - this.offset);
            methodImportProposal.setRelevance(relevance);
            methodImportProposal.setSignature(getSignature(method));
            methodImportProposal.setTokenRange(importStart - this.offset, importEnd - this.offset);
            methodImportProposal.setTypeName(method.returnType.qualifiedSourceName());
            */

            proposal.setRequiredProposals(new CompletionProposal[] {methodImportProposal});
        }

        LazyJavaCompletionProposal lazyProposal = null;
        if (kind == CompletionProposal.ANNOTATION_ATTRIBUTE_REF) {
            proposal.setSignature(createTypeSignature(getMethod().getReturnType()));
            proposal.setFlags(getModifiers() & 0xFFFDFFFF); // clear the "default method" flag

            lazyProposal = new LazyJavaCompletionProposal(proposal, javaContext);
            lazyProposal.setProposalInfo(new AnnotationAtttributeProposalInfo(javaContext.getProject(), proposal));
        } else {
            if (getProposalFormattingOptions().doParameterGuessing) {
                lazyProposal = GroovyJavaGuessingCompletionProposal.createProposal(proposal, javaContext, true, contributor, getProposalFormattingOptions());
            }
            if (lazyProposal == null) {
                lazyProposal = new GroovyJavaMethodCompletionProposal(proposal, javaContext, getProposalFormattingOptions(), contributor);
                // if location is METHOD_CONTEXT, then the type must be MethodInfoContentAssistContext
                // ...but there are other times when the type is MethodInfoContentAssistContext as well
                if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
                    ((GroovyJavaMethodCompletionProposal) lazyProposal).contextOnly();
                }
            }
            if (context.location == ContentAssistLocation.METHOD_CONTEXT) { // NOTE: I've seen STATEMENT for 'assertNu|' and EXPRESSION for 'Assert.assertNu|'
                // attempt to find the location immediately after the opening paren
                // if this is wrong, no big deal, but the context information will not be properly highlighted
                // assume that there is the method name, and then an opening paren (or a space) and then the arguments
                lazyProposal.setContextInformationPosition(((MethodInfoContentAssistContext) context).methodNameEnd + 1);
            }
        }
        return lazyProposal;
    }

    private void fillInExtraParameters(NamedArgsMethodNode namedArgsMethod, GroovyCompletionProposal proposal) {
        proposal.setParameterNames(getSpecialParameterNames(namedArgsMethod.getParameters()));
        proposal.setRegularParameterNames(getSpecialParameterNames(namedArgsMethod.getRegularParams()));
        proposal.setNamedParameterNames(getSpecialParameterNames(namedArgsMethod.getNamedParams()));
        proposal.setOptionalParameterNames(getSpecialParameterNames(namedArgsMethod.getOptionalParams()));

        proposal.setParameterTypeNames(getParameterTypeNames(namedArgsMethod.getParameters()));
        proposal.setRegularParameterTypeNames(getParameterTypeNames(namedArgsMethod.getRegularParams()));
        proposal.setNamedParameterTypeNames(getParameterTypeNames(namedArgsMethod.getNamedParams()));
        proposal.setOptionalParameterTypeNames(getParameterTypeNames(namedArgsMethod.getOptionalParams()));
    }

    private boolean isParens(ContentAssistContext context, JavaContentAssistInvocationContext javaContext) {
        if (javaContext.getDocument().getLength() > context.completionEnd) {
            try {
                return javaContext.getDocument().getChar(context.completionEnd) == '(';
            } catch (BadLocationException e) {
                GroovyContentAssist.logError("Exception during content assist", e);
            }
        }
        return false;
    }

    protected char[] createMethodSignature() {
        return ProposalUtils.createMethodSignature(method);
    }

    protected int getModifiers() {
        return method.getModifiers();
    }

    protected char[] completionName(boolean includeParens) {
        String name = method.getName();
        char[] nameArr = name.toCharArray();
        if (ProposalUtils.hasWhitespace(nameArr)) {
            name = '"' + name + '"';
        }
        if (includeParens) {
            name += "()";
        }
        return name.toCharArray();
    }

    protected char[][] createAllParameterNames(ICompilationUnit unit) {
        Parameter[] params = method.getParameters();
        final int n = params == null ? 0 : params.length;

        // short circuit
        if (n == 0) {
            return CharOperation.NO_CHAR_CHAR;
        }

        char[][] paramNames = null;
        // if the MethodNode has param names filled in, then use that
        if (params[0].getName().equals("arg0") || params[0].getName().equals("param0")) {
            paramNames = calculateAllParameterNames(unit, method);
        }

        if (paramNames == null) {
            paramNames = new char[n][];
            for (int i = 0; i < n; i += 1) {
                String name = params[i].getName();
                if (name != null) {
                    paramNames[i] = name.toCharArray();
                } else {
                    paramNames[i] = ("arg" + i).toCharArray();
                }
            }
        }

        return paramNames;
    }

    protected char[][] getParameterTypeNames(Parameter[] parameters) {
        char[][] typeNames = new char[parameters.length][];
        int i = 0;
        for (Parameter param : parameters) {
            typeNames[i++] = ProposalUtils.createSimpleTypeName(param.getType());
        }
        return typeNames;
    }

    /**
     * FIXADE I am concerned that this takes a long time since we are doing a lookup for each method any way to cache?
     */
    protected char[][] calculateAllParameterNames(ICompilationUnit unit, MethodNode method) {
        Parameter[] params = method.getParameters();
        final int n = (params == null ? 0 : params.length);
        if (n == 0) {
            return CharOperation.NO_CHAR_CHAR;
        }

        try {
            IType declaringType = findDeclaringType(unit, method);
            if (declaringType != null && declaringType.exists()) {
                String[] parameterTypeSignatures = new String[n];
                for (int i = 0; i < n; i += 1) {
                    if (declaringType.isBinary()) {
                        parameterTypeSignatures[i] = ProposalUtils.createTypeSignatureStr(params[i].getType());
                    } else {
                        parameterTypeSignatures[i] = ProposalUtils.createUnresolvedTypeSignatureStr(params[i].getType());
                    }
                }
                // try to find the precise method
                IMethod jdtMethod = findPreciseMethod(declaringType, parameterTypeSignatures);
                // method was found somehow...use it
                if (jdtMethod != null) {
                    String[] paramNames = jdtMethod.getParameterNames();
                    char[][] paramNamesChar = new char[paramNames.length][];
                    for (int i = 0; i < paramNames.length; i += 1) {
                        paramNamesChar[i] = paramNames[i].toCharArray();
                    }
                    return paramNamesChar;
                }
            }
        } catch (JavaModelException e) {
            GroovyContentAssist.logError("Exception while looking for parameter types of " + method.getName(), e);
        }
        return null;
    }

    /**
     * Finds a method with the same name and parameter types.
     */
    private IMethod findPreciseMethod(IType declaringType, String[] parameterTypeSignatures) throws JavaModelException {
        IMethod closestMatch = declaringType.getMethod(method.getName(), parameterTypeSignatures);
        if (closestMatch != null && closestMatch.exists()) {
            return closestMatch;
        } else {
            // try something else and be a little more lenient
            // look for any methods with the same name and argument types
            // (ignoring generic types)
            IMethod[] methods = declaringType.getMethods();
            closestMatch = null;
            // prefer retrieving the method with the same arg types as
            // specified in the parameter.
            methodsIteration: for (IMethod maybeMethod : methods) {
                if (maybeMethod.getElementName().equals(method.getName())) {
                    String[] maybeMethodParameters = maybeMethod.getParameterTypes();
                    assert maybeMethodParameters != null;
                    if (maybeMethodParameters.length == parameterTypeSignatures.length) {
                        closestMatch = maybeMethod;
                        for (int i = 0, n = maybeMethodParameters.length; i < n; i += 1) {
                            String maybeMethodParameterName = removeGenerics(maybeMethodParameters[i]);
                            if (!parameterTypeSignatures[i].equals(maybeMethodParameterName)) {
                                continue methodsIteration;
                            }
                        }
                        return closestMatch;
                    }
                }
            }
        }
        return closestMatch;
    }

    /**
     * @return parameter signature without generics signature
     */
    private String removeGenerics(String maybeMethodParameterName) {
        int genericStart = maybeMethodParameterName.indexOf("<");
        if (genericStart > 0) {
            maybeMethodParameterName = maybeMethodParameterName.substring(0, genericStart) + maybeMethodParameterName.substring(maybeMethodParameterName.indexOf(">") + 1, maybeMethodParameterName.length());
        }
        return maybeMethodParameterName;
    }

    private char[][] getSpecialParameterNames(Parameter[] params) {
        // as opposed to getAllParameterNames, we can assume that the names are
        // correct as is because these parameters were explicitly set by a script
        char[][] paramNames = new char[params.length][];
        for (int i = 0, n = params.length; i < n; i += 1) {
            paramNames[i] = params[i].getName().toCharArray();
        }
        return paramNames;
    }

    private IType findDeclaringType(ICompilationUnit unit, MethodNode method) throws JavaModelException {
        if (cachedDeclaringType == null) {
            cachedDeclaringType = unit.getJavaProject().findType(method.getDeclaringClass().getName(), new NullProgressMonitor());
        }
        return cachedDeclaringType;
    }
    private IType cachedDeclaringType;
}
