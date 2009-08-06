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
package org.codehaus.groovy.eclipse.core.types;

import static org.codehaus.groovy.eclipse.core.types.GroovyDeclaration.Kind.*;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;


public class ClassType extends GroovyDeclaration {
	public ClassType(String signature, int modifiers, String name) {
		super(signature, modifiers, name);
	}

	public Kind getType() {
		return CLASS;
	}

    @Override
    public IJavaElement toJavaElement(GroovyProjectFacade project) {
        try {
            return project.getProject().findType(name, new NullProgressMonitor());
        } catch (JavaModelException e) {
            GroovyCore.logException("Error converting to java element", e);
            return null;
        }
    }
}