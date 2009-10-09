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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author Andrew Eisenberg
 * @created Oct 7, 2009
 *
 */
public abstract class AbstractGroovyLaunchShortcut  implements ILaunchShortcut {
    /**
     * Used for dialog presentation if the user needs to choose from
     * matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TITLE = "Select Groovy {0} to launch";
    
    /**
     * Used for dialog presentation if the user needs to choose from
     * matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TEXT = "Select the Groovy {0} to launch";

    /**
     * This is the string that will show if the groovy file the user is trying to run 
     * doesn't meet the criteria to be run.
     */
    public static final String GROOVY_FILE_NOT_RUNNABLE_MESSAGE = "Groovy {0} not found in current selection";

    
    private final String title;
    private final String text;
    private final String msg;
    
    public AbstractGroovyLaunchShortcut() {
        title = SELECT_CONFIG_DIALOG_TITLE.replace("{0}", applicationOrScript());
        text = SELECT_CONFIG_DIALOG_TEXT.replace("{0}", applicationOrScript());
        msg = GROOVY_FILE_NOT_RUNNABLE_MESSAGE.replace("{0}", applicationOrScript());
    }
    
    /**
     * Launches from the package explorer.
     * 
     * @see ILaunchShortcut#launch
     */
    public void launch(ISelection selection, String mode) {
        ICompilationUnit unit = extractCompilationUnit(selection);
    	if (unit != null) {
    		launchGroovy(unit, mode);
    	}
    }

    /**
     * @param selection
     * @return
     */
    private ICompilationUnit extractCompilationUnit(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection struct = (IStructuredSelection) selection;
            Object obj = struct.getFirstElement();
            ICompilationUnit unit;
            if (obj instanceof IAdaptable) {
                unit = (ICompilationUnit) ((IAdaptable) obj).getAdapter(ICompilationUnit.class);
                if (unit != null) {
                    return unit;
                }
                IFile file = (IFile) ((IAdaptable) obj).getAdapter(IFile.class);
                if (file != null) {
                    return JavaCore.createCompilationUnitFrom(file);
                }
            }
        }
        return null;
    }

    /**
     * Finds or creates a launch configuration for the given file then 
     * launches it.
     * 
     * @param file The file to launch.
     * @param mode The mode to launch in.
     */
    protected void launchGroovy(ICompilationUnit unit, String mode) {
        IType[] types = null;
        try {
            types = unit.getAllTypes();
        } catch (JavaModelException e) {
            GroovyCore.errorRunningGroovy(e);
            return;
        }
        IType runType = findClassToRun(types);
        if (runType == null) {
            GroovyCore.errorRunningGroovy(
                    new Exception(msg));
            return;     
        }
        IJavaProject javaProject = unit.getJavaProject();
        
        Map<String, String> launchConfigProperties = createLaunchProperties(runType);

        try {
            ILaunchConfigurationWorkingCopy workingConfig = findOrCreateLaunchConfig(launchConfigProperties, 
                    runType.getElementName());
            workingConfig.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, Arrays.asList( JavaRuntime.computeDefaultRuntimeClassPath(javaProject)) );
            ILaunchConfiguration config = workingConfig.doSave();
            DebugUITools.launch(config, mode);  
        } catch (CoreException e) {
            GroovyCore.errorRunningGroovyFile((IFile) unit.getResource(), e);
        } 

    }

    /**
     * @param runType
     * @return
     */
    protected abstract Map<String, String> createLaunchProperties(IType runType);
    
    /**
     * Launches from the source file.
     * 
     * @see ILaunchShortcut#launch
     */
    public void launch(IEditorPart editor, String mode) {
    	// make sure we are saved as we run groovy from the file
    	editor.getEditorSite().getPage().saveEditor(editor,false);
    	IEditorInput input = editor.getEditorInput();
    	IFile file = (IFile) input.getAdapter(IFile.class);
    	ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
    	launchGroovy(unit, mode);
    }

    /**
     * Finds the runnable classnode in an array.  If more than one possible node is found, 
     * will prompt the user to select one.  
     * 
     * @param classNodes
     * @return Returns a classnode if found, or null if no classNode can be run.
     * @throws OperationCanceledException If the user selects cancel
     */
    protected abstract IType findClassToRun(IType[] types);

    /**
     * This method will find a Launch configration that matches the passed
     * properties of if one is not found will create one.
     * 
     * @param configProperties A <String, String> Map of launch configuration
     * properties.
     * @param classUnderTest The name of the class (without package) that is
     * being tested.
     * @return Returns a launch configuration for the class under test with
     * the passed properties.
     * @throws CoreException
     */
    public ILaunchConfigurationWorkingCopy findOrCreateLaunchConfig(
            Map<String, String> configProperties, String classUnderTest)
            throws CoreException {
        ILaunchConfigurationWorkingCopy returnConfig;
        ILaunchConfiguration config = findConfiguration(configProperties);
        if (config == null) {
            returnConfig = createLaunchConfig(configProperties, classUnderTest);
        } else {
            returnConfig = config.getWorkingCopy();
        }
        return returnConfig;
    }

    /**
     * This method creates a new launch configuration working copy for the 
     * classUnderTest with the properties defined in configProperites.
     * 
     * @param configProperties A <String, String> Map of launch configuration
     * properties.
     * @param classUnderTest The name of the class (without package) that is
     * being tested.
     * @return Returns a new launch configuration.
     * @throws CoreException
     */
    public ILaunchConfigurationWorkingCopy createLaunchConfig(Map<String, String> configProperties, String classUnderTest)
            throws CoreException {
            	String launchName = getLaunchManager().generateUniqueLaunchConfigurationNameFrom( classUnderTest ) ;
            	ILaunchConfigurationWorkingCopy returnConfig = 
            		getGroovyLaunchConfigType().newInstance(null, launchName);
            
            	for (Iterator< String > it = configProperties.keySet().iterator(); it.hasNext();) {
            		String key = it.next();
            		String value = configProperties.get(key);
            		returnConfig.setAttribute(key, value);
            	}
            
            	return returnConfig; 
            }

    /**
     * This class finds any launch configrations that match the defined
     * properties.  If more that one match is found the user is prompted
     * to select one.
     * 
     * @param configProperties A <String, String> Map of properties to check
     * when searching for a matching launch configuration.
     * @return Returns a launch configuration that matches the given properties
     * if a match is found, otherwise returns null.
     * @throws CoreException
     */
    public ILaunchConfiguration findConfiguration(
            Map<String, String> configProperties) throws CoreException {
        ILaunchConfiguration returnValue = null;
        ILaunchConfigurationType configType = getGroovyLaunchConfigType();
        List<ILaunchConfiguration> candidateConfigs = ListUtil.newEmptyList();

        ILaunchConfiguration[] configs = getLaunchManager()
                .getLaunchConfigurations(configType);
        for (int i = 0; i < configs.length; i++) {
            ILaunchConfiguration config = configs[i];

            boolean matches = true;
            for (Iterator<String> it = configProperties.keySet().iterator(); it
                    .hasNext()
                    && matches;) {
                String key = it.next();
                String value = configProperties.get(key);
                if (!config.getAttribute(key, "").equals(value)) {
                    matches = false;
                }
            }
            if (matches) {
                candidateConfigs.add(config);
            }
        }
    
    	int candidateCount = candidateConfigs.size();
    	if (candidateCount == 1) {
    		returnValue = candidateConfigs.get(0);
    	} else if(candidateCount > 1) {
    		returnValue = chooseConfiguration(candidateConfigs);
    	}
    	return returnValue;
    }

    /**
     * Prompts the user to select a launch configuration from configList.
     * 
     * @param configList A List of ILaunchConfigrations for the user to 
     * pick from.
     * @return Returns the ILaunchConfiguration that the user selected.
     */
    public ILaunchConfiguration chooseConfiguration(List< ILaunchConfiguration > configList) {
    	IDebugModelPresentation labelProvider= DebugUITools.newDebugModelPresentation();
    	return (ILaunchConfiguration) LaunchShortcutHelper.chooseFromList(configList, labelProvider, title, text);
    }

    /**
     * This is a convenience method for getting the Groovy launch configuration
     * type from the Launch Manager.
     * 
     * @return Returns the ILaunchConfigurationType for running Groovy classes.
     */
    protected abstract ILaunchConfigurationType getGroovyLaunchConfigType();

    /**
     * This is a convenince method for getting the Launch Manager from 
     * the Debug plugin.
     * 
     * @return Returns the default Eclipse launch manager.
     */
    public static ILaunchManager getLaunchManager() {
    	return DebugPlugin.getDefault().getLaunchManager() ;
    }

    
    protected abstract String applicationOrScript();
}
