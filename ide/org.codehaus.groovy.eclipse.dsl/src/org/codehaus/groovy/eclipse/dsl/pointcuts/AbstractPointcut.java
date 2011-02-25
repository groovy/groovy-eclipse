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

import groovy.lang.Closure;

import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.ContributionGroup;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.OrPointcut;
import org.eclipse.core.resources.IProject;



/**
 * Abstract implementation of the Pointcut.  Most concrete instances will only 
 * accept one argument.  
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
    
    public abstract BindingSet matches(GroovyDSLDContext pattern);
    
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
    
    /**
     * matches on the pointct passed in and also
     * binds the argument to the argument name if 
     * a name exists.
     * @param argument
     * @param pattern
     * @return
     */
    protected BindingSet matchOnPointcutArgument(
            IPointcut argument, GroovyDSLDContext pattern) {
        BindingSet set = argument.matches(pattern);
        String bindName = getFirstArgumentName();
        if (set != null && bindName != null) {
            Object val = set.getDefaultBinding();
            set.addBinding(bindName, val);
        }
        return set;
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
    
    public final String getNameForArgument(Object arg) {
        return elements.nameOf(arg);
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
                    .getDSLDStore(project).addContributionGroup(this, group);
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
     * @return a string if number of args is not 1 else null
     */
    protected final String hasOneArg() {
        if (elements.getElements().length == 1) {
            return null;
        } else {
            return "Expecting 1 argument, but found " + elements.getElements().length + ".  Consider using '&' or '|' to connect arguments.";
        }
    }
    
    /**
     * A standard verification that checks to see the number of args
     * @arg num 
     * @return a string if number of args is not 1 or 0 else null
     */
    protected final String hasOneOrNoArgs() {
        if (elements.getElements().length <= 1) {
            return null;
        } else {
            return "Expecting 1 or no arguments, but found " + elements.getElements().length + ".  Consider using '&' or '|' to connect arguments.";
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
            return "This pointcut supports exactly one argument of type Pointcut or String.  Consider using '&' or '|' to connect arguments.";
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        return null;
    }
    
    protected final String oneStringOrOnePointcutOrOneClassArg() {
        String maybeStatus = allArgsAreStrings();
        String maybeStatus2 = allArgsArePointcuts(); 
        String maybeStatus3 = allArgsAreClasses(); 
        if (maybeStatus != null && maybeStatus2 != null && maybeStatus3 != null) {
            return "This pointcut supports exactly one argument of type Pointcut or String or Class.  Consider using '&' or '|' to connect arguments.";
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        return null;
    }
    
    protected final String allArgsAreClasses() {
        for (Object arg : elements.getElements()) {
            if (arg == null) {
                continue;
            }
            if (! (arg instanceof Class<?>)) {
                return "All arguments should be classes";
            }
        }
        return null;
    }

    
    protected IPointcut and(IPointcut other) {
        AbstractPointcut andPointcut = new AndPointcut(containerIdentifier);
        andPointcut.setProject(project);
        andPointcut.and(this);
        andPointcut.and(other);
        return andPointcut;
    }
    protected IPointcut or(IPointcut other) {
        AbstractPointcut orPointcut = new OrPointcut(containerIdentifier);
        orPointcut.setProject(project);
        orPointcut.or(this);
        orPointcut.or(other);
        return orPointcut;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AbstractPointcut [getClass()=");
        builder.append(getClass());
        builder.append(", containerIdentifier=");
        builder.append(containerIdentifier);
        builder.append(", elements=");
        builder.append(elements);
        builder.append("]");
        return builder.toString();
    }
    
    
}
