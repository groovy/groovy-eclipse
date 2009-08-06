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

import java.util.Set;

import org.codehaus.groovy.eclipse.core.IGroovyProjectAware;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContext;
import org.codehaus.groovy.eclipse.core.context.ISourceCodeContextAware;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.types.Field;
import org.codehaus.groovy.eclipse.core.types.IMemberLookup;
import org.codehaus.groovy.eclipse.core.types.Method;
import org.codehaus.groovy.eclipse.core.types.Property;
import org.codehaus.groovy.eclipse.core.types.TypeUtil;
import org.codehaus.groovy.eclipse.core.util.SetUtil;

/**
 * Compose a number of lookups into one lookup.
 * 
 * @author empovazan
 */
public class CompositeLookup implements IMemberLookup, IGroovyProjectAware,
        ISourceCodeContextAware {
    private final IMemberLookup[] lookups;

    /**
     * @param lookups
     *            The array of lookups which may be an empty array.
     */
    public CompositeLookup(IMemberLookup[] lookups) {
        this.lookups = lookups;
    }

    public Field[] lookupFields(final String type, final String prefix,
            final boolean accessible, final boolean staticAccess,
            final boolean exact) {
        if (lookups.length == 0)
            return TypeUtil.NO_FIELDS;
        final Set<Field> resultSet = SetUtil.linkedSet();
        for (final IMemberLookup lookup : lookups)
            SetUtil.setAdd(resultSet, lookup.lookupFields(type, prefix,
                    accessible, staticAccess, exact));
        return resultSet.toArray(new Field[0]);
    }

    public Property[] lookupProperties(final String type, final String prefix,
            final boolean accessible, final boolean staticAccess,
            final boolean exact) {
        if (lookups.length == 0)
            return TypeUtil.NO_PROPERTIES;
        final Set<Property> resultSet = SetUtil.linkedSet();
        for (final IMemberLookup lookup : lookups)
            SetUtil.setAdd(resultSet, lookup.lookupProperties(type, prefix,
                    accessible, staticAccess, exact));
        return resultSet.toArray(new Property[0]);
    }

    public Method[] lookupMethods(final String type, final String prefix,
            final boolean accessible, final boolean staticAccess,
            final boolean exact) {
        if (lookups.length == 0)
            return TypeUtil.NO_METHODS;
        final Set<Method> resultSet = SetUtil.linkedSet();
        for (final IMemberLookup lookup : lookups)
            SetUtil.setAdd(resultSet, lookup.lookupMethods(type, prefix,
                    accessible, staticAccess, exact));
        return resultSet.toArray(new Method[0]);
    }

    public Method[] lookupMethods(final String type, final String prefix,
            final String[] paramTypes, final boolean accessible,
            final boolean staticAccess, final boolean exact) {
        if (lookups.length == 0)
            return TypeUtil.NO_METHODS;
        final Set<Method> resultSet = SetUtil.linkedSet();
        for (final IMemberLookup lookup : lookups)
            SetUtil.setAdd(resultSet, lookup.lookupMethods(type, prefix,
                    paramTypes, accessible, staticAccess, exact));
        return resultSet.toArray(new Method[0]);
    }

    public void setGroovyProject(final GroovyProjectFacade project) {
        for (final IMemberLookup lookup : lookups) {
            if (!(lookup instanceof IGroovyProjectAware)) {
                continue;
            }

            ((IGroovyProjectAware) lookup).setGroovyProject(project);
        }
    }

    public void setSourceCodeContext(final ISourceCodeContext context) {
        for (final IMemberLookup lookup : lookups) {
            if (!(lookup instanceof ISourceCodeContextAware))
                continue;
            ((ISourceCodeContextAware) lookup).setSourceCodeContext(context);
        }
    }
}
