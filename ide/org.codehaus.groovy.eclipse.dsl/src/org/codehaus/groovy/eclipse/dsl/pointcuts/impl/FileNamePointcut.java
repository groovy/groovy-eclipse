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
package org.codehaus.groovy.eclipse.dsl.pointcuts.impl;

import org.codehaus.groovy.eclipse.dsl.pointcuts.AbstractPointcut;
import org.codehaus.groovy.eclipse.dsl.pointcuts.BindingSet;
import org.codehaus.groovy.eclipse.dsl.pointcuts.GroovyDSLDContext;
import org.codehaus.groovy.eclipse.dsl.pointcuts.PointcutVerificationException;

/**
 * Tests that the current file matches the name passed in.
 * 
 * Argument should be the full file name including extendion, but not the path.
 * 
 * @author andrew
 * @created Apr 5, 2011
 */
public class FileNamePointcut extends AbstractPointcut {

    public FileNamePointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        if (pattern.simpleFileName != null && pattern.simpleFileName.equals(getFirstArgument())) {
            return new BindingSet().addDefaultBinding(pattern.simpleFileName);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern) != null;
    }

    @Override
    public void verify() throws PointcutVerificationException {
        String maybeStatus = allArgsAreStrings();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            throw new PointcutVerificationException(maybeStatus, this);
        }
        super.verify();
    }
}
