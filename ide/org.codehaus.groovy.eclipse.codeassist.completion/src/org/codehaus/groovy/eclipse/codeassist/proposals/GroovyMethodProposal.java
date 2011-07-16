 /*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistLocation;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.ui.text.java.LazyJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyMethodProposal extends AbstractGroovyProposal {

    protected final MethodNode method;
    private String contributor;

    private boolean useNamedArguments; // allow individual method proposal contributors to override
                                       // the setting in the preferences

    private ProposalFormattingOptions options;

    public GroovyMethodProposal(MethodNode method) {
        super();
        this.method = method;
        contributor = "Groovy";
        useNamedArguments = false;
    }
    public GroovyMethodProposal(MethodNode method, String contributor) {
        this(method);
        this.contributor = contributor;
    }

    public GroovyMethodProposal(MethodNode method, String contributor, ProposalFormattingOptions options) {
        this(method, contributor);
        this.options = options;
    }

    public void setUseNamedArguments(boolean useNamedArguments) {
        this.useNamedArguments = useNamedArguments;
    }

    @Override
    public AnnotatedNode getAssociatedNode() {
        return method;
    }

    public IJavaCompletionProposal createJavaProposal(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {

        // hmmm...not sure why we need -1 here. This is the only way that I can
        // get
        // context information hovers to show, and it doesn't seem to affect
        // anything else.
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(CompletionProposal.METHOD_REF,
                context.completionLocation - 1);

        if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
            // only show context information and only for methods
            // that exactly match the name. This happens when we are at the
            // start
            // of an argument or an open paren
            if (!context.completionExpression.equals(method.getName())) {
                return null;
            }
            proposal.setReplaceRange(context.completionLocation, context.completionLocation);
            proposal.setCompletion(CharOperation.NO_CHAR);
        } else {
            // otherwise this is a normal method proposal
            proposal.setCompletion(completionName(!isParens(context, javaContext)));
            proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionEnd);
        }
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(method.getDeclaringClass()));
        proposal.setName(method.getName().toCharArray());
        proposal.setParameterNames(createParameterNames(context.unit));
        proposal.setParameterTypeNames(createParameterTypeNames(method));
        proposal.setFlags(getModifiers());
        proposal.setAdditionalFlags(CompletionFlags.Default);
        char[] methodSignature = createMethodSignature();
        proposal.setKey(methodSignature);
        proposal.setSignature(methodSignature);
        proposal.setRelevance(computeRelevance());

        LazyJavaCompletionProposal lazyProposal = null;
        ProposalFormattingOptions groovyProposalOptions = getGroovyProposalOptions();
        if (groovyProposalOptions.doParameterGuessing) {
            lazyProposal = GroovyJavaGuessingCompletionProposal.createProposal(proposal, javaContext, true, contributor,
                    groovyProposalOptions);
        }
        if (lazyProposal == null) {
            lazyProposal = new GroovyJavaMethodCompletionProposal(proposal, javaContext, groovyProposalOptions, contributor);
            if (context.location == ContentAssistLocation.METHOD_CONTEXT) {
                ((GroovyJavaMethodCompletionProposal) lazyProposal).contextOnly();
            }
        }
        return lazyProposal;

    }

    private ProposalFormattingOptions getGroovyProposalOptions() {
        if (options == null) {
            options = ProposalFormattingOptions.newFromOptions();
        }
        return options.newFromExisting(useNamedArguments, method);
    }

    /**
     * @param context
     * @param javaContext
     * @return
     * @throws BadLocationException
     */
    private boolean isParens(ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {
        if (javaContext.getDocument().getLength() > context.completionEnd) {
            try {
                return javaContext.getDocument().getChar(context.completionEnd) == '(';
            } catch (BadLocationException e) {
                GroovyCore.logException("Exception during content assist", e);
            }
        }
        return false;
    }
    protected boolean shouldUseNamedArguments(IPreferenceStore prefs) {
        return (prefs
                .getBoolean(PreferenceConstants.GROOVY_CONTENT_NAMED_ARGUMENTS) && method instanceof ConstructorNode)
                || useNamedArguments;
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
        boolean hasWhitespace = false;
        for (int i = 0; i < nameArr.length; i++) {
            if (Character.isWhitespace(nameArr[i])) {
                hasWhitespace = true;
                break;
            }
        }
        if (hasWhitespace) {
            name = "\"" + name + "\"";
        }

        // don't include parens if the char after the completionEnd is a paren (don't want to double
        // insert)
        if (includeParens) {
            return (name + "()").toCharArray();
        } else {
            return name.toCharArray();
        }
    }

    protected char[][] createParameterNames(ICompilationUnit unit) {

        Parameter[] params = method.getParameters();
        int numParams = params == null ? 0 : params.length;

        // short circuit
        if (numParams == 0) {
            return new char[0][];
        }

        char[][] paramNames = null;
        // if the MethodNode has param names filled in, then use that
        if (params[0].getName().equals("arg0")
                || params[0].getName().equals("param0")) {
            paramNames = getParameterNames(unit, method);
        }

        if (paramNames == null) {
            paramNames = new char[params.length][];
            for (int i = 0; i < params.length; i++) {
                paramNames[i] = params[i].getName().toCharArray();
            }
        }

        return paramNames;
    }

    protected char[][] createParameterTypeNames(MethodNode method) {
        char[][] typeNames = new char[method.getParameters().length][];
        int i = 0;
        for (Parameter param : method.getParameters()) {
            typeNames[i] = ProposalUtils.createSimpleTypeName(param.getType());
            i++;
        }
        return typeNames;
    }

    /**
     * FIXADE I am concerned that this takes a long time since we are doing a lookup for each method
     * any way to cache?
     * @throws JavaModelException
     */
    protected char[][] getParameterNames(ICompilationUnit unit, MethodNode method) {
        try {
            IType type = unit.getJavaProject().findType(method.getDeclaringClass().getName(), new NullProgressMonitor());
            if (type != null && type.exists()) {
                Parameter[] params = method.getParameters();
                int numParams = params == null ? 0 : params.length;

                if (numParams == 0) {
                    return new char[0][];
                }

                String[] parameterTypeSignatures = new String[numParams];
                boolean doResolved = type.isBinary();
                for (int i = 0; i < parameterTypeSignatures.length; i++) {
                    if (doResolved) {
                        parameterTypeSignatures[i] = ProposalUtils.createTypeSignatureStr(params[i].getType());
                    } else {
                        parameterTypeSignatures[i] = ProposalUtils.createUnresolvedTypeSignatureStr(params[i].getType());
                    }
                }
                IMethod jdtMethod = null;

                // try to find the precise method
                IMethod maybeMethod = type.getMethod(method.getName(),
                        parameterTypeSignatures);
                if (maybeMethod != null && maybeMethod.exists()) {
                    jdtMethod = maybeMethod;
                } else {
                    // try something else and be a little more lenient
                    // look for any methods with the same name and number of
                    // arguments
                    IMethod[] methods = type.getMethods();
                    for (IMethod maybeMethod2 : methods) {
                        if (maybeMethod2.getElementName().equals(
                                method.getName())
                                && maybeMethod2.getNumberOfParameters() == numParams) {
                            jdtMethod = maybeMethod2;
                        }
                    }
                }

                // method was found somehow...return it.
                if (jdtMethod != null) {
                    String[] paramNames = jdtMethod.getParameterNames();
                    char[][] paramNamesChar = new char[paramNames.length][];
                    for (int i = 0; i < paramNames.length; i++) {
                        paramNamesChar[i] = paramNames[i].toCharArray();
                    }
                    return paramNamesChar;
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception while looking for parameter types of " + method.getName(), e);
        }
        return null;
    }

    /**
	 * @return the method
	 */
	public MethodNode getMethod() {
		return method;
	}
}
