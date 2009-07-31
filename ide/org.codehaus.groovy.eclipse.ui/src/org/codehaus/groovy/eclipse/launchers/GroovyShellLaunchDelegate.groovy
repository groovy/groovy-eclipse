/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/

package org.codehaus.groovy.eclipse.launchers;

import static org.eclipse.core.runtime.FileLocator.resolve;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.osgi.framework.Bundle;

/**
 * @author Andrew Eisenberg
 * @created Jul 31, 2009
 *
 */
public class GroovyShellLaunchDelegate extends JavaLaunchDelegate {

    @Override
    public String[] getClasspath(ILaunchConfiguration configuration)
            throws CoreException {
        
        String[] classpath = super.getClasspath(configuration)
        def newClasspath = ListUtil.array(classpath)
        newClasspath.add(getPathTo("jline-*.jar"));
        newClasspath.add(getPathTo("antlr-*.jar"));
        newClasspath.add(getPathTo("commons-cli-*.jar"));
        
        return newClasspath.toArray(new String[0])
    }
    
    @SuppressWarnings("unchecked")
    static String getPathTo(String jarName) throws CoreException, IOException {
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy")
        Enumeration<URL> enu = groovyBundle.findEntries("", jarName, false)
        if (enu.hasMoreElements()) {
            URL jar = resolve(enu.nextElement())
            return jar.getFile()
        } else {
            throw new CoreException(new Status(Status.ERROR, GroovyPlugin.PLUGIN_ID, "Could not find $jarName on the class path.  Please add it manually"))
        }
    }

}