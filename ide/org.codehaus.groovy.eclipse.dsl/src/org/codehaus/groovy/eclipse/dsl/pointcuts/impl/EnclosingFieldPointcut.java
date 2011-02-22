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

import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.IPointcut;

/**
 * Tests that the type being analyzed matches.  The match can
 * either be a string match (ie - the type name),
 * or it can pass the current type to a containing pointcut
 * @author andrew
 * @created Feb 10, 2011
 */
public class EnclosingFieldPointcut extends AbstractPointcut {

    public EnclosingFieldPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        FieldNode enclosing = pattern.getCurrentScope().getEnclosingFieldDeclaration();
        if (enclosing == null) {
            return null;
        }
        
        Object firstArgument = getFirstArgument();
        if (firstArgument instanceof String) {
            if (enclosing.getName().equals(firstArgument)) {
                return new BindingSet().addDefaultBinding(enclosing);
            } else {
                return null;
            }
        } else {
            pattern.setOuterPointcutBinding(enclosing);
            BindingSet matches = ((IPointcut) firstArgument).matches(pattern);
            if (matches != null) {
                matches.addDefaultBinding(enclosing);
            }
            return matches;
        }
    }

    /**
     * expecting one arg that is either a string or a pointcut or a class
     */
    @Override
    public String verify() {
        String oneStringOrOnePointcutArg = oneStringOrOnePointcutArg();
        if (oneStringOrOnePointcutArg == null) {
            return super.verify();
        }
        return oneStringOrOnePointcutArg;
    }
}