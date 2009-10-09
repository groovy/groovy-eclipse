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

package org.codehaus.groovy.eclipse.launchers;

import java.util.List;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Andrew Eisenberg
 * @created Oct 7, 2009
 *
 */
public class GroovyScriptLauncherTab extends AbstractGroovyLauncherTab
        implements ILaunchConfigurationTab {

    @Override
    protected List<IType> findAllRunnableTypes(IJavaProject javaProject)
            throws JavaModelException {
        return new GroovyProjectFacade(javaProject).findAllScripts();
    }

}
