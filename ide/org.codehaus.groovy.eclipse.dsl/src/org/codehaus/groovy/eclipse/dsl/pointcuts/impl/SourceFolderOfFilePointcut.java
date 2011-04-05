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
 * Tests that the file currently being checked is in the given source folder
 * The result is cached in the pattern providing a fail/succeed fast strategy.
 * 
 * Argument should be the workspace relative path to the source folder, using '/'
 * as a path separator.
 * @author andrew
 * @created Feb 10, 2011
 */
public class SourceFolderOfFilePointcut extends AbstractPointcut {

    public SourceFolderOfFilePointcut(String containerIdentifier, String pointcutName) {
        super(containerIdentifier, pointcutName);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        if (pattern.fullPathName != null && pattern.fullPathName.startsWith((String) getFirstArgument())) {
            return new BindingSet().addDefaultBinding(pattern.fullPathName);
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
