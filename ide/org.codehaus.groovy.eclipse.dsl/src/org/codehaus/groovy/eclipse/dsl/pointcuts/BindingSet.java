/*******************************************************************************
 * Copyright (c) 2011 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Andrew Eisenberg - Initial implemenation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A set of elements bound under the current evaluated pointcut
 * BindingSet 
 * @author andrew
 * @created Feb 10, 2011
 */
public class BindingSet {

    // the table of named bindings built up through contained pointcuts
    private final Map<String, Collection<Object>> namedBindings = new HashMap<String, Collection<Object>>();
    
    
    public BindingSet() {
    }

    /**
     * Augments the existing named binding with the collection value.
     * 
     * Creates the binding if it doesn't exist yet
     * @param name
     * @param value should not be null
     * @return
     */
    public BindingSet addToBinding(String name, Collection<?> value) {
        Collection<Object> binding = namedBindings.get(name);
        if (binding == null) {
            binding = new HashSet<Object>();
            namedBindings.put(name, binding);
        }
        binding.addAll(value);
        return this;
    }

    public Map<String, Collection<Object>> getBindings() {
        return Collections.unmodifiableMap(namedBindings);
    }
    
    public Collection<Object> getBinding(String name) {
        return namedBindings.get(name);
    }

    
    public int size() {
        return namedBindings.size();
    }
}
