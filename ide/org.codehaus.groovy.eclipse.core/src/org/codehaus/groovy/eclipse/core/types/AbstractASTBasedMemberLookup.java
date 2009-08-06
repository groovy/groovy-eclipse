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

import static org.codehaus.groovy.eclipse.core.types.TypeUtil.newClassType;
import static org.codehaus.groovy.eclipse.core.types.TypeUtil.newMethod;
import static org.codehaus.groovy.eclipse.core.util.ListUtil.newList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

public abstract class AbstractASTBasedMemberLookup extends AbstractMemberLookup {
    protected abstract ClassNode getClassNodeForName(final String type);

    @Override
    protected List<Field> collectAllFields(String type) {
        final List<Field> results = new ArrayList<Field>();
        return collectAllFields(results, getClassNodeForName(type));
    }

    private List<Field> collectAllFields(List<Field> results,
            ClassNode classNode) {
        if (classNode == null) {
            return results;
        }

        // Collect super fields first
        ClassNode superClass = classNode.getSuperClass();
        if (superClass != null) {
            collectAllFields(results, superClass);
        }

        // Now collect fields.
        final List<FieldNode> fieldNodes = newList((List<FieldNode>) classNode
                .getFields());
        ClassType declaringClass = TypeUtil.newClassType(classNode);
        for (Iterator iter = fieldNodes.iterator(); iter.hasNext();) {
            FieldNode fieldNode = (FieldNode) iter.next();
            results.add(TypeUtil.newField(declaringClass, fieldNode));
        }
        return results;
    }

    @Override
    protected List<Property> collectAllProperties(String type) {
        final List<Property> results = new ArrayList<Property>();
        return collectAllProperties(results, getClassNodeForName(type));
    }

    private List<Property> collectAllProperties(List<Property> results,
            ClassNode classNode) {
        if (classNode == null) {
            return results;
        }

        List properties = classNode.getProperties();
        if (properties.size() > 0) {
            ClassType declaringClass = TypeUtil.newClassType(classNode);
            for (Iterator iter = properties.iterator(); iter.hasNext();) {
                PropertyNode propertyNode = (PropertyNode) iter.next();
                results.add(TypeUtil.newProperty(declaringClass, propertyNode));
            }
        }
        return results;
    }

    @Override
    protected List<Method> collectAllMethods(final String type) {
        return collectAllMethods(newList(new Method[0]),
                getClassNodeForName(type));
    }

    private List<Method> collectAllMethods(List<Method> results,
            ClassNode classNode) {
        if (classNode == null)
            return results;

        // Lookup super method first.
        ClassNode superClass = classNode.getSuperClass();
        if (superClass != null) {
            collectAllMethods(results, superClass);
        }

        // Lookup interfaces.
        ClassNode[] interfaces = classNode.getInterfaces();
        if (interfaces != null) { // null or empty array? It is not documented.
            for (int i = 0; i < interfaces.length; ++i) {
                collectAllMethods(results, interfaces[i]);
            }
        }

        // Now lookup locals.
        final List<MethodNode> methodNodes = new ArrayList<MethodNode>();
        for (final Object methodNode : classNode.getMethods())
            methodNodes.add((MethodNode) methodNode);
        final ClassType declaringClass = newClassType(classNode);
        for (final MethodNode node : methodNodes)
            results.add(newMethod(declaringClass, node));
        return results;
    }

}