/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import java.util.Collection;
import java.util.HashSet;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;
import org.eclipse.core.resources.IStorage;

/**
 * Takes two or more elements and
 */
public class AndPointcut extends AbstractPointcut {

    public AndPointcut(IStorage containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public Collection<?> matches(GroovyDSLDContext pattern, Object toMatch) {
        Object[] args = getArgumentValues();
        Collection<Object> result = new HashSet<>();
        for (Object arg : args) {
            Collection<?> intermediate = matchOnPointcutArgumentReturnInner((IPointcut) arg, pattern, ensureCollection(toMatch));
            if (intermediate == null) {
                return null;
            }
            result.addAll(intermediate);
        }
        return !result.isEmpty() ? result : null;
    }

    /**
     * Flatten all contained 'and' pointcuts into the top level (but only if they are unnamed)
     */
    @Override
    public IPointcut normalize() {
        IPointcut newPointcut = super.normalize();
        if (newPointcut instanceof AndPointcut) {
            AndPointcut newAnd = (AndPointcut) newPointcut;
            AndPointcut newNewAnd = new AndPointcut(getContainerIdentifier(), "and");
            // flatten the ands
            for (int i = 0; i < newAnd.getArgumentValues().length; i++) {
                String name = newAnd.getArgumentNames()[i];
                Object argument = newAnd.getArgumentValues()[i];
                if (argument instanceof AndPointcut && name == null) {
                    AndPointcut other = (AndPointcut) argument;
                    Object[] argumentValues = other.getArgumentValues();
                    String[] argumentNames = other.getArgumentNames();
                    int argCount = argumentNames.length;
                    for (int j = 0; j < argCount; j++) {
                        newNewAnd.addArgument(argumentNames[j], argumentValues[j]);
                    }
                } else {
                    newNewAnd.addArgument(name, argument);
                }
            }
            return newNewAnd;
        } else {
            return newPointcut;
        }
    }

    @Override
    public void verify() throws PointcutVerificationException {
        // don't call super
        String allArgsArePointcuts = allArgsArePointcuts();
        if (allArgsArePointcuts != null) {
            throw new PointcutVerificationException(allArgsArePointcuts, this);
        }
    }

    /*@Override
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
    }*/
}
