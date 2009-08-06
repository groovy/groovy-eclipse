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
package org.codehaus.groovy.eclipse.core.inference.internal;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.LocalVariable;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.internal.InferringEvaluationContext;

/**
 * Inference entry points.
 * 
 * @author empovazan
 */
public class Infer {
	public static GroovyDeclaration localVariable(LocalVariable variable, InferringEvaluationContext evalContext) {
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
