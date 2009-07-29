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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Type;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;

/**
 * Inference entry points.
 * 
 * @author empovazan
 */
public class Infer {
	public static Type localVariable(LocalVariable variable, InferringEvaluationContext evalContext) {
		return new InferLocalVariableTypeOperation(variable, evalContext).getLocalVariable();
	}

	public static Field field(Field field, InferringEvaluationContext evalContext) {
		ISourceCodeContext sourceCodeContext = evalContext.getSourceCodeContext();
		ClassNode classNode = (ClassNode) sourceCodeContext.getASTPath()[1]; // module -> class
		if (field.getDeclaringClass().getName().equals(classNode.getName())) {
			return new InferThisClassFieldTypeOperation(field, evalContext).getField();
		} else {
			return new Field(field.getSignature(), field.getModifiers(), field.getName(), field.getDeclaringClass(),
					true);
		}
	}
	
	public static Method method(Method method, InferringEvaluationContext evalContext) {
		return new InferMethodReturnTypeOperation(method, evalContext).getMethod();
	}
}
