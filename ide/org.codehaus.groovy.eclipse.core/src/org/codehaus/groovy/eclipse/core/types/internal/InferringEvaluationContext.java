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
package org.codehaus.groovy.eclipse.core.types.internal;

import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.inference.internal.Infer;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Type;

/**
 * Implementation of ITypeEvaluationContext which attempts to infer types.
 * <p>
 * Intended for creation using the TypeEvaluationContextBuilder.
 * 
 * @author empovazan
 */
public class InferringEvaluationContext extends TypedEvaluationContext {
	protected ISourceCodeContext sourceCodeContext;

	public void setSourceCodeContext(ISourceCodeContext sourceCodeContext) {
		this.sourceCodeContext = sourceCodeContext;
	}

	public ISourceCodeContext getSourceCodeContext() {
		return sourceCodeContext;
	}

	public Type lookupSymbol(String name) {
		Type type = super.lookupSymbol(name);
		if (type.isGroovyType() && !type.isInferred()) {
			switch (type.getType()) {
				case Type.LOCAL_VARIABLE:
					return Infer.localVariable((LocalVariable) type, this);
				case Type.FIELD:
					return Infer.field((Field)type, this);
				default:
					Field field = (Field) type;
					return new Field(field.getSignature(), field.getModifiers(), field.getName(), field
							.getDeclaringClass(), true);
			}
		}
		return type;
	}
	
	public Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible, boolean staticAccess) {
		Method method = super.lookupMethod(type, name, paramTypes, accessible, staticAccess);
		if (method.isGroovyType() && !method.isInferred()) {
			return Infer.method(method, this);
		}
		return method;
	}
}