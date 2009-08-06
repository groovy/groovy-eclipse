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
package org.codehaus.groovy.eclipse.core.types;

import static org.codehaus.groovy.eclipse.core.types.GroovyDeclaration.Kind.FIELD;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;


public class Field extends Member {
	public Field(String signature, int modifiers, String name, ClassType declaringClass) {
		this(signature, modifiers, name, declaringClass, false);
	}
	
	public Field(String signature, int modifiers, String name, ClassType declaringClass, boolean inferred) {
		super(signature, modifiers, name, declaringClass, inferred);
	}
	
	public Kind getType() {
		return FIELD;
	}

	public String toString() {
		return "Field:" + name + " " + signature + " - " + declaringClass;
	}
	
    @Override
    public IJavaElement toJavaElement(GroovyProjectFacade project) {
        IJavaElement elt = getDeclaringClass().toJavaElement(project);
        if (elt != null && elt.getElementType() == IJavaElement.TYPE) {
            return ((IType) elt).getField(name);
        }
        return null;
    }
}