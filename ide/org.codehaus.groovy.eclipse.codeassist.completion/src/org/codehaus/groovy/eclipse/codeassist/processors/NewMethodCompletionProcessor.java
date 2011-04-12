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

package org.codehaus.groovy.eclipse.codeassist.processors;

import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createDisplayString;
import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createMethodSignatureStr;
import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createSimpleTypeName;
import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.createTypeSignature;
import static org.codehaus.groovy.eclipse.codeassist.ProposalUtils.getImage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.eclipse.codeassist.relevance.Relevance;
import org.codehaus.groovy.eclipse.codeassist.requestor.ContentAssistContext;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.ui.text.java.OverrideCompletionProposal;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.objectweb.asm.Opcodes;

/**
 * @author Andrew Eisenberg
 * @created Nov 10, 2009
 *
 */
public class NewMethodCompletionProcessor extends AbstractGroovyCompletionProcessor {

    class GroovyOverrideCompletionProposal extends OverrideCompletionProposal {

        public GroovyOverrideCompletionProposal(IJavaProject jproject,
                ICompilationUnit cu, String methodName, String[] paramTypes,
                int start, int length, StyledString displayName,
                String completionProposal) {
            super(jproject, cu, methodName, paramTypes, start, length, displayName,
                    completionProposal);

            String repl = completionProposal +
                    " {\n\t\t// TODO Groovy Auto-generated method stub\n" +
                    "\t\t// Only partially implemented. Perform organize imports\n" +
                    "\t\t// to properly import parameter and return types\n\t}";
            setReplacementString(repl);
        }
    }


    public NewMethodCompletionProcessor(ContentAssistContext context, JavaContentAssistInvocationContext javaContext, SearchableEnvironment nameEnvironment) {
        super(context, javaContext, nameEnvironment);
    }

    public List<ICompletionProposal> generateProposals(IProgressMonitor monitor) {
        List<MethodNode> unimplementedMethods = getAllUnimplementedMethods(getClassNode());
        List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
        ContentAssistContext context = getContext();
        IType enclosingType = context.getEnclosingType();
        if (enclosingType != null) {
            for (MethodNode method : unimplementedMethods) {
                proposals.add(createProposal(method, context, enclosingType));
            }
        }
        // now add proposals from relevant proposal providers
        try {
            List<IProposalProvider> providers = ProposalProviderRegistry.getRegistry().getProvidersFor(context.unit);
            for (IProposalProvider provider : providers) {
                List<MethodNode> newProposals = provider.getNewMethodProposals(context);
                if (newProposals != null) {
                    for (MethodNode methodNode : newProposals) {
                        proposals.add(createProposal(methodNode, context, enclosingType));
                    }
                }
            }
        } catch (CoreException e) {
            GroovyCore.logException("Exception looking for proposal providers in " + context.unit.getElementName(), e);
        }

        return proposals;
    }

    /**
     * @return
     */
    private ClassNode getClassNode() {
        // if the current completion is inside a script, then the containing code block will be a Block object, not a ClassNode
        // Must get class node in a different way.
        return getContext().containingCodeBlock instanceof ClassNode ?
                (ClassNode) getContext().containingCodeBlock :
                    getScript();
    }

    /**
     *
     */
    private ClassNode getScript() {
        ModuleNode module = getContext().unit.getModuleNode();
        for (ClassNode clazz : (Iterable<ClassNode>) module.getClasses()) {
            if (clazz.isScript()) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("Expecting script in current module: " + module.getPackageName());
    }

    private ICompletionProposal createProposal(MethodNode method,
            ContentAssistContext context, IType enclosingType) {
        int relevance = Relevance.VERY_HIGH.getRelavance();

        GroovyCompletionProposal proposal = createProposal(CompletionProposal.METHOD_DECLARATION, context.completionLocation);
        String methodSignature = createMethodSignatureStr(method);
        proposal.setSignature(methodSignature.toCharArray());
        proposal.setDeclarationSignature(createTypeSignature(method.getDeclaringClass()));
        proposal.setName(method.getName().toCharArray());
        proposal.setDeclarationTypeName(method.getDeclaringClass().getName().toCharArray());
        proposal.setTypeName(method.getReturnType().getName().toCharArray());
        proposal.setParameterNames(getParameterNames(method));
        proposal.setParameterTypeNames(getParameterTypeNames(method));
        StringBuffer completion = new StringBuffer();
        createMethod(method, completion);
        proposal.setCompletion(completion.toString().toCharArray());
        proposal.setDeclarationKey(method.getDeclaringClass().getName().toCharArray());
        proposal.setReplaceRange(context.completionLocation
                - context.completionExpression.length(), context.completionEnd);
        proposal.setFlags(method.getModifiers());
        proposal.setRelevance(relevance);

        OverrideCompletionProposal override = new GroovyOverrideCompletionProposal(context.unit.getJavaProject(),
                context.unit, method.getName(), Signature.getParameterTypes(methodSignature), context.completionLocation,
                context.completionExpression.length(), createDisplayString(proposal), String.valueOf(proposal.getCompletion()));
        override.setImage(getImage(proposal));
        override.setRelevance(relevance);
        override.setReplacementOffset(context.completionLocation - context.completionExpression.length());
        override.setReplacementLength(context.completionExpression.length());
        override.setRelevance(proposal.getRelevance());
        return override;
    }


    /**
     * @param method
     * @return
     */
    private char[][] getParameterNames(MethodNode method) {
        Parameter[] parameters = method.getParameters();
        char[][] paramNames = new char[parameters.length][];
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = parameters[i].getName().toCharArray();
        }
        return paramNames;
    }

    /**
     * @param method
     * @return
     */
    private char[][] getParameterTypeNames(MethodNode method) {
        Parameter[] parameters = method.getParameters();
        char[][] paramTypeNames = new char[parameters.length][];
        for (int i = 0; i < paramTypeNames.length; i++) {
            paramTypeNames[i] = createSimpleTypeName(parameters[i].getType());
        }
        return paramTypeNames;
    }

    private List<MethodNode> getAllUnimplementedMethods(ClassNode declaring) {
        List<MethodNode> allMethods = declaring.getAllDeclaredMethods();
        List<MethodNode> thisClassMethods = declaring.getMethods();
        List<MethodNode> unimplementedMethods = new ArrayList<MethodNode>(allMethods.size()-thisClassMethods.size());

        // uggh n^2 loop.  Can be made more efficient by doing declaring.getMethods(allMethodNode.getName())
        for (MethodNode allMethodNode : allMethods) {

            if (allMethodNode.getName().startsWith(getContext().completionExpression)) {
                if (isOverridableMethod(allMethodNode)) {

                    boolean found = false;
                    inner:
                    for (MethodNode thisClassMethod : thisClassMethods) {
                        if (allMethodNode.getParameters().length == thisClassMethod.getParameters().length &&
                            allMethodNode.getName().equals(thisClassMethod.getName())) {
                            // now check param types
                            Parameter[] allMethodParams = allMethodNode.getParameters();
                            Parameter[] thisClassParams = thisClassMethod.getParameters();
                            for (int i = 0; i < thisClassParams.length; i++) {
                                if (! allMethodParams[i].getType().getName().equals(thisClassParams[i].getType().getName())) {
                                    continue inner;
                                }
                            }
                            found = true;
                            break inner;
                        }
                    }
                    if (!found) {
                        unimplementedMethods.add(allMethodNode);
                    }
                    found = false;
                }
            }
        }
        return unimplementedMethods;
    }

    /**
     * @param allMethodNode
     * @return
     */
    private boolean isOverridableMethod(MethodNode methodNode) {
        String name = methodNode.getName();
        return !name.contains("$") && !name.contains("<") &&
            !methodNode.isPrivate() &&
            !methodNode.isStatic() &&
            (methodNode.getModifiers() & Opcodes.ACC_FINAL) == 0 ;
    }


    private void createMethod(MethodNode method, StringBuffer completion) {
        //// Modifiers
        // flush uninteresting modifiers
        int insertedModifiers = method.getModifiers() & ~(Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_PUBLIC);
        ASTNode.printModifiers(insertedModifiers, completion);

        //// Type parameters
        // ignore too difficult and not really needed for Groovy

//        GenericsType[] typeVariableBindings = method.getGenericsTypes();
//        if(typeVariableBindings != null && typeVariableBindings.length != 0) {
//            completion.append('<');
//            for (int i = 0; i < typeVariableBindings.length; i++) {
//                if(i != 0) {
//                    completion.append(',');
//                    completion.append(' ');
//                }
//                createTypeVariable(typeVariableBindings[i], completion);
//            }
//            completion.append('>');
//            completion.append(' ');
//        }

        //// Return type
        createType(method.getReturnType(), completion, false);
        completion.append(' ');

        //// Selector
        completion.append(method.getName());

        completion.append('(');

        ////Parameters
        Parameter[] parameters = method.getParameters();
        int length = parameters.length;
        for (int i = 0; i < length; i++) {
            if(i != 0) {
                completion.append(',');
                completion.append(' ');
            }
            createType(parameters[i].getType(), completion, true);
            completion.append(' ');
            completion.append(parameters[i].getName());
        }

        completion.append(')');

        //// Exceptions
        ClassNode[] exceptions = method.getExceptions();

        if (exceptions != null && exceptions.length > 0){
            completion.append(' ');
            completion.append("throws");
            completion.append(' ');
            for(int i = 0; i < exceptions.length ; i++){
                if(i != 0) {
                    completion.append(' ');
                    completion.append(',');
                }
                createType(exceptions[i], completion, false);
            }
        }
    }

    // ignore.  Too difficult and not really needed for groovy.
//    private void createTypeVariable(GenericsType typeVariable, StringBuffer completion) {
//        completion.append(typeVariable.getName());
//
//        if (typeVariable.getUpperBounds() != null && typeVariable.getUpperBounds().length > 0) {
//            for (int i = 0; i < typeVariable.getUpperBounds().length; i++) {
//                if (i > 0) {
//                }
//                completion.append(' ');
//                completion.append("extends");
//                completion.append(' ');
//                createType(typeVariable.getUpperBounds()[0], completion);
//
//            }
//        }
//        if (typeVariable.get != null && typeVariable.superInterfaces != Binding.NO_SUPERINTERFACES) {
//           if (typeVariable.firstBound != typeVariable.superclass) {
//               completion.append(' ');
//               completion.append("extends");
//               completion.append(' ');
//           }
//           for (int i = 0, length = typeVariable.superInterfaces.length; i < length; i++) {
//               if (i > 0 || typeVariable.firstBound == typeVariable.superclass) {
//                   completion.append(' ');
//                   completion.append(EXTENDS);
//                   completion.append(' ');
//               }
//               createType(typeVariable.superInterfaces[i], scope, completion);
//           }
//        }
//    }

    /**
     * @param classNode
     * @param completion
     * this ignores type variables
     */
    private void createType(ClassNode type, StringBuffer completion, boolean isParameter) {
        int arrayCount = 0;
        while (type.getComponentType() != null) {
            arrayCount++;
            type = type.getComponentType();
        }
        if (type.getName().equals("java.lang.Object") && arrayCount == 0) {
            if (!isParameter) {
                completion.append("def");
            }
        } else {
            completion.append(type.getNameWithoutPackage());
            for (int i = 0; i < arrayCount; i++) {
                completion.append("[]");
            }
        }
    }

}
