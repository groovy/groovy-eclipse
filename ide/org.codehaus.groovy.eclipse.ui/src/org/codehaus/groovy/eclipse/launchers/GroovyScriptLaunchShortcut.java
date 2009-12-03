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


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.compiler.CompilerUtils;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.osgi.framework.Bundle;

/**
 * This class is reponsible for creating a launching Groovy script files.  If an 
 * existing launch configuration exists it will use that, if not it will
 * create a new launch configuration and launch it.
 * 
 * @see ILaunchShortcut
 */
public class GroovyScriptLaunchShortcut extends AbstractGroovyLaunchShortcut {

	public static final String GROOVY_SCRIPT_LAUNCH_CONFIG_ID = "org.codehaus.groovy.eclipse.groovyScriptLaunchConfiguration" ; 
	
    public ILaunchConfigurationType getGroovyLaunchConfigType() {
        return getLaunchManager().getLaunchConfigurationType(GROOVY_SCRIPT_LAUNCH_CONFIG_ID) ;
    }
    
    public IType findClassToRun(IType[] types) {
        IType returnValue = null;
        List<IType> candidates = new ArrayList<IType>();
        for (int i = 0; i < types.length; i++) {
            if (GroovyProjectFacade.hasRunnableMain(types[i])) {
                candidates.add(types[i]);
            }
        }
        
        if (candidates.size() == 1) {
            returnValue = candidates.get(0);
        } else {
            returnValue = LaunchShortcutHelper.chooseClassNode(candidates);
        }
        
        return returnValue;
    }
    
    @Override
    protected String applicationOrScript() {
        return "script";
    }

    @Override
    protected Map<String, String> createLaunchProperties(IType runType) {
        Map<String, String> launchConfigProperties = new HashMap<String, String>();
        String className = runType.getCompilationUnit().getResource().getFullPath().removeFirstSegments(1).makeRelative().toOSString();
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, 
                "org.codehaus.groovy.tools.GroovyStarter");
        IJavaProject javaProject = runType.getJavaProject();
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, 
                javaProject.getElementName());
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
                "-Dgroovy.starter.conf="+getGroovyConf() + 
                " -Dgroovy.home="+getGroovyHome()
                );
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
                "--classpath " + getClasspath(javaProject) +
                " --main groovy.ui.GroovyMain " +
                className);
     
        return launchConfigProperties;
    }


    /**
     * @return the classpath as a string.  need to include only the source and output folders.
     * source folders come first since source should be checked before binary for scripts
     */
    private String getClasspath(IJavaProject javaProject) {
        StringBuilder sbSrc = new StringBuilder();
        StringBuilder sbBin = new StringBuilder();
        sbSrc.append("\"");
        try {
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : entries) {
                int kind = entry.getEntryKind();
                if (kind == IClasspathEntry.CPE_SOURCE) {
                    IPath srcPath = entry.getPath();
                    if (srcPath.segmentCount() > 1) {
                        sbSrc.append(srcPath.removeFirstSegments(1).toOSString() + File.pathSeparator);
                    } else {
                        sbSrc.append("." + File.pathSeparator);
                    }
                    IPath outPath = entry.getOutputLocation();
                    if (outPath != null) {
                        if (outPath.segmentCount() > 1) {
                            sbBin.append(outPath.removeFirstSegments(1).toOSString() + File.pathSeparator);
                        } else {
                            sbBin.append("." + File.pathSeparator);
                        }
                    }
                }
            }
            IPath defaultOutPath = javaProject.getOutputLocation();
            if (defaultOutPath != null) {
                if (defaultOutPath.segmentCount() > 1) {
                    sbBin.append(defaultOutPath.removeFirstSegments(1).toOSString());
                } else {
                    sbBin.append(".");
                }
            }
            if (sbBin.length() > 0) {
                sbSrc.append(sbBin);
            } else {
                // remove trailing file separator
                sbSrc.replace(sbBin.length()-1, sbBin.length(), "");
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception launching groovy script", e);
        }
        sbSrc.append("\"");
        return sbSrc.toString();
    }

    @SuppressWarnings("unchecked")
    private String getGroovyConf() {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        Enumeration<URL> enu = groovyBundle.findEntries("conf", "groovy-starter.conf", false);
        if (enu != null) {
            URL jar = enu.nextElement();
            // remove the "reference:/" protocol
            try {
                jar = FileLocator.resolve(jar);
                return "\"" + jar.getFile() + "\"";
            } catch (IOException e) {
                GroovyCore.logException("Error finding groovy-starter.conf", e);
            }
        }
        // should throw an exception here
        return null;
    }
    
    private String getGroovyHome() {
        Bundle groovyBundle = CompilerUtils.getActiveGroovyBundle();
        try {
            return "\"" + 
            FileLocator.getBundleFile(groovyBundle).toString() + "\""
            ;
        } catch (IOException e) {
            GroovyCore.logException("Error finding Groovy Home", e);
            // should throw an exception here
            return null;
        }
    }
}
