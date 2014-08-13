/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software Inc.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.smoke.test;

import junit.framework.Test;

/**
 * Tycho refuses to run tests directly from another bundle, so we will just have to
 * create a class here that runs them.
 */
public class SmokeTests {
    public static Test suite() throws Exception {
        return org.codehaus.groovy.alltests.SmokeTests.suite();
    }
}
