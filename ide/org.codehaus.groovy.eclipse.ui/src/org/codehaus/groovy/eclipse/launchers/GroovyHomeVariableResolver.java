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

    public String resolveValue(IDynamicVariable variable, String argument) throws CoreException {
        Bundle activeGroovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration entries = activeGroovyBundle.findEntries("", "conf", false);
        if (entries.hasMoreElements()) {
            URL entry = (URL) entries.nextElement();

            try {
                String file = FileLocator.resolve(entry).getFile();
                if (file.endsWith("conf/")) {
                    file = file.substring(0, file.length() - "conf/".length());
                }
                return file;
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR, GroovyActivator.PLUGIN_ID,
                        "Problem finding active Groovy bundle", e));
            }
        }
        return "/";
    }

}
