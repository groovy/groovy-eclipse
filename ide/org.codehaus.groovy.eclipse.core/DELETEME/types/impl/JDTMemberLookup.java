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

package org.codehaus.groovy.eclipse.core.types.impl;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.AbstractMemberLookup;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.objectweb.asm.Opcodes;

/**
 * @author Andrew Eisenberg
 * @created Sep 19, 2009
 * Looks up members using JDT
 */
public class JDTMemberLookup extends AbstractMemberLookup implements
IGroovyProjectAware {
    
    private GroovyProjectFacade project;
    
    private ITypeHierarchy cachedHierarchy;
    private String targetType;
    
    public JDTMemberLookup(GroovyProjectFacade project) {
        this.project = project;
    }
    
    @Override
    protected List<Field> collectAllFields(String typeName) {
        List<Field> fields = new ArrayList<Field>();

        // if typeName is an array, then treat as Object and add the length field
        if (typeName.charAt(0) == '[') {
            fields.add(TypeUtil.newField("length", "I", typeName, Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL));
            typeName = "java.lang.Object";
        }
        try {
            for (IType toFind : getAllTypes(typeName)) {
                for (IField field : toFind.getFields()) {
                    fields.add(TypeUtil.newField(field, toFind));
                }
            }
        } catch (Exception e) {
            GroovyCore.logException("Exception raised", e);
        }
        return fields;
    }
    
    @Override
    protected List<Method> collectAllMethods(String typeName) {
        List<Method> methods = new ArrayList<Method>();
        // if typeName is an array, then treat as Object and add the length field
        if (typeName.charAt(0) == '[') {
            typeName = "java.lang.Object";
        }
        try {
            for (IType toFind : getAllTypes(typeName)) {
                for (IMethod method : toFind.getMethods()) {
                    methods.add(TypeUtil.newMethod(method, toFind));
                }
            }
        } catch (Exception e) {
            GroovyCore.logException("Exception raised", e);
        }
        return methods;
    }
    
    /**
     * @param typeName
     * @return
     */
    private IType[] getAllTypes(String typeName) {
        if (!typeName.equals(targetType)) {
            targetType = typeName;
            createHierarchy(typeName);
        }
        return cachedHierarchy != null ? cachedHierarchy.getAllTypes() : new IType[0];
    }

    /**
     * @param type
     */
    private void createHierarchy(String typeName) {
        try {
            IType type = project.getProject().findType(typeName, new NullProgressMonitor());
            if (type != null) {
                cachedHierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
            } else {
                cachedHierarchy = null;
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Error creating type hierarchy for " + typeName, e);
        }
    }


    public void setGroovyProject(GroovyProjectFacade project) {
        this.project = project;
    }
    
}
