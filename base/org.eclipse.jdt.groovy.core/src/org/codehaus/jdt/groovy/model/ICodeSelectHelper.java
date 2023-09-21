/*
 * Copyright 2009-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.jdt.groovy.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;

/**
 * Performs code select in a Groovy-aware way.
 */
@FunctionalInterface
public interface ICodeSelectHelper {
    IJavaElement[] select(GroovyCompilationUnit unit, int start, int length);
}

class CodeSelectHelperFactory {
    // Inject the code select helper
    private static final String CODE_SELECT_HELPER_EXTENSION = "org.eclipse.jdt.groovy.core.codeSelectHelper";

    static ICodeSelectHelper selectHelper;
    static {

        // Exactly one code select helper is allowed. Do a check for this.
        IExtensionPoint extPoint = Platform.getExtensionRegistry().getExtensionPoint(CODE_SELECT_HELPER_EXTENSION);
        IExtension[] exts = extPoint.getExtensions();
        if (exts.length < 1) {
            throw new IllegalArgumentException("No Code Select Helper found");
        } else if (exts.length > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("Too many Code Select Helpers found:\n");
            for (IExtension ext : exts) {
                sb.append("    " + ext.getNamespaceIdentifier() + "." + ext.getSimpleIdentifier());
            }
            throw new IllegalArgumentException(sb.toString());
        }
        IConfigurationElement[] elts = exts[0].getConfigurationElements();
        if (elts.length < 1) {
            throw new IllegalArgumentException("No Code Select Helper found");
        } else if (elts.length > 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("Too many Code Select Helpers found:\n");
            for (IConfigurationElement elt : elts) {
                sb.append("    " + elt.getNamespaceIdentifier());
            }
            throw new IllegalArgumentException(sb.toString());
        }

        // all good. Now, instantiate and assign the code select helper
        try {
            selectHelper = (ICodeSelectHelper) elts[0].createExecutableExtension("class");
        } catch (CoreException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static void setSelectHelper(ICodeSelectHelper newSelectHelper) {
        selectHelper = newSelectHelper;
    }
}
