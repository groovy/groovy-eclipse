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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.model.GroovyProjectFacade;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.groovy.eclipse.core.util.ListUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

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

    public static final String GROOVY_TYPE_TO_RUN = "org.codehaus.groovy.eclipse.launch.runType";

    private final String title;
    private final String text;
    private final String msg;

    public AbstractGroovyLaunchShortcut() {
        title = SELECT_CONFIG_DIALOG_TITLE.replace("{0}", applicationOrConsole());
        text = SELECT_CONFIG_DIALOG_TEXT.replace("{0}", applicationOrConsole());
        msg = GROOVY_FILE_NOT_RUNNABLE_MESSAGE.replace("{0}", applicationOrConsole());
    }

    /**
     * Launches from the package explorer.
     *
     * @see ILaunchShortcut#launch
     */
    public void launch(ISelection selection, String mode) {
        ICompilationUnit unit = extractCompilationUnit(selection);
        IJavaProject javaProject;
        if (unit != null) {
            javaProject = unit.getJavaProject();
        } else {
            javaProject = extractJavaProject(selection);
        }
        if (javaProject==null && unit==null) {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Can't run script",
                    "No script or project selected!");
            return;
        }
        if (unit != null || canLaunchWithNoType()) {
            launchGroovy(unit, javaProject, mode);
        } else {
            MessageDialog.openError(PlatformUI.getWorkbench().
                    getActiveWorkbenchWindow().getShell(), "Can't run script", "No script selected!");
        }
    }

    /**
     * @param selection
     * @return
     */
    private IJavaProject extractJavaProject(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection struct = (IStructuredSelection) selection;
            Object obj = struct.getFirstElement();
            IJavaProject javaProject;
            if (obj instanceof IAdaptable) {
                javaProject = (IJavaProject) ((IAdaptable) obj).getAdapter(IJavaProject.class);
                if (javaProject != null) {
                    return javaProject;
                }
                IProject project = (IProject) ((IAdaptable) obj).getAdapter(IProject.class);
                if (project != null) {
                    return JavaCore.create(project);
                }
            }
        }
        return null;
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
    protected void launchGroovy(ICompilationUnit unit, IJavaProject javaProject, String mode) {
        IType runType = null;

        // if unit is null, then we are not looking for a run type
        if (unit != null) {
            IType[] types = null;
            try {
                types = unit.getAllTypes();
            } catch (JavaModelException e) {
                GroovyCore.errorRunningGroovy(e);
                return;
            }
            runType = findClassToRun(types);
            if (runType == null) {
                GroovyCore.errorRunningGroovy(new Exception(msg));
                return;
            }
        }
        Map<String, String> launchConfigProperties = createLaunchProperties(runType, javaProject);

        try {
            ILaunchConfigurationWorkingCopy workingConfig = findOrCreateLaunchConfig(launchConfigProperties,
                    runType != null ? runType.getElementName() : javaProject.getElementName());
            workingConfig.setAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, Arrays.asList(
                            JavaRuntime.computeDefaultRuntimeClassPath(javaProject)));
            ILaunchConfiguration config = workingConfig.doSave();
            DebugUITools.launch(config, mode);
        } catch (CoreException e) {
            GroovyCore.errorRunningGroovyFile((IFile) unit.getResource(), e);
        }

    }


    protected abstract String classToRun();

    protected Map<String, String> createLaunchProperties(IType runType, IJavaProject javaProject) {
        Map<String, String> launchConfigProperties = new HashMap<String, String>();
        String pathToClass;

        if (runType != null) {
            try {
                pathToClass = " \"${resource_loc:" + runType.getResource().getFullPath().toPortableString() + "}\"";
            } catch (NullPointerException e) {
                pathToClass = "";
                GroovyCore.errorRunningGroovy(new IllegalArgumentException("Could not find file to run for " + runType));
            }
        } else {
            pathToClass = "";
        }
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
                "org.codehaus.groovy.tools.GroovyStarter");
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                javaProject.getElementName());
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
                // don't add the groovyConf here
                // see https://jira.codehaus.org/browse/GRECLIPSE-1650
                //                "-Dgroovy.starter.conf="+getGroovyConf() +
                " -Dgroovy.home="+getGroovyHome()
                );
        launchConfigProperties.put(
                GROOVY_TYPE_TO_RUN,
                runType == null ? "" : runType.getFullyQualifiedName()
                );
        launchConfigProperties.put(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "--classpath "
                + generateClasspath(javaProject) + " --main " + classToRun() + pathToClass);
        launchConfigProperties.put(
                IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
                getWorkingDirectory(runType, javaProject));

        return launchConfigProperties;
    }
    private String getGroovyConf() {
        return "\"${groovy_home}/conf/groovy-starter.conf\"";
    }

    private String getGroovyHome() {
        return "\"${groovy_home}\"";
    }

    private String getWorkingDirectory(IType runType, IJavaProject javaProject) {
        String workingDirSetting = GroovyPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY);
        if (workingDirSetting.equals(PreferenceConstants.GROOVY_SCRIPT_ECLIPSE_HOME)) {
            return "${eclipse_home}";
        } else if (workingDirSetting.equals(PreferenceConstants.GROOVY_SCRIPT_SCRIPT_LOC) && runType != null) {
            try {
                return runType.getResource().getParent().getLocation().toOSString();
            } catch (Exception e) {
                GroovyCore.logException("Exception trying to find the location of " + runType.getElementName(), e);
                return getProjectLocation(runType);
            }
        } else {
            // (workingDirSetting.equals(PreferenceConstants.GROOVY_SCRIPT_PROJECT_HOME))
            // default here if there is no type
            return getProjectLocation(javaProject);
        }
    }


    private String getProjectLocation(IJavaElement elt) {
        return "${workspace_loc:/" + elt.getJavaProject().getProject().getName() + "}";
    }

    private String getProjectLocation(IPath path) {
        if (path.segmentCount() > 0) {
            return "${workspace_loc:/" + path.segment(0) + "}";
        } else {
            return "${workspace_loc}";
        }
    }

    /* make protected for testing purposes */
    protected String generateClasspath(IJavaProject javaProject) {
        SortedSet<String> sourceEntries = new TreeSet<String>();
        SortedSet<String> binEntries = new TreeSet<String>();
        addClasspathEntriesForProject(javaProject, sourceEntries, binEntries);
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (String entry : sourceEntries) {
            sb.append(entry);
            sb.append(File.pathSeparator);
        }
        for (String entry : binEntries) {
            sb.append(entry);
            sb.append(File.pathSeparator);
        }

        if (sb.length() > 0) {
            sb.replace(sb.length()-1, sb.length(), "\"");
        }
        return sb.toString();
    }

    /**
     * Need to recursively walk the classpath and visit all dependent projects
     * Not looking at classpath containers yet.
     *
     * @param javaProject
     * @param entries
     */
    private void addClasspathEntriesForProject(IJavaProject javaProject,
            SortedSet<String> sourceEntries, SortedSet<String> binEntries) {
        List<IJavaProject> dependingProjects = new ArrayList<IJavaProject>();
        try {
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : entries) {
                int kind = entry.getEntryKind();
                switch(kind) {
                    case IClasspathEntry.CPE_LIBRARY:
                        IPath libPath = entry.getPath();
                        if (!isPathInWorkspace(libPath)) {
                            sourceEntries.add(libPath.toOSString());
                            break;
                        }
                        //$FALL-THROUGH$
                    case IClasspathEntry.CPE_SOURCE:
                        IPath srcPath = entry.getPath();
                        String sloc = getProjectLocation(srcPath);
                        if (srcPath.segmentCount() > 1) {
                            sloc += File.separator + srcPath.removeFirstSegments(1).toOSString();
                        }
                        sourceEntries.add(sloc);

                        IPath outPath = entry.getOutputLocation();
                        if (outPath != null) {
                            String bloc = getProjectLocation(outPath);
                            if (outPath.segmentCount() > 1) {
                                bloc += File.separator + outPath.removeFirstSegments(1).toOSString();
                            }
                            binEntries.add(bloc);
                        }
                        break;


                    case IClasspathEntry.CPE_PROJECT:
                        dependingProjects.add(javaProject.getJavaModel().getJavaProject(entry.getPath().lastSegment()));
                        break;
                }
            }
            IPath defaultOutPath = javaProject.getOutputLocation();
            if (defaultOutPath != null) {
                String bloc = getProjectLocation(javaProject);
                if (defaultOutPath.segmentCount() > 1) {
                    bloc += File.separator + defaultOutPath.removeFirstSegments(1).toOSString();
                }
                binEntries.add(bloc);
            }
        } catch (JavaModelException e) {
            GroovyCore.logException("Exception generating classpath for launching groovy script", e);
        }
        // recur through dependent projects
        for (IJavaProject dependingProject : dependingProjects) {
            if (dependingProject.getProject().isAccessible()) {
                addClasspathEntriesForProject(dependingProject, sourceEntries, binEntries);
            }
        }
    }

    /**
     * True if this is a path to a resource in the workspace, false otherwise
     *
     * @param libPath
     * @return
     */
    private boolean isPathInWorkspace(IPath libPath) {
        if (!libPath.isAbsolute() || libPath.segmentCount() == 0) {
            return true;
        }
        return ResourcesPlugin.getWorkspace().getRoot().getProject(libPath.segment(0)).exists();
    }

    /**
     * Launches from the source file.
     *
     * @see ILaunchShortcut#launch
     */
    public void launch(IEditorPart editor, String mode) {
        // make sure we are saved as we run groovy from the file
        editor.getEditorSite().getPage().saveEditor(editor, false);
        IEditorInput input = editor.getEditorInput();
        IFile file = (IFile) input.getAdapter(IFile.class);
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit != null) {
            launchGroovy(unit, unit.getJavaProject(), mode);
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
            Map<String, String> configProperties, String simpleMainTypeName)
                    throws CoreException {
        ILaunchConfigurationWorkingCopy returnConfig;
        ILaunchConfiguration config = findConfiguration(configProperties.get(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME),
                configProperties.get(GROOVY_TYPE_TO_RUN));
        if (config == null) {
            returnConfig = createLaunchConfig(configProperties, simpleMainTypeName);
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
        String launchName = getLaunchManager().generateLaunchConfigurationName(classUnderTest);
        ILaunchConfigurationWorkingCopy returnConfig = getGroovyLaunchConfigType().newInstance(null, launchName);

        for (Iterator<String> it = configProperties.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            String value = configProperties.get(key);
            returnConfig.setAttribute(key, value);
        }

        return returnConfig;
    }

    /**
     * This class finds any launch configrations that match the defined
     * properties. If more that one match is found the user is prompted
     * to select one.
     *
     * Semantics now matches {@link JavaLaunchShortcut}. If the main type name
     * and the
     * project name match, then this is considered a match.
     *
     * @param configProperties A <String, String> Map of properties to check
     *            when searching for a matching launch configuration.
     * @return Returns a launch configuration that matches the given properties
     *         if a match is found, otherwise returns null.
     * @throws CoreException
     */
    private ILaunchConfiguration findConfiguration(String projectName, String mainTypeName) throws CoreException {
        ILaunchConfiguration returnValue = null;
        ILaunchConfigurationType configType = getGroovyLaunchConfigType();
        List<ILaunchConfiguration> candidateConfigs = ListUtil.newEmptyList();

        ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
        for (int i = 0; i < configs.length; i++) {
            ILaunchConfiguration config = configs[i];

            if (config.getAttribute(GROOVY_TYPE_TO_RUN, "").equals(mainTypeName) &&
                    config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(projectName)) {
                candidateConfigs.add(config);
            }
        }

        int candidateCount = candidateConfigs.size();
        if (candidateCount == 1) {
            returnValue = candidateConfigs.get(0);
        } else if (candidateCount > 1) {
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
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        return LaunchShortcutHelper.chooseFromList(configList, labelProvider, title, text);
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
        return DebugPlugin.getDefault().getLaunchManager();
    }


    protected abstract String applicationOrConsole();

    protected abstract boolean canLaunchWithNoType();
}
