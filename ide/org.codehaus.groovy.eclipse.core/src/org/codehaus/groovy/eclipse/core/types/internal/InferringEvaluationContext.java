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
package org.codehaus.groovy.eclipse.core.types.internal;

import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.inference.internal.Infer;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;

/**
 * Implementation of ITypeEvaluationContext which attempts to infer types.
 * <p>
 * Intended for creation using the TypeEvaluationContextBuilder.
 * 
 * @author empovazan
 */
public class InferringEvaluationContext extends TypedEvaluationContext {

    public InferringEvaluationContext(GroovyProjectFacade project) {
        super(project);
    }

    protected ISourceCodeContext sourceCodeContext;

	public void setSourceCodeContext(ISourceCodeContext sourceCodeContext) {
		this.sourceCodeContext = sourceCodeContext;
	}

	public ISourceCodeContext getSourceCodeContext() {
		return sourceCodeContext;
	}

	public GroovyDeclaration lookupSymbol(String name) {
		GroovyDeclaration type = super.lookupSymbol(name);
		if (!type.isInferred()) {
			switch (type.getType()) {
				case LOCAL_VARIABLE:
					return Infer.localVariable((LocalVariable) type, this);
				case FIELD:
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
		if (!method.isInferred()) {
			return Infer.method(method, this);
		}
		return method;
	}
}