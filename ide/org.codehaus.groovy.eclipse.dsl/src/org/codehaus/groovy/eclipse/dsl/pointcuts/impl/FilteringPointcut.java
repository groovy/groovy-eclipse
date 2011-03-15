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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;

/**
 * An abstract pointcut that filters the value of {@link GroovyDSLDContext#getOuterPointcutBinding()}.
 * 
 * You can think of the filter pointcut as taking a set of things from the outer pointcut, and 
 * either sending it to the inner pointcut, which will do its own filter, or performing a filter
 * using its argument.
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public abstract class FilteringPointcut<T> extends AbstractPointcut {

    private final Class<T> filterBy;
    
    public FilteringPointcut(String containerIdentifier, Class<T> filterBy) {
        super(containerIdentifier);
        this.filterBy = filterBy;
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        
        List<T> outerList = filterOuterBindingByType(pattern);
        if (outerList == null || outerList.size() == 0) {
            return null;
        }
        
        Object first = getFirstArgument();
        if (first instanceof IPointcut) {
            pattern.setOuterPointcutBinding(reduce(outerList));
            return matchOnPointcutArgument((IPointcut) first, pattern);
        } else {
            Object filtered = filterResult(outerList, pattern);
            if (filtered != null) {
                return new BindingSet(filtered);
            }
            return null;
        }
    }
    
    protected Object filterResult(List<T> results, GroovyDSLDContext context) {
        Object o = getFirstArgument();
        String firstArg = o instanceof String ? (String) o : null;
        List<T> filtered = new ArrayList<T>(results.size());
        for (T obj : results) {
            T maybe = filterObject(obj, context, firstArg);
            if (maybe != null) {
                filtered.add(maybe);
            }
        }
        return reduce(filtered);
    }

    protected Object reduce(List<T> filtered) {
        if (filtered == null || filtered.size() == 0) {
            return null;
        } else if (filtered.size() == 1) {
            return filtered.get(0);
        } else {
            return filtered;
        }
    }

    /**
     * Filters an individual object based on some criteria in the concrete query
     * @param result
     * @return
     */
    protected abstract T filterObject(T result, GroovyDSLDContext context, String firstArgAsString);


    /**
     * extracts annotated nodes from the outer binding, or from the current type if there is no outer binding
     * the outer binding should be either a {@link Collection} or a {@link ClassNode}
     */
    protected List<T> filterOuterBindingByType(GroovyDSLDContext pattern) {
        Object outer = pattern.getOuterPointcutBinding();
        if (outer == null && filterBy.isInstance(pattern.getCurrentType())) {
            return Collections.singletonList((T) pattern.getCurrentType());
        } else {
            if (outer instanceof  Collection<?>) {
                List<T> objs = new ArrayList<T>();
                for (Object elt : (Collection<Object>) outer) {
                    if (filterBy.isInstance(elt)) {
                        objs.add((T) elt);
                    }
                }
                return objs;
            } else if (filterBy.isInstance(outer)) {
                return Collections.singletonList((T) outer);
            }
        }
        return null;
    }

    /**
     * Expecting one argument that can either be a string or another pointcut
     */
    @Override
    public void verify() throws PointcutVerificationException {
        super.verify();

        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg != null) {
            String hasNoArgs = hasNoArgs();
            if (hasNoArgs != null) {
                throw new PointcutVerificationException(
                        "This pointcut expects either no arguments or 1 String or 1 pointcut argument", this);
            }
        }
    }
}
