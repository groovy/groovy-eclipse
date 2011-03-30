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

import org.codehaus.groovy.eclipse.GroovyLogManager;
import org.codehaus.groovy.eclipse.TraceCategory;
import org.codehaus.groovy.eclipse.dsl.GroovyDSLCoreActivator;
import org.codehaus.groovy.eclipse.dsl.contributions.DSLContributionGroup;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.AndPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.impl.NotPointcut;
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
    
    public void verify() throws PointcutVerificationException {
        // most pointcuts can't have more than one argument
        if (elements.size > 1) {
            throw new PointcutVerificationException("Can't have more than one argument to this pointcut", this);
        }
    }
    
    public final void addArgument(Object argument) {
        elements.add(null, argument);
    }
    
    public abstract BindingSet matches(GroovyDSLDContext pattern);
    
    public final void addArgument(String name, Object argument) {
        if (name == null) {
            addArgument(argument);
            return;
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
        for (int i = 0; i < elements.size; i++) {
            Object elt = elements.elementAt(i);
            if (elt instanceof IPointcut) {
                elements.setElement(((IPointcut) elt).normalize(), i);
            }
        }
        // project is only required for registering with contribution groups.
        // can set to null now
//        this.project = null;
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
    
    public void accept(@SuppressWarnings("rawtypes") Closure contributionGroupClosure) {
        IContributionGroup group = new DSLContributionGroup(contributionGroupClosure);
        if (project != null) {
            try {
                
                // verify correctness of this pointcut
                this.verify();
                
                // project is set to null inside of normalize, so cache it here
                IProject p = project;
                
                // now perform some optimizations on it
                // potentially creates a copy of the original pointcut
                IPointcut normalized = this.normalize();

                
                // register this pointcut and group for the given project
                GroovyDSLCoreActivator.getDefault().getContextStoreManager()
                    .getDSLDStore(p).addContributionGroup(normalized, group);
                
                
            } catch (PointcutVerificationException e) {
                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "Ignoring invalid pointcut");
                    GroovyLogManager.manager.log(TraceCategory.DSL, e.getPointcutMessage());
                    GroovyLogManager.manager.logException(TraceCategory.DSL, e);
                }
            }
        }
    }
    
    /**
     * A standard verification that checks to see that all args are pointcuts.
     * @return null if all args are pointcuts, error status otherwise
     * @throws PointcutVerificationException 
     */
    protected final String allArgsArePointcuts() throws PointcutVerificationException {
        for (Object arg : elements.getElements()) {
            if (arg == null) {
                continue;
            }
            if (! (arg instanceof IPointcut)) {
                return "All arguments should be pointcuts";
            } else {
                ((IPointcut) arg).verify();
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
    
    protected final String hasNoArgs() {
        if (elements.getElements().length == 0) {
            return null;
        } else {
            return "Expecting no arguments, but found " + elements.getElements().length + ".  Consider using '&' or '|' to connect arguments.";
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
    
    protected final String oneStringOrOnePointcutArg() throws PointcutVerificationException {
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
    
    protected final String oneStringOrOnePointcutOrOneClassArg() throws PointcutVerificationException {
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
        andPointcut.addArgument(this);
        andPointcut.addArgument(other);
        return andPointcut;
    }
    protected IPointcut or(IPointcut other) {
        AbstractPointcut orPointcut = new OrPointcut(containerIdentifier);
        orPointcut.setProject(project);
        orPointcut.addArgument(this);
        orPointcut.addArgument(other);
        return orPointcut;
    }

    protected IPointcut bitwiseNegate() {
        AbstractPointcut notPointcut = new NotPointcut(containerIdentifier);
        notPointcut.setProject(project);
        notPointcut.addArgument(this);
        return notPointcut;
    }

    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("IPointcut [\n  getClass()=");
        builder.append(getClass().getSimpleName());
        builder.append("\n  containerIdentifier=");
        builder.append(containerIdentifier);
        builder.append("\n  elements=");
        builder.append(elements);
        builder.append("\n]");
        return builder.toString();
    }
    
    
}
