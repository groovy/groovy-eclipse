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

/**
 * Tests that the file currently being checked is in the given source folder
 * The result is cached in the pattern providing a fail/succeed fast strategy.
 * 
 * Argument should be the workspace relative path to the source folder, using '/'
 * as a path separator.
 * @author andrew
 * @created Feb 10, 2011
 */
public class SourceFolderPointcut extends AbstractPointcut {

    public SourceFolderPointcut(String containerIdentifier) {
        super(containerIdentifier);
    }

    @Override
    public BindingSet matches(GroovyDSLDContext pattern) {
        if (pattern.fileName != null && pattern.fileName.startsWith((String) getFirstArgument())) {
            return new BindingSet().addDefaultBinding(pattern.fileName);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean fastMatch(GroovyDSLDContext pattern) {
        return matches(pattern) != null;
    }

    @Override
    public String verify() {
        String maybeStatus = allArgsAreStrings();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        maybeStatus = hasOneArg();
        if (maybeStatus != null) {
            return maybeStatus;
        }
        return super.verify();
    }
}
