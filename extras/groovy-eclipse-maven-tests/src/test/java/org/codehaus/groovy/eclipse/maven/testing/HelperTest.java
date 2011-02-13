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

import junit.framework.TestCase;

/**
 * Tests for the {@link Helper} class.
 */
public class HelperTest extends TestCase {
    public void testHelper() throws Exception {
        new Helper().help(new Example());
    }
}
