/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Edward Povazan   - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.codebrowsing.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NamedArgumentListExpression;
import org.codehaus.groovy.eclipse.codebrowsing.DeclarationCategory;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchInfo;
import org.codehaus.groovy.eclipse.codebrowsing.IDeclarationSearchProcessor;
import org.codehaus.groovy.eclipse.codebrowsing.SourceCodeFinder;
import org.codehaus.groovy.eclipse.core.DocumentSourceBuffer;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.ISourceBuffer;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.context.impl.SourceCodeContextFactory;
import org.codehaus.groovy.eclipse.core.impl.ReverseSourceBuffer;
import org.codehaus.groovy.eclipse.core.inference.internal.SourceContextInfo;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.MemberLookupRegistry;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.SymbolTableRegistry;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.impl.CategoryLookup;
import org.codehaus.groovy.eclipse.core.types.impl.ClassLoaderMemberLookup;
import org.codehaus.groovy.eclipse.core.types.impl.CompositeLookup;
import org.codehaus.groovy.eclipse.core.types.impl.GroovyProjectMemberLookup;
import org.codehaus.groovy.eclipse.core.util.ExpressionFinder;
import org.codehaus.groovy.eclipse.core.util.ParseException;
import org.codehaus.groovy.eclipse.editor.actions.IDocumentFacade;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * @author emp
 * @author andrew
 */
public class MethodCallExpressionProcessor implements
		IDeclarationSearchProcessor {
	public IJavaElement[] getProposalsOLD(IDeclarationSearchInfo info) {
		MethodCallExpression expr = (MethodCallExpression) info.getASTNode();
		ClassNode thisClassNode = info.getClassNode();
		
		// is this method call a closure defined in local scope?
		List<IJavaElement> proposals = createLocalClosureCallProposals(info, expr);
		
		// regular method/static method
		ClassNode receiverClassNode = expr.getObjectExpression().getText().equals("this") ?
		        thisClassNode : expr.getObjectExpression().getType();
		proposals.addAll(createMethodCallProposal(DeclarationCategory.METHOD, info,
				receiverClassNode, expr));
		
		// could be a declaration in a superclass
		String targetClassName = expr.getObjectExpression().getText();
		if (proposals.size() == 0 && targetClassName.equals("this")) {
		    ClassNode superClass = thisClassNode.getSuperClass();
		    while (superClass != null && proposals.size() == 0) {
		        proposals.addAll(createMethodCallProposal(DeclarationCategory.METHOD, info,
		                superClass, expr));
		        superClass = superClass.getSuperClass();
		    }
		}
		
		
		return proposals.toArray(new IJavaElement[proposals.size()]);
	}

    private List<IJavaElement> createMethodCallProposal(String category,
			IDeclarationSearchInfo info, ClassNode receiverClassNode,
			MethodCallExpression expr) {
		
		IDocumentFacade facade = info.getEditorFacade();
		IType receiverType = facade.getProjectFacade().groovyClassToJavaType(receiverClassNode);
		if (receiverType == null) {
		    return Collections.EMPTY_LIST;
		}

		String methodName = expr.getMethodAsString();
		int argsLen = getNumargs(expr);

		
		List<IJavaElement> results = new ArrayList<IJavaElement>();
		
		try {
            for (IMethod method : receiverType.getMethods()) {
                if (methodEquals(methodName, argsLen, method)) {
                    results.add(method);
                }
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception during content assist", e);
        }

		return results;
	}

    private int getNumargs(MethodCallExpression expr) {
        int argsLen = 0;
		Expression args = expr.getArguments();
		if (args instanceof ArgumentListExpression || 
		        args instanceof NamedArgumentListExpression) {
		    List<? extends Expression> argExpr = extractArgsExpressions(args);	
		    argsLen = argExpr.size();
		}
        return argsLen;
    }

    private boolean methodEquals(String methodName, int argsLen, IMethod method) {
        if (!method.getElementName().equals(methodName)) {
            return false;
        }
        int thisMethodsArgsLen = method.getParameterTypes() == null ? 
                0 : method.getParameterTypes().length;
        if (argsLen == thisMethodsArgsLen) {
            return true;
        }
        
        // TODO check for optional args...this might just work...
        return false;
    }

    private List<? extends Expression> extractArgsExpressions(Expression args) {
        List<? extends Expression> argExpr;
        if (args instanceof ArgumentListExpression) {
            ArgumentListExpression argsList = (ArgumentListExpression) args;
            argExpr = argsList.getExpressions();
        } else {
            NamedArgumentListExpression argsList = (NamedArgumentListExpression) args;
            argExpr = argsList.getMapEntryExpressions();
        }
        return argExpr;
    }
    
    /**
     * Looks for the selected method call expression as a closure 
     */
    private List<IJavaElement> createLocalClosureCallProposals(
            IDeclarationSearchInfo info, MethodCallExpression expr) {
        List<IJavaElement> proposals = new LinkedList<IJavaElement>();
        ISourceCodeContext[] allContexts = createContexts(info);
        ISourceCodeContext sourceContext = allContexts[allContexts.length-1];
        VariableScope scope = getVariableScope(sourceContext);
        while (scope != null) {
            for (Iterator<Variable> varIter = scope.getDeclaredVariablesIterator(); varIter.hasNext();) {
                Variable var = (Variable) varIter.next();
                
                // I *think* it is ok to ignore vars of type DynamicVariable
                if (var.getName().equals(info.getIdentifier()) && var instanceof ASTNode) {
                    proposals.add(
                            SourceCodeFinder.find(scope.getClassScope(), 
                                    (ASTNode) var, info.getEditorFacade().getFile()));
                }
            }
            scope = scope.getParent();
        }
        return proposals;
    }
    
    
    private VariableScope getVariableScope(ISourceCodeContext sourceContext) {
        ASTNode astNode = sourceContext.getASTPath()[sourceContext.getASTPath().length-1];
        if (astNode instanceof MethodNode) {
            astNode = ((MethodNode) astNode).getCode();
        } else if (astNode instanceof ClosureExpression) {
            astNode = ((ClosureExpression) astNode).getCode();
        } else if (astNode instanceof MethodCallExpression) {
            ArgumentListExpression ale = (ArgumentListExpression) 
                    ((MethodCallExpression) astNode).getArguments();
            if (ale.getExpression(0) instanceof ClosureExpression) {
                astNode = ((ClosureExpression) ale.getExpression(0)).getCode();
            }
        }
        try {
            // copied from groovy code that doesn't care abot
            // static type
            java.lang.reflect.Method getVariableScopeField = astNode.getClass().getMethod("getVariableScope");
            return (VariableScope) getVariableScopeField.invoke(astNode);
        } catch (Exception e) {
            return null;
        }
    }


    protected ISourceCodeContext[] createContexts(IDeclarationSearchInfo info) {
        IDocumentFacade facade = info.getEditorFacade();
        IRegion r = info.getRegion();
        ISourceBuffer buffer = new DocumentSourceBuffer(facade.getDocument());
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, facade.adapt(ModuleNode.class), r);
    }

    public IJavaElement[] getProposals(IDeclarationSearchInfo info) {
        
        ISourceBuffer buffer = new DocumentSourceBuffer(info.getEditorFacade().getDocument());
        int offset = info.getRegion().getOffset() + info.getRegion().getLength();
        GroovyProjectFacade project = info.getEditorFacade().getProjectFacade();
        
        SourceContextInfo sourceInfo = SourceContextInfo.create(info.getModuleNode(), project, offset, buffer);
        if (sourceInfo != null) {
            List<IJavaElement> elts = new ArrayList<IJavaElement>();
            try {
                Field[] fields = sourceInfo.lookup.lookupFields(sourceInfo.eval.getName(), sourceInfo.name, false, false, true);
                for (Field field : fields) {
                    ClassType declaring = field.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        IField javaField = type.getField(field.getName());
                        if (javaField.exists()) {
                            elts.add(javaField);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + info.getModuleNode().getClasses().get(0).getName(), e);
            }

            try {
                Method[] methods = sourceInfo.lookup.lookupMethods(sourceInfo.eval.getName(), sourceInfo.name, false, false, true);
                for (Method method : methods) {
                    ClassType declaring = method.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        List<String> paramTypeSigs = new ArrayList<String>(method.getParameters().length);
                        for (Parameter param : method.getParameters()) {
                            paramTypeSigs.add(param.getSignature());
                        }
                        String[] paramTypeSigsArr;
                        if (!type.isReadOnly()) {
                            // source type, must convert method type signatures to be unresolved
                            //  FIXADE M2 this could be a problem for source types that do not use qualified names
                            // maybe need to do a different way of finding method
                            paramTypeSigsArr = paramTypeSigs.toArray(new String[0]);
                            for (int i = 0; i < paramTypeSigsArr.length; i++) {
                                if (paramTypeSigsArr[i].charAt(0) == 'L') {
                                    paramTypeSigsArr[i] = "Q" + paramTypeSigsArr[i].substring(1);
                                }
                            }
                        } else {
                            paramTypeSigsArr = paramTypeSigs.toArray(new String[0]);
                        }
                         
                        IMethod javaMethod = type.getMethod(method.getName(), paramTypeSigsArr);
                        if (javaMethod.exists()) {
                            elts.add(javaMethod);
                        } else {
                            // might be a synthetic method or one added through AST transformations
                            // add the declaring class instead
                            elts.add(type);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + info.getModuleNode().getClasses().get(0).getName(), e);
            }
            
            try {
                Property[] properties = sourceInfo.lookup.lookupProperties(sourceInfo.eval.getName(), sourceInfo.name, false, false, true);
                for (Property property : properties) {
                    ClassType declaring = property.getDeclaringClass();
                    IType type = project.getProject().findType(declaring.getName(), new NullProgressMonitor());
                    if (type != null && type.exists()) {
                        IMethod javaMethod = type.getMethod(property.getName(), new String[0]);
                        if (javaMethod.exists()) {
                            elts.add(javaMethod);
                        }
                    }
                }
            } catch (JavaModelException e) {
                GroovyCore.logException("Error while code browsing on " + info.getModuleNode().getClasses().get(0).getName(), e);
            }
            
            return elts.toArray(new IJavaElement[0]);
        } else {
            return new IJavaElement[0];
        }
    }
    
    protected ISourceCodeContext[] createContexts(ModuleNode moduleNode, ISourceBuffer buffer, int offset) {
        SourceCodeContextFactory factory = new SourceCodeContextFactory();
        return factory.createContexts(buffer, moduleNode, new Region(offset, 0));
    }
}