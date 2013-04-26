/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.ast;

import java.util.Iterator;

/**
 * Some static methods that access values on ASTNodes available 
 * in the 1.7 stream, but not in the 1.6 stream
 * @author andrew
 * @created Jun 17, 2010
 */
public class ASTNodeCompatibilityWrapper {
	
	private ASTNodeCompatibilityWrapper() {
		// not instantiable
    }

	/**
	 * This method will never return null
	 */
	public static ClassNode getScriptClassDummy(ModuleNode module) {
	    return module.getScriptClassDummy();
	}
	
	public static Iterator<InnerClassNode> getInnerClasses(ClassNode clazz) {
		return clazz.getInnerClasses();
	}
}
