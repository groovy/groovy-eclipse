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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.codeassist.ProposalUtils;
import org.codehaus.groovy.eclipse.codeassist.processors.GroovyCompletionProposal;
import org.codehaus.groovy.eclipse.codeassist.proposals.GroovyJavaMethodCompletionProposal.ProposalOptions;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.CompletionFlags;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Andrew Eisenberg
 * @created Nov 12, 2009
 *
 */
public class GroovyMethodProposal extends AbstractGroovyProposal {
    
    
    protected final MethodNode method;
    private final ProposalOptions groovyFormatterPrefs;
    private String contributor;
    
    public GroovyMethodProposal(MethodNode method) {
        super();
        this.method = method;
        IPreferenceStore prefs = GroovyPlugin.getDefault().getPreferenceStore();
        groovyFormatterPrefs = 
            new ProposalOptions(
                    prefs.getBoolean(
                            PreferenceConstants.GROOVY_CONTENT_ASSIST_NOPARENS), 
                    prefs.getBoolean(
                            PreferenceConstants.GROOVY_CONTENT_ASSIST_BRACKETS));
        contributor = "Groovy";
    }
    public GroovyMethodProposal(MethodNode method, String contributor) {
        this(method);
        this.contributor = contributor;
    }


    public IJavaCompletionProposal createJavaProposal(
            ContentAssistContext context,
            JavaContentAssistInvocationContext javaContext) {
        GroovyCompletionProposal proposal = new GroovyCompletionProposal(
                CompletionProposal.METHOD_REF, context.completionLocation);
        
        proposal.setCompletion(completionName());
        proposal.setDeclarationSignature(ProposalUtils.createTypeSignature(method.getDeclaringClass()));
        proposal.setName(method.getName().toCharArray());
        proposal.setParameterNames(createParameterNames(context.unit));
        proposal.setParameterTypeNames(createParameterTypeNames(method));
        proposal.setReplaceRange(context.completionLocation - context.completionExpression.length(), context.completionLocation - context.completionExpression.length());
        proposal.setFlags(getModifiers());
        proposal.setAdditionalFlags(CompletionFlags.Default);
        char[] methodSignature = createMethodSignature();
        proposal.setKey(methodSignature);
        proposal.setSignature(methodSignature);
        proposal.setRelevance(getRelevance(proposal.getName()));

        // maybe some point in the distant future, we can look at 
        // FilledArgumentNamesMethodProposal, but this will be difficult
        return new GroovyJavaMethodCompletionProposal(proposal,
                    javaContext, groovyFormatterPrefs, contributor);

    }
    
    protected char[] createMethodSignature() {
        return ProposalUtils.createMethodSignature(method);
    }

    protected int getModifiers() {
        return method.getModifiers();
    }


    protected char[] completionName() {
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
        return (name + "()").toCharArray();
    }
    
    protected char[][] createParameterNames(ICompilationUnit unit) {
        
        char[][] paramNames = getParameterNames(unit, method);
        if (paramNames != null) {
            return paramNames;
        }
        
        Parameter[] params = method.getParameters();
        paramNames = new char[params.length][];
        for (int i = 0; i < params.length; i++) {
            paramNames[i] = params[i].getName().toCharArray();
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
     * FIXADE cannot find parameters names with type parameters.
     * @throws JavaModelException 
     */
    protected char[][] getParameterNames(ICompilationUnit unit, MethodNode method) {
        try {
            IType type = unit.getJavaProject().findType(method.getDeclaringClass().getName(), new NullProgressMonitor());
            if (type != null && type.exists()) {
                Parameter[] params = method.getParameters();
                String[] parameterTypeSignatures = new String[params == null ? 0 : params.length];
                boolean doResolved = type.isBinary();
                for (int i = 0; i < parameterTypeSignatures.length; i++) {
                    if (doResolved) {
                        parameterTypeSignatures[i] = ProposalUtils.createTypeSignatureStr(params[i].getType());
                    } else {
                        parameterTypeSignatures[i] = ProposalUtils.createUnresolvedTypeSignatureStr(params[i].getType());
                    }
                }
                IMethod jdtMethod = type.getMethod(method.getName(), parameterTypeSignatures);
                if (jdtMethod != null && jdtMethod.exists()) {
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
}
