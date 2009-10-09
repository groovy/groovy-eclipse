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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;

/**
 * This class is reponsible for creating a launching Groovy files.  If an 
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 * 
 * @see ILaunchShortcut
 */
public class GroovyApplicationLaunchShortcut extends AbstractGroovyLaunchShortcut {

	public static final String GROOVY_APP_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyApplicationLaunchConfiguration"; 
	
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_APP_LAUNCH_CONFIG_ID);
    }
    
    public IType findClassToRun(IType[] types) {
        IType returnValue = null ; 
        List<IType> candidates = new ArrayList<IType>();
        
        for (int i = 0; i < types.length; i++) {
            if (GroovyProjectFacade.hasRunnableMain(types[i])) {
                candidates.add(types[i]);
            }
        }
        
        if( candidates.size() == 1 ) {
            returnValue = candidates.get(0);
        } else {
            returnValue = LaunchShortcutHelper.chooseClassNode(candidates);
        }
        
        return returnValue;
    }
    
    /**
     * @param runType
     * @return
     */
    @Override
    protected Map<String, String> createLaunchProperties(IType runType) {
        Map<String, String> launchConfigProperties = new HashMap<String, String>();
        String className = runType.getFullyQualifiedName();
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, 
                className);
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
                runType.getJavaProject().getElementName());
        return launchConfigProperties;
    }
    
    @Override
    protected String applicationOrScript() {
        return "application";
    }

}