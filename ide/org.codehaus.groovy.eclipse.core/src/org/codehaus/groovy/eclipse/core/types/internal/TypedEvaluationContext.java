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

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.ClassType;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.ISymbolTable;
import org.codehaus.groovy.eclipse.core.types.ITypeEvaluationContext;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Modifiers;
import org.codehaus.groovy.eclipse.core.types.Parameter;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.GroovyDeclaration;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;

/**
 * Implementation of ITypeEvaluationContext which assumes all types are known.
 * <p>
 * Intended for creation using the TypeEvaluationContextBuilder.
 * 
 * @author empovazan
 */
public class TypedEvaluationContext implements ITypeEvaluationContext {
	private static final String[] NO_IMPORTS = new String[0];
	private static final Parameter[] NO_PARAMETERS = new Parameter[0];
	private static final String OBJECT_TYPE = "java.lang.Object";
	private static final ClassType DECLARING_CLASS = TypeUtil.newClassType(java.lang.Object.class);
	
	private GroovyProjectFacade project;
	
	private ClassLoader classLoader;

	private String[] imports = NO_IMPORTS;

	private ISymbolTable symbolTable;

	private IMemberLookup memberLookup;
	
    public GroovyProjectFacade getProject() {
        return project;
    }

    public TypedEvaluationContext(GroovyProjectFacade project) {
        this.project = project;
    }

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void setImports(String[] imports) {
		this.imports = imports;
	}

	public void setSymbolTable(ISymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	public void setMemberLookup(IMemberLookup memberLookup) {
		this.memberLookup = memberLookup;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String[] getImports() {
		return imports;
	}
	
	public ISymbolTable getSymbolTable() {
		return symbolTable;
	}
	
	public IMemberLookup getMemberLookup() {
		return memberLookup;
	}
	
	public GroovyDeclaration lookupSymbol(String name) {
		GroovyDeclaration type =  symbolTable.lookup(name);
		if (type != null) {
			return type; 
		}
		return new Field(OBJECT_TYPE, Modifiers.ACC_PUBLIC, name, DECLARING_CLASS, true);
	}

	public Field lookupField(String type, String name, boolean accessible, boolean staticAccess) {
		Field[] fields = memberLookup.lookupFields(type, name, accessible, staticAccess, true);
		if (fields.length != 0) {
			return fields[0];
		} else {
			return new Field(OBJECT_TYPE, Modifiers.ACC_PUBLIC, name, DECLARING_CLASS, true);
		}
	}

	public Property lookupProperty(String type, String name, boolean accessible, boolean staticAccess) {
		Property[] properties = memberLookup.lookupProperties(type, name, accessible, staticAccess, true);
		if (properties.length != 0) {
			return properties[0];
		} else {
			return null;
			//return new Property(OBJECT_TYPE, Modifiers.ACC_PUBLIC, name, true, true, DECLARING_CLASS, true);
		}
	}

	public Method lookupMethod(String type, String name, String[] paramTypes, boolean accessible, boolean staticAccess) {
		Method[] methods = memberLookup.lookupMethods(type, name, paramTypes, accessible, staticAccess, true);
		if (methods.length != 0) {
			return methods[0];
		} else {
			return new Method(Modifiers.ACC_PUBLIC, name, NO_PARAMETERS, OBJECT_TYPE, DECLARING_CLASS, true);
		}
	}
}