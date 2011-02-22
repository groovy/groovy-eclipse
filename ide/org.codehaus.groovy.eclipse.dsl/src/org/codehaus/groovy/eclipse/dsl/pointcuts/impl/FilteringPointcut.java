/*
 * Copyright 2003-2010 the original author or authors.
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

    public final BindingSet matches(GroovyDSLDContext pattern) {
        
        List<T> outerList = filterOuterBindingByType(pattern);
        if (outerList == null || outerList.size() == 0) {
            return null;
        }
        
        Object first = getFirstArgument();
        if (first instanceof IPointcut) {
            pattern.setOuterPointcutBinding(reduce(outerList));
            return ((IPointcut) first).matches(pattern);
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
    public String verify() {
        String hasOneOrNoArgs = hasOneOrNoArgs();
        if (hasOneOrNoArgs != null) {
            return hasOneOrNoArgs;
        }
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg == null) {
            return super.verify();
        }
        return oneStringOrOnePointcutArg;
    }
}
