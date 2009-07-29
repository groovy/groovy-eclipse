/*******************************************************************************
 * Copyright (c) 2007, 2009 Codehaus.org, SpringSource, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Unattributed        - Initial API and implementation
 *     Andrew Eisenberg - modified for Groovy Eclipse 2.0
 *******************************************************************************/
package org.codehaus.groovy.eclipse.launchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
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
 * This class is reponsible for creating a launching Groovy files.  If an 
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 * 
 * @see ILaunchShortcut
 */
public class GroovyLaunchShortcut implements ILaunchShortcut {

	/**
	 * The ID of this groovy launch configuration
	 */
	public static final String GROOVY_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyLaunchConfiguration" ; 
	
	/**
	 * Used for dialog presentation if the used needs to choose from
	 * matching Launch configurations
	 */
	public static final String SELECT_CONFIG_DIALOG_TITLE = "Select Groovy Launch" ;
	
	/**
	 * Used for dialog presentation if the used needs to choose from
	 * matching Launch configurations
	 */
	public static final String SELECT_CONFIG_DIALOG_TEXT = "Please select the Groovy configuration to Launch" ;

	/**
	 * This is the string that will show if the groovy file the user is trying to run 
	 * doesn't meet the criteria to be run.
	 */
	public static final String GROOVY_FILE_NOT_RUNNABLE_MESSAGE = "This script or class could not be run.\nIt should either:\n - have a main method,\n - or be a class extending GroovyTestCase" ;
	
	/**
	 * Launches from the package explorer.
	 * 
	 * @see ILaunchShortcut#launch
	 */
	public void launch(ISelection selection, String mode)  {
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).getFirstElement() instanceof ICompilationUnit) {
			IStructuredSelection structredSelection = (IStructuredSelection) selection;
			ICompilationUnit unit = (ICompilationUnit) structredSelection.getFirstElement(); 
			launchGroovy(unit, mode);
		}
	}

	/**
	 * Launches from the source file.
	 * 
	 * @see ILaunchShortcut#launch
	 */
	public void launch(IEditorPart editor, String mode)  {
		// make sure we are saved as we run groovy from the file
		editor.getEditorSite().getPage().saveEditor(editor,false);
		IEditorInput input = editor.getEditorInput();
		IFile file = (IFile) input.getAdapter(IFile.class);
		ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
		launchGroovy(unit, mode);
	}

	/**
	 * Finds or creates a launch configuration for the given file then 
	 * launches it.
	 * 
	 * @param file The file to launch.
	 * @param mode The mode to launch in.
	 */
	public void launchGroovy(ICompilationUnit unit, String mode) {
	    IType[] types = null;
        try {
            types = unit.getAllTypes();
        } catch (JavaModelException e) {
            GroovyCore.errorRunningGroovy(e);
            return;
        }
		IType runType = findClassNode(types);
		if (runType == null) {
			GroovyCore.errorRunningGroovy(
			        new Exception(GROOVY_FILE_NOT_RUNNABLE_MESSAGE));
			return;		
		}
		
		Map<String, String> launchConfigProperties = new HashMap<String, String>();

		//TODO Need to add functionality to support Runnable and other groovy options that are not standard java runnable files 
		String className = "" ;
		if( GroovyProjectFacade.isTestCaseClass(runType)) {
			className = "junit.textui.TestRunner";
			launchConfigProperties.put(
			        IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, 
			        runType.getFullyQualifiedName());
		} else {
			className = runType.getFullyQualifiedName();
		}

		IJavaProject javaProject = unit.getJavaProject();
		
		launchConfigProperties.put(
		        IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, 
		        className);
		launchConfigProperties.put(
		        IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
		        javaProject.getElementName());

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
	 * Finds the runnable classnode in an array.  If more than one possible node is found, 
	 * will prompt the user to select one.  
	 * 
	 * @param classNodes
	 * @return Returns a classnode if found, or null if no classNode can be run.
	 * @throws OperationCanceledException If the user selects cancel
	 */
	public IType findClassNode(IType[] types) {
	    IType returnValue = null ; 
		List<IType> candidates = new ArrayList<IType>();
		
		for (int i = 0; i < types.length; i++) {
			if (GroovyProjectFacade.hasRunnableMain(types[i]) || 
				GroovyProjectFacade.isTestCaseClass(types[i]) ) {
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
	public ILaunchConfigurationWorkingCopy findOrCreateLaunchConfig(Map< String, String > configProperties, String classUnderTest) throws CoreException {
		ILaunchConfigurationWorkingCopy returnConfig ;
		ILaunchConfiguration config = findConfiguration(configProperties) ; 
		if(config == null) {
			returnConfig = createLaunchConfig(configProperties, classUnderTest);
		} else {
			returnConfig = config.getWorkingCopy() ; 
		}
		return returnConfig ; 
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
	public ILaunchConfigurationWorkingCopy createLaunchConfig(Map< String, String > configProperties, String classUnderTest) throws CoreException {
		String launchName = getLaunchManager().generateUniqueLaunchConfigurationNameFrom( classUnderTest ) ;
		ILaunchConfigurationWorkingCopy returnConfig = 
			getGroovyLaunchConfigType().newInstance(null, launchName ) ;

		for (Iterator< String > it = configProperties.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = configProperties.get(key);
			returnConfig.setAttribute(key, value);
		}

		return returnConfig ; 
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
	public ILaunchConfiguration findConfiguration(Map< String, String > configProperties) throws CoreException {
		ILaunchConfiguration returnValue = null ; 
		ILaunchConfigurationType configType = getGroovyLaunchConfigType();
		List< ILaunchConfiguration > candidateConfigs = ListUtil.newEmptyList();

		ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
		for (int i= 0; i < configs.length; i++) {
			ILaunchConfiguration config = configs[i];

			boolean matches = true ; 
			for (Iterator< String > it = configProperties.keySet().iterator(); it.hasNext() && matches ; ) {
				String key = it.next();
				String value = configProperties.get(key);
				if(!config.getAttribute(key, "").equals(value)) {
					matches = false ; 
				}
			}
			if (matches) {
				candidateConfigs.add(config);
			}
		}

		int candidateCount = candidateConfigs.size();
		if (candidateCount == 1) {
			returnValue = candidateConfigs.get(0);
		} else if( candidateCount > 1 ) {
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
		return (ILaunchConfiguration) LaunchShortcutHelper.chooseFromList(configList, labelProvider, SELECT_CONFIG_DIALOG_TITLE, SELECT_CONFIG_DIALOG_TEXT);
	}
	
	/**
	 * This is a convenience method for getting the Groovy launch configuration
	 * type from the Launch Manager.
	 * 
	 * @return Returns the ILaunchConfigurationType for running Groovy classes.
	 */
	public static ILaunchConfigurationType getGroovyLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(GROOVY_LAUNCH_CONFIG_ID) ;
	}

	/**
	 * This is a convenince method for getting the Launch Manager from 
	 * the Debug plugin.
	 * 
	 * @return Returns the default Eclipse launch manager.
	 */
	public static ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager() ;
	}

}
