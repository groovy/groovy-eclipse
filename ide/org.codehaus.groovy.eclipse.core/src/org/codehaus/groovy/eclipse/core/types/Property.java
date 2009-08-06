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

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

public class Property extends Member {
	private boolean readable;

	private boolean writable;

	public Property(String signature, int modifiers, String name, boolean readable, boolean writable, ClassType declaringClass) {
		this(signature, modifiers, name, readable, writable, declaringClass, false);
	}
	
	public Property(String signature, int modifiers, String name, boolean readable, boolean writable,
			ClassType declaringClass, boolean inferred) {
		super(signature, modifiers, name, declaringClass, inferred);
		this.readable = readable;
		this.writable = writable;
	}

	public boolean isReadable() {
		return readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		try {
			Property property = (Property) obj;
			return super.equals(obj) && readable == property.readable && writable == property.writable;
		} catch (ClassCastException e) {
			return false;
		}
	}
	
	public int hashCode() {
		int code = super.hashCode();
		code += readable ? 1 : -1;
		code += writable ? 1 : -1;
		return code;
	}

	public String toString() {
		return "Property:" + name + " " + signature + " - " + declaringClass;
	}

	/**
	 * return the field associated with this property
	 */
    @Override
    public IJavaElement toJavaElement(GroovyProjectFacade project) {
        IJavaElement elt = getDeclaringClass().toJavaElement(project);
        if (elt != null && elt.getElementType() == IJavaElement.TYPE) {
            return ((IType) elt).getField(name);
        }
        return null;
    }
}
