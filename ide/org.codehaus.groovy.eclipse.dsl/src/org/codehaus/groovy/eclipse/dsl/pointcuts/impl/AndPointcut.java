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

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * Takes two or more elements and 
 * @author andrew
 * @created Feb 10, 2011
 */
public class AndPointcut extends AbstractPointcut {

    public AndPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        Object[] args = getArgumentValues();
        BindingSet bindings = null;
        Object outerBinding = pattern.getOuterPointcutBinding();
        for (Object arg : args) {
            // must re-set this value each time since each segment of the
            // and pointcut will change this value 
            pattern.setOuterPointcutBinding(outerBinding);
            BindingSet other = ((IPointcut) arg).matches(pattern);
            if (other != null) {
                if (bindings == null) {
                    bindings = other;
                } else {
                    bindings.combineBindings(other);
                }
            } else {
                // failure
                return null;
            }
        }
        return bindings;
    }

    public IPointcut normalize() {
        Object[] args = getArgumentValues();
        for (Object arg : args) {
            ((IPointcut) arg).normalize();
        }
        return super.normalize();
    }

    @Override
    public String verify() {
        String allArgsArePointcuts = allArgsArePointcuts();
        if (allArgsArePointcuts == null) {
            return super.verify();
        } else {
            return allArgsArePointcuts;
        }
    }
    
    @Override
    protected IPointcut and(IPointcut other) {
        if (other instanceof AndPointcut) {
            Object[] argumentValues = other.getArgumentValues();
            String[] argumentNames = other.getArgumentNames();
            int argCount = argumentNames.length;
            for (int i = 0; i < argCount; i++) {
                this.addArgument(argumentNames[i], argumentValues[i]);
            }
        } else {
            addArgument(other);
        }
        return this;
    }
}
