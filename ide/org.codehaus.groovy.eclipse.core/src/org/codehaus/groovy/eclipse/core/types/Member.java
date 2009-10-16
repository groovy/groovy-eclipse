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
import static org.codehaus.groovy.eclipse.core.types.GroovyDeclaration.Kind.MEMBER;

public abstract class Member extends GroovyDeclaration {
	protected ClassType declaringClass;

	public Member(String signature, int modifiers, String name, ClassType declaringClass) {
		this(signature, modifiers, name, declaringClass, false);
	}
	
	public Member(String signature, int modifiers, String name, ClassType declaringClass, boolean inferred) {
		super(signature, modifiers, name, inferred);
		this.declaringClass = declaringClass;
	}
	
	public Kind getType() {
		return MEMBER;
	}
	
	public ClassType getDeclaringClass() {
		return declaringClass;
	}
	
	@Override
	public boolean equals(Object obj) {
	    return super.equals(obj) && declaringClass.equals(((Member) obj).getDeclaringClass());
	}
	
	public int hashCode() {
	    return super.hashCode() * declaringClass.hashCode();
	}
	@Override
	protected boolean similarKinds(GroovyDeclaration rhs) {
	    return rhs.getType() == MEMBER || rhs.getType() == FIELD;
	}
	
	@Override
	public boolean isSimilar(GroovyDeclaration rhs) {
	    return super.isSimilar(rhs) && declaringClass.isSimilar(((Member) rhs).declaringClass);
	}
}
