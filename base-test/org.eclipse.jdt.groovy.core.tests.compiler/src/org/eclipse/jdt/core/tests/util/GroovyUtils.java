/*******************************************************************************
 * Copyright (c) 2011 Codehaus, SpingSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.util;

import java.net.URL;

import org.eclipse.core.runtime.Platform;

/**
 * 
 * @author Andrew Eisenberg
 * @created Mar 17, 2011
 */
public class GroovyUtils {
    static public final int GROOVY_LEVEL;
    static {
        int groovyLevel = 18;
        URL groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.8.2.jar");
        if (groovyJar==null) {
            groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.7.10.jar");
            groovyLevel=17;
            if (groovyJar==null) {
                groovyJar = Platform.getBundle("org.codehaus.groovy").getEntry("lib/groovy-1.6.7.jar");
                groovyLevel=16;
            }
        }
        GROOVY_LEVEL = groovyLevel;
    }
    public static boolean isGroovy16() {
        return GROOVY_LEVEL == 16;
    }
    public static boolean isGroovy17() {
        return GROOVY_LEVEL == 17;
    }
    public static boolean isGroovy18() {
        return GROOVY_LEVEL == 18;
    }

}
