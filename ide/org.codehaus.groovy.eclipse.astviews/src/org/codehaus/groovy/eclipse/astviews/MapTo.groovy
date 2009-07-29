/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.astviews


import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.eclipse.core.util.JVM1_4Util

class MapTo {
	// TODO: no syntax errors in Eclipse if the package for the below is not imported. Why?
	static mapToNames = [
      	(ClassNode) : { "${it.name}" },
      	(Expression) : { "${JVM1_4Util.getSimpleName(it.class)}" },
      	(ExpressionStatement) : { "(${JVM1_4Util.getSimpleName(it.expression.class)}) ${it.expression.text}" },
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