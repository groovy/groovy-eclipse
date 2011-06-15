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

import java.util.Collection;

import org.codehaus.groovy.eclipse.dsl.DSLDStore;
import org.codehaus.groovy.eclipse.dsl.contributions.IContributionGroup;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IStorage;


/**
 * A pointcut that can be defined in a DSLD file.  A pointcut describes
 * <i>where</i> a contribution group should be applied.
 * <p>
 * An {@link IPointcut} instance may contain state, but they must be immutable once created.
 * Pointcut evaluation may be occurring simultaneously on multiple threads.
 * <p>
 * Here is the order in which the methods are called:
 * <ol>
 * <li> any number of {@link #addArgument(Object)} and {@link #addArgument(String, Object)}.  
 *      but a pointcut must have only named or only unnamed arguments.  The two styles
 *      cannot be mixed
 * <li> {@link #verify()}.  If this method does not return an OK status, then the following methods are not called
 * <li> {@link #normalize()}  this method will reorganize this query and contained queries to make more efficient.
 *      after this method call, Poincuts are immutable
 * <li> {@link #matches(GroovyDSLDContext)}.  This method can be called any number of times and from different threads.
 *      It should not mutate state
 * <ol>
 * 
 * There are several broad categories of pointcuts, each with their own expectactions:
 * <ol>
 * <li>Filtering pointcuts, which require a surrounding pointcut to pass some state in (eg- find field/method)
 * <li>Structural pointcuts, which depend on the lexical structure of the code (eg- enclosingClass)
 * <li>Layout pointcuts, which depend on invariant factors external to the current module (eg- location in project, or file name)
 * <li>Semantic pointcuts, which depend on inferencing (eg- currentType)
 * </ol>
 * Each category offers a different way to internally optimize
 * 
 * 
 * @author andrew
 * @created Nov 17, 2010
 */
public interface IPointcut {

    /**
     * Detmrmines if this pointcut is applicable in the current context
     * @param pattern corresponding to the current location being inferred.
     * @param toMatch objet to match on (might be a {@link Collection}) 
     * @return a {@link BindingSet} corresponding to the items bound in this
     * pointcut evaluation, or else null if there was no match
     */
    Collection<?> matches(GroovyDSLDContext pattern, Object toMatch);
    
    /**
     * A unique identifier for the container that created this context.
     * @return a unique identifier
     */
    IStorage getContainerIdentifier();
    
    
    /**
     * Recursively rewrites this query so that it can run faster
     * @return a rewritten query that will likely run faster, or this object if no nomralization is applicable.
     */
    IPointcut normalize();
    
    /**
     * Adds an argument for this pointcut 
     * @param name the optional name for this argument or null if none
     * @param argument the argument (may be another pointcut, or a string parameter, etc)
     */
    void addArgument(String name, Object argument);
    
    
    /**
     * Adds an argument for this pointcut.  Variant of {@link #addArgument(String, Object)} that
     * does not take a name 
     * @param argument the argument (may be another pointcut, or a string parameter, etc)
     */
    void addArgument(Object argument);
    
    void verify() throws PointcutVerificationException;
    
    
    Object getFirstArgument();
    String getFirstArgumentName();
    Object[] getArgumentValues();
    String[] getArgumentNames();
    
    void setProject(IProject project);
    
    
    /**
     * Associates the {@link IContributionGroup} with this pointcut.
     * The pointcut and the contribution group are registered in the {@link DSLDStore}
     * @param contributionGroupClosure the closure that will be used by an {@link IContributionGroup}
     */
    void accept(@SuppressWarnings("rawtypes") Closure contributionGroupClosure);

    /**
     * Do a fast match on this pattern.  Return false, if there is no way that any pattern that is contained inside
     * of this one could ever match.  Return true if there is a chance.
     * 
     * For example if the {@link IPointcut} looks at file names or file paths and there this initial pattern does
     * not match, then there are no sub-patterns that can ever match.
     * 
     * @param pattern
     * @return
     */
    boolean fastMatch(GroovyDSLDContext pattern);
    
    /**
     * User-readable name of the pointcut
     * @return
     */
    String getPointcutName();
    
    /**
     * Extended name printed out during errors
     * @return
     */
    String getPointcutDebugName();
}
