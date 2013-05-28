 /*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.core.launchers
;

import static org.eclipse.core.runtime.FileLocator.resolve;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.GroovyCoreActivator;
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

    public static final String JLINE_JAR = "jline-*.jar";


    @Override
    public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException {

        String[] classpath = super.getClasspath(configuration);
        List<String> newClasspath = ListUtil.array(classpath);
        try {
            newClasspath.add(getPathTo("jline-*.jar"));
        } catch (IOException e) {
            GroovyCore.logException("Could not fine path to jline jars", e);
        }

        return newClasspath.toArray(new String[0]);
    }

    private static String getPathTo(String jarName) throws CoreException, IOException {
        Bundle groovyBundle = Platform.getBundle("org.codehaus.groovy");
        Enumeration<URL> enu = groovyBundle.findEntries("lib", jarName, false);
        if (enu != null && enu.hasMoreElements()) {
            URL jar = resolve(enu.nextElement());
            return jar.getFile();
        } else {
            throw new CoreException(new Status(Status.ERROR, GroovyCoreActivator.PLUGIN_ID, "Could not find $jarName on the class path.  Please add it manually"));
        }
    }


    public static List<String> getExtraClasspathElements() throws CoreException, IOException {
        return Collections.singletonList(GroovyShellLaunchDelegate.getPathTo(GroovyShellLaunchDelegate.JLINE_JAR));
    }
}