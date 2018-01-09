/*
 * Copyright 2009-2017 the original author or authors.
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
package org.codehaus.groovy.eclipse.launchers;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.codehaus.groovy.activator.GroovyActivator;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.osgi.framework.Bundle;

public class GroovyHomeVariableResolver implements IDynamicVariableResolver {

    @Override
    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        Bundle activeGroovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration<URL> entries = activeGroovyBundle.findEntries("", "conf", false);
        if (entries.hasMoreElements()) {
            URL entry = entries.nextElement();
            try {
                String file = FileLocator.resolve(entry).getFile();
                if (file.endsWith("conf/")) {
                    file = file.substring(0, file.length() - "conf/".length());
                }
                return file;
            } catch (IOException e) {
                throw new CoreException(
                    new Status(IStatus.ERROR, GroovyActivator.PLUGIN_ID, "Problem finding active Groovy bundle", e));
            }
        }
        return "/";
    }
}
