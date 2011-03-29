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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A set of elements bound under the current evaluated pointcut
 * @author andrew
 * @created Feb 10, 2011
 */
public class BindingSet {
    private final Map<String, Object> bindings = new HashMap<String, Object>();
    
    public BindingSet() {
    }
    public BindingSet(Object defaultBinding) {
        addDefaultBinding(defaultBinding);
    }
    public BindingSet(String name, Object defaultBinding) {
        if (name != null) {
            addBinding(name, defaultBinding);
        }
        addDefaultBinding(defaultBinding);
    }

    public BindingSet addBinding(String name, Object value) {
        bindings.put(name, value);
        return this;
    }
    
    public BindingSet addDefaultBinding(Object value) {
        bindings.put(null, value);
        return this;
    }
    
    public Map<String, Object> getBindings() {
        return Collections.unmodifiableMap(bindings);
    }

    public BindingSet combineBindings(BindingSet other) {
        Object newDefaultBinding = other.getDefaultBinding();
        Object origDefaultBinding = this.getDefaultBinding();
        bindings.putAll(other.getBindings());
        // now need to combine the default bindings
        addDefaultBinding(combine(newDefaultBinding, origDefaultBinding));
        return this;
    }
    
    /**
     * Combines the two elements into a single element.  If one or both of the elements are collections, then 
     * a collections of the combined elements are returned.
     * @param newDefaultBinding
     * @param origDefaultBinding
     * @return a combined object
     */
    private Object combine(Object newDefaultBinding, Object origDefaultBinding) {
        Object newObj;
        if (newDefaultBinding == null) {
            newObj = origDefaultBinding;
        } else if (origDefaultBinding == null) {
            newObj = newDefaultBinding;
        } else 
        
        // combine collections if one or both are collections, else create a new collection and return it
        if (newDefaultBinding instanceof Set<?>) {
            if (origDefaultBinding instanceof Collection<?>) {
                ((Set<Object>) newDefaultBinding).addAll((Collection<Object>) origDefaultBinding);
            } else {
                ((Set<Object>) newDefaultBinding).add(origDefaultBinding);
            }
            newObj = newDefaultBinding;
        } else if (newDefaultBinding instanceof List<?>) {
            if (origDefaultBinding instanceof Collection<?>) {
                ((List<Object>) newDefaultBinding).addAll((Collection<Object>) origDefaultBinding);
            } else {
                ((List<Object>) newDefaultBinding).add(origDefaultBinding);
            }
            newObj = newDefaultBinding;
        } else if (origDefaultBinding instanceof Set<?>) {
            ((Set<Object>) origDefaultBinding).add(newDefaultBinding);
            newObj = origDefaultBinding;
        } else if (origDefaultBinding instanceof List<?>) {
            ((List<Object>) origDefaultBinding).add(newDefaultBinding);
            newObj = origDefaultBinding;
        } else {
            // neither are collections, so create a new one
            List<Object> newList = new ArrayList<Object>(2);
            newList.add(origDefaultBinding);
            newList.add(newDefaultBinding);
            newObj = newList;
        }
    
        return newObj;
    }

    /**
     * Intersects the two bindings.  All named bindings from either binding will be 
     * retained (an one may overwrite the other, but the default binding will be intersected.
     * That is, non-equal parts will be removed.  If there is no overlap between the default bindings,
     * then <code>null</code> is returned, indicating that this pointcut matching has failed.
     * 
     * @param other
     * @return an combination of the two bindings, including an intersection of the default 
     * binding or null if there is no intersection.
     */
    public BindingSet intersectBindings(BindingSet other) {
        Object newDefaultBinding = other.getDefaultBinding();
        Object origDefaultBinding = this.getDefaultBinding();
        bindings.putAll(other.getBindings());
        // now need to intersect the default bindings
        Object intersection = intersect(newDefaultBinding, origDefaultBinding);
        if (intersection == null) {
            // no overlap
            return null;
        }
        addDefaultBinding(intersection);
        return this;
    }
    
    
    
    /**
     * @param newDefaultBinding
     * @param origDefaultBinding
     * @return
     */
    private Object intersect(Object newDefaultBinding, Object origDefaultBinding) {
        Object newObj;
        if (newDefaultBinding == null) {
            newObj = null;
        } else if (origDefaultBinding == null) {
            newObj = null;
        } else 
        
        // combine collections if one or both are collections, else create a new collection and return it
        if (newDefaultBinding instanceof Set<?>) {
            if (origDefaultBinding instanceof Collection<?>) {
                ((Set<Object>) newDefaultBinding).retainAll((Collection<Object>) origDefaultBinding);
                newObj = newDefaultBinding;
            } else {
                if (((Set<Object>) newDefaultBinding).contains(origDefaultBinding)) {
                    newObj = origDefaultBinding;
                } else {
                    newObj = null;
                }
            }
        } else if (newDefaultBinding instanceof List<?>) {
            if (origDefaultBinding instanceof Collection<?>) {
                ((List<Object>) newDefaultBinding).retainAll((Collection<Object>) origDefaultBinding);
                newObj = newDefaultBinding;
            } else {
                if (((List<Object>) newDefaultBinding).contains(origDefaultBinding)) {
                    newObj = origDefaultBinding;
                } else {
                    newObj = null;
                }
            }
        } else if (origDefaultBinding instanceof Collection<?>) {
            if (((Collection<Object>) origDefaultBinding).contains(newDefaultBinding)) {
                newObj = newDefaultBinding;
            } else {
                newObj = null;
            }
        } else {
            // neither are collections, so check equality
            if (origDefaultBinding.equals(newDefaultBinding)) {
                newObj = origDefaultBinding;
            } else {
                newObj = null;
            }
        }
        return newObj;
    }

    public Object getBinding(String name) {
        return bindings.get(name);
    }

    /**
     * returns the degault binding, that is the 
     * object that was bound from the previous pointcut
     */
    public Object getDefaultBinding() {
        return bindings.get(null);
    }
    
    public int size() {
        return bindings.size();
    }
}
