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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.ContributionGroup;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Status;



/**
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public abstract class AbstractPointcut implements IPointcut {

    private String containerIdentifier;
    
    private StringObjectVector elements = new StringObjectVector(1);
    
    private boolean invalidArguments = false;
    
    private IProject project;
    
    public AbstractPointcut(String containerIdentifier) {
        this.containerIdentifier = containerIdentifier;
    }

    public String getContainerIdentifier() {
        return containerIdentifier;
    }
    
    public void setContainerIdentifier(String containerIdentifier) {
        this.containerIdentifier = containerIdentifier;
    }
    
    public String verify() {
        return invalidArguments ? "Cannot mix named and unnamed arguments" : null;
    }
    
    public final void addArgument(Object argument) {
        // Cannot mix named and unnamed args
        if (elements.size > 0 && !elements.containsName(null)) {
            invalidArguments = true;
        }
        elements.add(null, argument);
    }
    
    
    
    public final void addArgument(String name, Object argument) {
        if (name == null) {
            addArgument(argument);
            return;
        }
        // Cannot mix named and unnamed args
        if (elements.size >= 1 && elements.containsName(null)) {
            invalidArguments = true;
        }
        elements.add(name, argument);
    }
    
    public final Object getFirstArgument() {
        if (elements.size > 0) {
            return elements.elementAt(0);
        } else {
            return null;
        }
    }
    
    public final String[] getArgumentNames() {
        return elements.getNames();
    }
    
    public final Object[] getArgumentValues() {
        return elements.getElements();
    }
    
    public final String getFirstArgumentName() {
        if (elements.size > 0) {
            return elements.nameAt(0);
        } else {
            return null;
        }
    }
    
    public final Object getArgument(String name) {
        return elements.find(name);
    }
    
    public IPointcut normalize() {
        for (Object elt : elements.getElements()) {
            if (elt instanceof IPointcut) {
                ((IPointcut) elt).normalize();
            }
        }
        // project is only required for registering with contribution groups.
        // can set to null now
        this.project = null;
        return this;
    }
    
    public boolean fastMatch(GroovyDSLDContext pattern) {
        for (Object elt : elements.getElements()) {
            if (elt instanceof IPointcut && ! ((IPointcut) elt).fastMatch(pattern)) {
                return false;
            }
        }
        return true;
    }
    
    public void setProject(IProject project) {
        this.project = project;
    }
    
    public void accept(Closure contributionGroupClosure) {
        IContributionGroup group = new ContributionGroup(contributionGroupClosure);
        if (project != null) {
            // register this pointcut and group for the given project
            GroovyDSLCoreActivator.getDefault().getContextStoreManager()
                    .getDSLDStore(project).addContribution(this, group);
        }
    }
    
    /**
     * A standard verification that checks to see that all args are pointcuts.
     * @return null if all args are pointcuts, error status otherwise
     */
    protected final String allArgsArePointcuts() {
        for (Object arg : elements.getElements()) {
            if (arg == null) {
                continue;
            }
            if (! (arg instanceof IPointcut)) {
                return "All arguments should be pointcuts";
            } else {
                String res = ((IPointcut) arg).verify();
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }
    
    /**
     * A standard verification that checks to see the number of args
     * @arg num 
     * @return null if number of args matches num, error message otherwise
     */
    protected final String matchesArgNumber(int num) {
        Object[] elements2 = elements.getElements();
        if (elements2.length == num) {
            return null;
        } else {
            return "Expecting " + num + " arguments, but found " + elements2.length;
        }
    }
    
    /**
     * A standard verification that checks to see the number of args
     * @arg num 
     * @return {@link Status#OK_STATUS} if number of args matches num
     */
    protected final String hasOneArg() {
        if (elements.getElements().length == 1) {
            return null;
        } else {
            return "Expecting 1 argument, but found " + elements.getElements().length;
        }
    }
    
    protected final String allArgsAreStrings() {
        for (Object arg : elements.getElements()) {
            if (arg == null) {
                continue;
            }
            if (! (arg instanceof String)) {
                return "All arguments should be strings";
            }
        }
        return null;
    }
    
    protected final String oneStringOrOnePointcutArg() {
        String maybeStatus = allArgsAreStrings();
        String maybeStatus2 = allArgsArePointcuts(); 
        if (maybeStatus != null && maybeStatus2 != null) {
            return "This pointcut supports exactly one argument of type Pointcut or String";
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        return null;
    }
    
    protected IPointcut and(IPointcut other) {
        AbstractPointcut andPointcut = new AndPointcut(containerIdentifier);
        andPointcut.and(this);
        andPointcut.and(other);
        return andPointcut;
    }
    protected IPointcut or(IPointcut other) {
        AbstractPointcut orPointcut = new OrPointcut(containerIdentifier);
        orPointcut.or(this);
        orPointcut.or(other);
        return orPointcut;
    }
}
