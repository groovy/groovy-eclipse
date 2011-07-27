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
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

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
    
    public FilteringPointcut(IStorage containerIdentifier, String pointcutName, Class<T> filterBy) {
        super(containerIdentifier, pointcutName);
        this.filterBy = filterBy;
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        
        Collection<T> explodedList = explodeObject(toMatch);
        if (explodedList == null || explodedList.size() == 0) {
            // nothing to match on, return failure
            return null;
        }
        
        Object first = getFirstArgument();
        if (first instanceof IPointcut) {
            // pass the exploded list to the inner pointcut and match on each element of the list
            return matchOnPointcutArgument((IPointcut) first, pattern, explodedList);
        } else {
            Collection<?> filtered = filterResult(explodedList, pattern);
            if (filtered != null) {
                return filtered;
            }
            return null;
        }
    }
    
    protected Collection<?> filterResult(Collection<T> results, GroovyDSLDContext context) {
        Object o = getFirstArgument();
        String firstArg = asString(o);
        Collection<T> filtered = new ArrayList<T>(results.size());
        for (T obj : results) {
            T maybe = filterObject(obj, context, firstArg);
            if (maybe != null) {
                filtered.add(maybe);
            }
        }
        return reduce(filtered);
    }

    /**
     * @param o
     * @return
     */
    protected String asString(Object o) {
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Class) {
            return ((Class<?>) o).getName();
        } else if (o instanceof ClassNode) {
            return ((ClassNode) o).getName();
        } else if (o instanceof ClassExpression) {
            return ((ClassExpression) o).getType().getName();
        }
        return null;
    }

    protected Collection<T> reduce(Collection<T> filtered) {
        if (filtered == null || filtered.size() == 0) {
            return null;
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
     * Converts element to a collection of the {@link #filterBy} type.
     * or returns null if no match
     */
    protected Collection<T> explodeObject(Object toMatch) {
        if (toMatch instanceof  Collection<?>) {
            List<T> objs = new ArrayList<T>();
            for (Object elt : (Collection<?>) toMatch) {
                if (filterBy.isInstance(elt)) {
                    objs.add((T) elt);
                }
            }
            if (objs.size() > 0) {
                return objs;
            }
        } else if (filterBy.isInstance(toMatch)) {
            return Collections.singletonList((T) toMatch);
        }
        return null;
    }

    /**
     * Expecting one argument that can either be a string or another pointcut
     */
    @Override
    public void verify() throws PointcutVerificationException {
        super.verify();

        String oneStringOrOnePointcutArg = oneStringOrOnePointcutOrOneClassArg();
        if (oneStringOrOnePointcutArg != null) {
            String hasNoArgs = hasNoArgs();
            if (hasNoArgs != null) {
                throw new PointcutVerificationException(
                        "This pointcut expects either no arguments or 1 String or 1 pointcut argument", this);
            }
        }
    }
}
