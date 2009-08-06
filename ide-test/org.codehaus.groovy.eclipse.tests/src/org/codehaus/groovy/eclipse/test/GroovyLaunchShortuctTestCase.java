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
package org.codehaus.groovy.eclipse.test;


import java.util.HashMap;
import java.util.Map;
import org.codehaus.groovy.eclipse.launchers.GroovyLaunchShortcut;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;


/**
 * This class is to test the GroovyLaunchShortcut class.
 * 
 * @author David Kerber
 */
public class GroovyLaunchShortuctTestCase extends EclipseTestCase {

	private final String CLASS_NAME = "GroovyTest" ;
	private final String FULL_CLASS_NAME = "org.codehaus.groovy.GroovyTest" ;
	private final String PROJECT_NAME = "GroovyProject" ;
	private final String ARGUMENTS = "arg0" ;
	private GroovyLaunchShortcut launchShortcut ; 
	
	/**
	 * @see org.codehaus.groovy.eclipse.test.EclipseTestCase#setUp()
	 */
	@Override
    public void setUp() throws Exception {
		super.setUp() ; 
		launchShortcut = new GroovyLaunchShortcut();
	}
	
	@Override
    public void tearDown() throws Exception {
		ILaunchConfigurationType configType = GroovyLaunchShortcut.getGroovyLaunchConfigType();
		ILaunchConfiguration[] configs = GroovyLaunchShortcut.getLaunchManager().getLaunchConfigurations(configType);
		for (int i = 0; i < configs.length; i++) {
			configs[i].delete() ; 
		}
		super.tearDown() ;
	}
	
	/**
	 * Test
	 * 
	 * @throws Exception 
	 */
	public void testCreateLaunchConfig() throws Exception{
		
		Map configProperties = getConfigProperties();
		
		ILaunchConfigurationWorkingCopy config = launchShortcut.createLaunchConfig(configProperties, CLASS_NAME);
		
		assertEquals("the configuration was not named correctly", 
				CLASS_NAME, config.getName());
		assertEquals("the configration main type was not set correctly",
				FULL_CLASS_NAME, config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "") ) ;
		assertEquals("the configuration project was not set correctly",
				PROJECT_NAME, config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, ""));
		assertEquals("the configuration arguments were not set correctly", 
				ARGUMENTS, config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")) ;
	}
	/**
	 * This is just to show that yes, this method will throw a 
	 * NullPointerException if you pass null into it.
	 * 
	 * @throws Exception 
	 */
	public void testCreateLaunchConfigNulls() throws Exception {
		
		try {
			launchShortcut.createLaunchConfig(null, null);	
			fail();
		} catch (NullPointerException npe) {
		}
	}
	
	/**
	 * Test
	 * 
	 * @throws Exception 
	 */
	public void testFindConfiguration() throws Exception {
		createLaunchConfigurations() ;
		final String matchingLaunchName = "LaunchA" ;
		createConfig(matchingLaunchName, FULL_CLASS_NAME, PROJECT_NAME, ARGUMENTS);
		ILaunchConfiguration config = launchShortcut.findConfiguration(getConfigProperties());
		assertEquals("findConfiguration didn't return the right launch configuration",
				matchingLaunchName, config.getName());
	}
	
	/**
	 * Test
	 * 
	 * @throws Exception 
	 */
	public void testFindConfigurationNotFound() throws Exception {
		createLaunchConfigurations() ;
		assertNull("no match should have been found", launchShortcut.findConfiguration(getConfigProperties()));
	}
	
	/**
	 * Creates and saves Launch Configurations used by test cases.
	 * 
	 * @throws Exception
	 */
	private void createLaunchConfigurations() throws Exception {
		createConfig("Launch1", FULL_CLASS_NAME, PROJECT_NAME, "fooArg");
		createConfig("Launch2", FULL_CLASS_NAME, "UnGroovyProject", ARGUMENTS);
		createConfig("Launch3", "org.eclipse.NotMyTest", PROJECT_NAME, ARGUMENTS);
	}
	private void createConfig(String name, String mainType, String projectName, String arguments) throws Exception {
		ILaunchConfigurationWorkingCopy config = 
			GroovyLaunchShortcut.getGroovyLaunchConfigType().newInstance(null, name ) ;
		config.setAttribute(projectName, arguments) ;
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainType );
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName );
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, arguments ) ;
		config.doSave() ; 
		
	}
	private Map getConfigProperties() {
		Map returnValue = new HashMap();
		returnValue.put(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, FULL_CLASS_NAME );
		returnValue.put(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, PROJECT_NAME );
		returnValue.put(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, ARGUMENTS ) ;
		return returnValue ; 
	}

	
	
}
