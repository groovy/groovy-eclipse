/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.core;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;

/**
 * Interface implemented by instances that are aware of Groovy projects. The single
 * {@link #setGroovyProject(GroovyProject)} method is called whenever the project changes. The method is not guaranteed
 * to be called at all, so implementers must check that it has in fact been set.
 * 
 * @author empovazan
 */
public interface IGroovyProjectAware {
	public void setGroovyProject(GroovyProjectFacade facade);
}
