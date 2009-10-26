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
package org.codehaus.groovy.eclipse.astviews


import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

class MapTo {
	// TODO: no syntax errors in Eclipse if the package for the below is not imported. Why?
	static mapToNames = [
      	(ClassNode) : { "${it.name}" },
      	(Expression) : { "${it.class.canonicalName}" },
      	(ExpressionStatement) : { "(${it.expression.class.canonicalName}) ${it.expression.text}" },
      	(FieldNode) : { it.name },
      	(MethodNode) : { it.name },
      	(ModuleNode) : { it.description },
      	(Parameter) : { it.name },
      	(PropertyNode) : { it.name },
    ]	
	
	static String names(Object value) {
		def cls = value.getClass()
		def getter = mapToNames[cls]
		while (getter == null && cls != null) {
			cls = cls.superclass
			getter = mapToNames[cls]
		}
		return getter?.call(value)
	}
}