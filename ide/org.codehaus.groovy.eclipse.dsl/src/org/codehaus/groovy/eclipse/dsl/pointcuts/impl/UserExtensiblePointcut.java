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

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;

/**
 * 
 * @author andrew
 * @created Feb 11, 2011
 */
public class UserExtensiblePointcut extends AbstractPointcut {
    private Closure closure;

    public UserExtensiblePointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    public UserExtensiblePointcut(String containerIdentifier, Closure c) {
        super(containerIdentifier);
        this.closure = c;
    }

    public void setClosure(Closure closure) {
        this.closure = closure;
    }
    
    public final BindingSet matches(GroovyDSLDContext pattern) {
        try {
            closure.setDelegate(pattern);
            closure.setResolveStrategy(Closure.DELEGATE_FIRST);
            Object result = closure.call();
            if (result == null) {
                return null;
            } else if (result instanceof BindingSet) {
                return (BindingSet) result;
            } else {
                return new BindingSet(result);
            }
        } catch (Exception e) {
            GroovyDSLCoreActivator.logException(e);
            return null;
        }
    }
}
