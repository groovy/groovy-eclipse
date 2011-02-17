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
 * The bind() pointcut takes a named argument where the argument is another pointcut.
 * @author andrew
 * @created Feb 10, 2011
 */
public class BindPointcut extends AbstractPointcut {
    
    
    public BindPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public BindingSet matches(GroovyDSLDContext pattern) {
        BindingSet set = ((IPointcut) getFirstArgument()).matches(pattern);
        if (set != null) {
            Object val = set.getBinding((String) null);
            set.addBinding(getFirstArgumentName(), val);
        }
        return set;
    }

    public IPointcut normalize() {
        ((IPointcut) getFirstArgument()).normalize();
        return super.normalize();
    }
    
    @Override
    public String verify() {
        String status = super.verify();
        if (status != null) {
            return status;
        } else {
            Object arg = getFirstArgument();
            if (arg instanceof IPointcut) {
                String name = getFirstArgumentName();
                if (name == null) {
                    return "bind requires a named argument";
                }                
                return ((IPointcut) arg).verify();
            } else {
                return "A pointcut is required as the single argument to bind";
            }
        }
    }

}
