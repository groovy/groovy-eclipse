/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.dsl.pointcuts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

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
import org.eclipse.core.resources.IStorage;

/**
 * Abstract implementation of the Pointcut.  Most concrete instances will only
 * accept one argument.
 */
public abstract class AbstractPointcut implements IPointcut {

    private IStorage containerIdentifier;

    private StringObjectVector elements = new StringObjectVector(1);

    private String pointcutName;

    private IProject project;

    public AbstractPointcut(IStorage containerIdentifier, String pointcutName) {
        this.containerIdentifier = containerIdentifier;
        this.pointcutName = pointcutName;
    }

    @Override
    public String getPointcutDebugName() {
        return pointcutName + " (" + getClass().getSimpleName() + ")";
    }

    @Override
    public String getPointcutName() {
        return pointcutName;
    }

    public void setPointcutName(String pointcutName) {
        this.pointcutName = pointcutName;
    }

    @Override
    public IStorage getContainerIdentifier() {
        return containerIdentifier;
    }

    public void setContainerIdentifier(IStorage containerIdentifier) {
        this.containerIdentifier = containerIdentifier;
    }

    @Override
    public final void addArgument(Object argument) {
        elements.add(null, argument);
    }

    @Override
    public final void addArgument(String name, Object argument) {
        if (name == null) {
            addArgument(argument);
            return;
        }
        elements.add(name, argument);
    }

    @Override
    public void verify() throws PointcutVerificationException {
        // most pointcuts can't have more than one argument
        if (elements.size > 1) {
            throw new PointcutVerificationException("Can't have more than one argument to this pointcut", this);
        }
    }

    /**
     * Attempt a match on the given object to match and return all the matches found
     * @param pattern pattern to match
     * @param toMatch object to match on
     * @return collection of objects matched, or null if no matches found
     */
    @Override
    public abstract Collection<?> matches(GroovyDSLDContext pattern, Object toMatch);

    /**
     * matches on the pointct passed in and also
     * binds the argument to the argument name if
     * a name exists.
     *
     * Must call if the argument to this pointcut is another pointcut
     */
    protected Collection<?> matchOnPointcutArgument(
            IPointcut argument, GroovyDSLDContext pattern, Collection<?> allElementsToMatch) {
        if (allElementsToMatch == null) {
            return null;
        }
        Collection<Object> outerResults = new LinkedHashSet<>();
        for (Object toMatch : allElementsToMatch) {
            Collection<?> innerResults = argument.matches(pattern, toMatch);
            if (innerResults != null) {
                String bindingName = getArgumentName(argument);
                if (bindingName != null) {
                    pattern.addToBinding(bindingName, innerResults);
                }
                outerResults.add(toMatch);
            }
        }
        // return null if no matches found
        return (!outerResults.isEmpty() ? outerResults : null);
    }

    /**
     * Variant of matchOnPointcutArgument here.  pass through the
     * bound results to the containing pointcut
     */
    protected Collection<?> matchOnPointcutArgumentReturnInner(
            IPointcut argument, GroovyDSLDContext pattern, Collection<?> allElementsToMatch) {
        String bindingName = getArgumentName(argument);
        Collection<Object> innerResults = new HashSet<>();
        for (Object toMatch : allElementsToMatch) {
            Collection<?> tempInnerResults = argument.matches(pattern, toMatch);
            if (tempInnerResults != null) {
                innerResults.addAll(tempInnerResults);
            }
        }
        if (bindingName != null && !innerResults.isEmpty()) {
            pattern.addToBinding(bindingName, innerResults);
        }
        // return null if no matches found
        return (!innerResults.isEmpty() ? innerResults : null);
    }

    /**
     * Flattens a map of collections into a single collection.
     */
    protected Collection<?> flatten(Map<Object, Collection<?>> pointcutResult) {
        Collection<Object> newCollection = new HashSet<>(pointcutResult.size());
        for (Collection<?> collection : pointcutResult.values()) {
            newCollection.addAll(collection);
        }
        return newCollection;
    }

    @Override
    public final Object getFirstArgument() {
        if (elements.size > 0) {
            return elements.elementAt(0);
        } else {
            return null;
        }
    }

    @Override
    public final String[] getArgumentNames() {
        return elements.getNames();
    }

    @Override
    public final Object[] getArgumentValues() {
        return elements.getElements();
    }

    /**
     * returns the associated name for the given argument.
     * null if this is not an argument and null if there
     * is no name
     * @param argument
     * @return
     */
    public final String getArgumentName(Object argument) {
        for (int i = 0; i < elements.size; i++) {
            if (elements.elementAt(i) == argument) {
                return elements.nameAt(i);
            }
        }
        return null;
    }

    @Override
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

    @Override
    public IPointcut normalize() {
        for (int i = 0, n = elements.size; i < n; i += 1) {
            Object elt = elements.elementAt(i);
            if (elt instanceof IPointcut) {
                elements.setElement(((IPointcut) elt).normalize(), i);
            }
        }
        return this;
    }

    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        for (Object elt : elements.getElements()) {
            if (elt instanceof IPointcut && !((IPointcut) elt).fastMatch(pattern)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setProject(IProject project) {
        this.project = project;
    }

    @Override
    public void accept(Closure contributionGroupClosure) {
        IContributionGroup group = new DSLContributionGroup(contributionGroupClosure);
        if (project != null) {
            try {
                // verify correctness of this pointcut
                verify();

                // project is set to null inside of normalize, so cache it here
                IProject p = project;

                // now perform some optimizations on it
                // potentially creates a copy of the original pointcut
                IPointcut normalized = normalize();

                if (GroovyLogManager.manager.hasLoggers()) {
                    GroovyLogManager.manager.log(TraceCategory.DSL, "Registering pointcut:\n" + this.toString());
                }
                // register this pointcut and group for the given project
                GroovyDSLCoreActivator.getDefault().getContextStoreManager().getDSLDStore(p).addContributionGroup(normalized, group);
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
     */
    protected final String allArgsArePointcuts() throws PointcutVerificationException {
        for (Object arg : elements.getElements()) {
            if (arg == null) {
                continue;
            }
            if (!(arg instanceof IPointcut)) {
                return "All arguments should be pointcuts";
            } else {
                ((IPointcut) arg).verify();
            }
        }
        return null;
    }

    /**
     * A standard verification that checks to see the number of args
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
            if (!(arg instanceof String)) {
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
            if (!(arg instanceof Class)) {
                return "All arguments should be classes";
            }
        }
        return null;
    }

    protected IPointcut and(IPointcut other) {
        AbstractPointcut andPointcut = new AndPointcut(containerIdentifier, "and");
        andPointcut.setProject(project);
        andPointcut.addArgument(this);
        andPointcut.addArgument(other);
        return andPointcut;
    }

    protected IPointcut or(IPointcut other) {
        AbstractPointcut orPointcut = new OrPointcut(containerIdentifier, "or");
        orPointcut.setProject(project);
        orPointcut.addArgument(this);
        orPointcut.addArgument(other);
        return orPointcut;
    }

    protected IPointcut bitwiseNegate() {
        AbstractPointcut notPointcut = new NotPointcut(containerIdentifier, "not");
        notPointcut.setProject(project);
        notPointcut.addArgument(this);
        return notPointcut;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(" + containerIdentifier + ")\n");
        formatedString(sb, 2);
        return sb.toString();
    }

    protected void formatedString(StringBuilder sb, int indent) {
        sb.append(getPointcutDebugName());
        elements.formattedString(sb, indent + 2);
        sb.append("\n");
    }

    static String spaces(int indent) {
        StringBuilder sb = new StringBuilder(indent + 2);
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    protected Map<String, Object> namedArgumentsAsMap() {
        return elements.asMap();
    }

    protected Collection<?> ensureCollection(Object toMatch) {
        if (toMatch == null) {
            return null;
        }
        return toMatch instanceof Collection ? (Collection<?>) toMatch : Collections.singleton(toMatch);
    }
}
