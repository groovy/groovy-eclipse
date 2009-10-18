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

package org.codehaus.groovy.eclipse.editor.actions;

import java.util.Set;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Andrew Eisenberg
 * @created Aug 26, 2009
 *
 */
public class RenameToGroovyAction extends RenameToGroovyOrJavaAction {
    public static String COMMAND_ID = "org.codehaus.groovy.eclipse.ui.convertToGroovy";
    public RenameToGroovyAction() {
        super(GROOVY);
    }
    
    protected void askToConvert(Set<IProject> affectedProjects, Shell shell) {
        if (affectedProjects.size() == 0) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        if (affectedProjects.size() > 1) {
            sb.append("Projects ");
            for (IProject project : affectedProjects) {
                sb.append(project.getName()).append(", ");
            }
            sb.replace(sb.length()-2, 2, " do ");
        } else {
            sb.append("Projects ").append(affectedProjects.iterator().next().getName()).append(" does ");
        }
        sb.append("have the Groovy nature.  Do you want to add it?");
        
        boolean yes = MessageDialog.openQuestion(shell, "Convert to Groovy?", sb.toString());
        if (yes) {
            for (IProject project : affectedProjects) {
                GroovyRuntime.addGroovyRuntime(project);
            }
        }
    }
}
