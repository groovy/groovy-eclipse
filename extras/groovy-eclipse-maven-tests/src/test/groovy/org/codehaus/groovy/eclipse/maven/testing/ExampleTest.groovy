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
package org.codehaus.groovy.eclipse.maven.testing

import groovy.util.GroovyTestCase

/**
 * Tests for the {@link Example} class.
 */
class ExampleTest
    extends GroovyTestCase
{
    void testShow() {
        new Example().show()
    }
}
