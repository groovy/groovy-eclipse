/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core.inference.internal;

import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;
import static org.codehaus.groovy.eclipse.core.util.MapUtil.newMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluationContextBuilder;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.codehaus.groovy.eclipse.core.types.TypeEvaluator.EvalResult;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;
import org.eclipse.jface.text.Region;

/**
 * Operation to infer the return type of a method.
 * 
 * @author empovazan
 */
public class InferMethodReturnTypeOperation {
	/**
	 * Recursive calls will lead to an infinitite type inference loop. The visit count is tracked here. A second visit
	 * occurs in order to allow for the recursive call to use the types of any other return values in the method.
	 */
	private static ThreadLocal< Map< String, Integer > > mapMethodKeyToVisitCount = new ThreadLocal< Map< String, Integer >>() {
        @Override
        protected Map<String, Integer> initialValue() {
			return newMap();
		}
	};
	private final InferringEvaluationContext evalContext;
	private final Method method;
	
	public InferMethodReturnTypeOperation(Method method, InferringEvaluationContext evalContext) {
		this.evalContext = evalContext;
		this.method = method;
	}

	public Method getMethod() {
		Map mapMethodKeyToVisitCount = InferMethodReturnTypeOperation.mapMethodKeyToVisitCount.get();
		MethodNode methodNode = getMethodNode(evalContext, method);
		String methodKey = createMethodKey(methodNode);

		// This is the second visit - return this method flagging the return type as 'ignore'
		// This will be used in the first visit to determine the appropriate type.
		// Note, checking for '1' as increment is done after this method, and first visit appears as '0'.
		if (getVisitCount(methodKey) == 1) {
			ClassType declaringClass = TypeUtil.newClassType(methodNode.getDeclaringClass());
			int modifiers = TypeUtil.convertFromASTModifiers(methodNode.getModifiers());
			String returnType = "$ignore$";
			Parameter[] parameters = TypeUtil.createParameterList(methodNode.getParameters());
			return new Method(modifiers, methodNode.getName(), parameters, returnType, declaringClass, true);
		}
		
		incVisitCount(methodKey);
		try {
			final MethodReturnExpressionCollector collector = new MethodReturnExpressionCollector();
			collector.visitMethod(methodNode);
			final List< Expression > expressions = collector.expressions;
			addImplicitReturnExpression(expressions, methodNode);
	
			String type = evaluateTypes(expressions);

			if (type == null) {
				type = TypeUtil.OBJECT_TYPE;
			}
	
			return new Method(method.getModifiers(), method.getName(), method.getParameters(), type, method
					.getDeclaringClass(), true);
		} finally {
			decVisitCount(methodKey);
		}
	}
	
	private String createMethodKey(MethodNode methodNode) {
		return methodNode.getTypeDescriptor();
	}

	private int getVisitCount(String methodKey) {
		Integer visitCount = (Integer) ((Map)mapMethodKeyToVisitCount.get()).get(methodKey);
		if (visitCount == null) {
			return 0;
		}
		return visitCount.intValue();
	}

	private void incVisitCount(String methodKey) {
		Integer visitCount = mapMethodKeyToVisitCount.get().get( methodKey );
		if (visitCount == null) {
			visitCount = new Integer(1);
		} else {
			visitCount = new Integer(visitCount.intValue() + 1);
		}
		mapMethodKeyToVisitCount.get().put(methodKey, visitCount);
	}

	private void decVisitCount(String methodKey) {
		Integer visitCount = mapMethodKeyToVisitCount.get().get(methodKey);
		if (visitCount != null) {
			if (visitCount.intValue() == 1) {
				mapMethodKeyToVisitCount.get().put(methodKey, null);
			} else {
				visitCount = new Integer(visitCount.intValue() - 1);
				mapMethodKeyToVisitCount.get().put(methodKey, visitCount);
			}
		}
	}

	private String evaluateTypes(List< Expression > expressions) {
	    final List< String > types = newList();
		for (Iterator< Expression > iter = expressions.iterator(); iter.hasNext();) {
			Expression expr = iter.next();
			ITypeEvaluationContext context = new TypeEvaluationContextBuilder().typeEvaluationContext(evalContext)
					.location(new Region(expr.getStart(), expr.getLength())).done();
			TypeEvaluator eval = new TypeEvaluator(context);
			EvalResult result = eval.evaluate(expr);
			if (result != null && !result.getName().equals("void") && !result.getName().equals("$ignore$")) {
				types.add(result.getName());
			}
		}

		mergeTypes(types);

		if (types.size() > 0) {
			return types.get(0);
		}

		return null;
	}

	private void mergeTypes(List types) {
		// FUTURE: it is possible for one return statement to return 10 and another 5.0, in which case the return
		// result should be the common base type Number.
	}

	private void addImplicitReturnExpression(List< Expression > expressions, MethodNode methodNode) {
		List statements = ((BlockStatement)methodNode.getCode()).getStatements();
		if (statements.size() > 0) {
			Statement stmt = (Statement) statements.get(statements.size() - 1);
			if (stmt instanceof ExpressionStatement) {
				expressions.add(((ExpressionStatement)stmt).getExpression());
			}
		}
	}

	private MethodNode getMethodNode(InferringEvaluationContext evalContext, Method method) {
		ModuleNode moduleNode = (ModuleNode) evalContext.getSourceCodeContext().getASTPath()[0];
		List classes = moduleNode.getClasses();
		for (Iterator iter = classes.iterator(); iter.hasNext();) {
			ClassNode classNode = (ClassNode) iter.next();
			List methods = classNode.getMethods(method.getName());
			MethodNode methodNode = findMethodByArgs(methods, method);
			if (methodNode != null) {
				return methodNode;
			}
		}
		throw new IllegalStateException("Internal error - expecting to find method in ModuleNode given a Method type.");
	}

	/**
	 * 
	 * @param methods
	 * @param method
	 * @return
	 */
	private MethodNode findMethodByArgs(List methods, Method method) {
	    final List< MethodNode > candidates = newList();
		Parameter[] parameters1 = method.getParameters();
		try {
			Class[] classes1 = getClassesFromParameters(parameters1);
			for (Iterator iter = methods.iterator(); iter.hasNext();) {
				MethodNode methodNode = (MethodNode) iter.next();
				org.codehaus.groovy.ast.Parameter[] parameters2 = methodNode.getParameters();
				if (parameters1.length == parameters2.length) {
					Class[] classes2 = getClassesFromParameters(parameters2);
					if (checkInvokableTypes(classes1, classes2)) {
						candidates.add(methodNode);
					}
				}
			}
		} catch (ClassNotFoundException e) {
		}
		
		if (candidates.size() == 1) {
			return candidates.get(0);
		} else if (candidates.size() > 1) {
			return chooseInvokableMethod(candidates);
		}
		
		return null;
	}
	
	/**
	 * Given a list of methods with the same number of parameters and compatible types
	 * @param candidates
	 * @return
	 */
	private MethodNode chooseInvokableMethod(List< MethodNode > candidates) {
		// Sort by parameters from subtypes to base types.
		Collections.sort(candidates, new Comparator< MethodNode >() {
			public int compare(MethodNode m1, MethodNode m2) {
				Class[] c1 = getClassesFromParameters(m1.getParameters());
				Class[] c2 = getClassesFromParameters(m2.getParameters());
				if (checkInvokableTypes(c1, c2)) {
					return -1; // m1 parameter types are sub types of m2 parameter types.
				} else if (checkInvokableTypes(c2, c1)) {
					return 1; // opposite of above.
				}
				return 0; // They can't be used to invoke one another, so 'equally uninvokable'.
			}
		});
		return candidates.get(0);
	}

	/**
	 * @param argsTypes Argument types of a caller.
	 * @param paramTypes Parameter types of a potential method to call.
	 * @return True if the arguments are equal type or subtypes of the parameter types.
	 */
	private boolean checkInvokableTypes(Class< ? >[] argsTypes, Class< ? >[] paramTypes) {
		for (int i = 0; i < paramTypes.length; i++) {
			if (!paramTypes[i].isAssignableFrom(argsTypes[i])) {
				return false;
			}
		}
		return true;
	}

	private Class[] getClassesFromParameters(Parameter[] parameters1) throws ClassNotFoundException {
		Class[] classes = new Class[parameters1.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = Class.forName(parameters1[i].getSignature()); 
		}
		return classes;
	}

	private Class[] getClassesFromParameters(org.codehaus.groovy.ast.Parameter[] parameters2) {
		Class[] classes = new Class[parameters2.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = parameters2[i].getType().getTypeClass();
		}
		return classes;
	}

	class MethodReturnExpressionCollector extends ClassCodeVisitorSupport {
		List< Expression > expressions = newList();
		
		@Override
        protected SourceUnit getSourceUnit() {
			return null;
		}
		
		@Override
        public void visitReturnStatement(ReturnStatement statement) {
			super.visitReturnStatement(statement);
			
			expressions.add(statement.getExpression());
		}
	}
}
