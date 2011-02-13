/*******************************************************************************
 * Copyright (c) 2010 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg     - Initial API and implementation
 *******************************************************************************/
package org.codehaus.groovy.eclipse.maven.testing;


/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 7, 2010
 */
public class JavaClass {
    Example e;
    Helper h;
    // include an unused var here.
    // if nowarn is set in the pom, then there should no warning in the output 
    private int unused;
}
