/*
 * Copyright 2009-2020 the original author or authors.
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
package org.codehaus.groovy.eclipse.launchers;

import static org.codehaus.groovy.eclipse.core.compiler.CompilerUtils.getActiveGroovyBundle;
import static org.codehaus.groovy.eclipse.core.compiler.CompilerUtils.getExportedGroovyAllJar;
import static org.codehaus.groovy.eclipse.core.compiler.CompilerUtils.getExtraJarsForClasspath;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY;
import static org.eclipse.jdt.launching.JavaRuntime.newArchiveRuntimeClasspathEntry;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.groovy.eclipse.GroovyPlugin;
import org.codehaus.groovy.eclipse.core.GroovyCore;
import org.codehaus.groovy.eclipse.core.preferences.PreferenceConstants;
import org.codehaus.jdt.groovy.model.GroovyCompilationUnit;
import org.codehaus.jdt.groovy.model.GroovyProjectFacade;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Version;

public abstract class AbstractGroovyLaunchShortcut implements ILaunchShortcut {

    public static final String GROOVY_TYPE_TO_RUN = "org.codehaus.groovy.eclipse.launch.runType";

    /**
     * This is the string that will show if the groovy file the user is trying to run doesn't meet the criteria to be run.
     */
    public static final String GROOVY_FILE_NOT_RUNNABLE_MESSAGE = "Groovy {0} not found in current selection";

    /**
     * Used for dialog presentation if the user needs to choose from matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TITLE = "Select Groovy {0} to launch";

    /**
     * Used for dialog presentation if the user needs to choose from matching Launch configurations
     */
    public static final String SELECT_CONFIG_DIALOG_TEXT = "Select the Groovy {0} to launch";

    private final String notRunnable = MessageFormat.format(GROOVY_FILE_NOT_RUNNABLE_MESSAGE, applicationOrConsole());
    private final String title = MessageFormat.format(SELECT_CONFIG_DIALOG_TITLE, applicationOrConsole());
    private final String text = MessageFormat.format(SELECT_CONFIG_DIALOG_TEXT, applicationOrConsole());

    /**
     * Returns the Groovy launch configuration type from the Launch Manager.
     */
    protected abstract ILaunchConfigurationType getGroovyLaunchConfigType();

    protected abstract String  applicationOrConsole();

    protected abstract boolean canLaunchWithNoType();

    protected String classToRun() {
        throw new IllegalStateException(getClass().getSimpleName() + " must override classToRun() or mainArgs(IType,IJavaProject)");
    }

    protected String mainArgs(IType runType, IJavaProject javaProject) {
        StringBuilder mainArgs = new StringBuilder(classToRun());
        if (runType != null) {
            try {
                mainArgs.append(" \"${workspace_loc:").append(runType.getResource().getFullPath().toOSString().substring(1)).append("}\"");
            } catch (NullPointerException e) {
                GroovyCore.logException("Error running Groovy", new IllegalArgumentException("Could not find file to run for " + runType));
            }
        }
        return mainArgs.toString();
    }

    //--------------------------------------------------------------------------

    @Override
    public void launch(IEditorPart editor, String mode) {
        ICompilationUnit unit = Adapters.adapt(editor, GroovyCompilationUnit.class);
        if (unit != null) {
            launchGroovy(unit, unit.getJavaProject(), mode);
        }
    }

    @Override
    public void launch(ISelection selection, String mode) {
        ICompilationUnit unit = null;
        if (selection instanceof IStructuredSelection) {
            unit = Adapters.adapt(((IStructuredSelection) selection).getFirstElement(), GroovyCompilationUnit.class);
        }

        IJavaProject javaProject;
        if (unit != null) {
            javaProject = unit.getJavaProject();
        } else {
            javaProject = extractJavaProject(selection);
        }

        if (javaProject != null && (unit != null || canLaunchWithNoType())) {
            launchGroovy(unit, javaProject, mode);
        } else {
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "Can't run script", javaProject != null ? "No script selected!" : "No script or project selected!");
        }
    }

    private IJavaProject extractJavaProject(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection struct = (IStructuredSelection) selection;
            Object obj = struct.getFirstElement();
            if (obj instanceof IAdaptable) {
                IJavaProject javaProject = Adapters.adapt(obj, IJavaProject.class);
                if (javaProject != null) {
                    return javaProject;
                }
                IProject project = Adapters.adapt(obj, IProject.class);
                if (project != null) {
                    return JavaCore.create(project);
                }
            }
        }
        return null;
    }

    /**
     * Finds or creates a launch configuration for the given unit and then launches it.
     */
    protected void launchGroovy(ICompilationUnit unit, IJavaProject javaProject, String mode) {
        IType runType = null;
        if (unit != null) {
            IType[] types = null;
            try {
                types = unit.getAllTypes();
            } catch (JavaModelException e) {
                GroovyCore.logException("Error running Groovy", e);
                return;
            }
            runType = findClassToRun(types);
            if (runType == null) {
                GroovyCore.logException("Error running Groovy", new Exception(notRunnable));
                return;
            }
        }

        Map<String, String> launchProperties = createLaunchProperties(runType, javaProject);
        try {
            String launchName = (runType != null ? runType.getElementName() : javaProject.getElementName());
            ILaunchConfigurationWorkingCopy workingCopy = findOrCreateLaunchConfig(launchProperties, launchName);

            List<String> classpath = new ArrayList<>();
            classpath.add(newArchiveRuntimeClasspathEntry(getExportedGroovyAllJar()).getMemento());
            for (IPath jarPath : getExtraJarsForClasspath()) {
                classpath.add(newArchiveRuntimeClasspathEntry(jarPath).getMemento());
            }
            Enumeration<URL> jarUrls = getActiveGroovyBundle().findEntries("lib/" + applicationOrConsole().toLowerCase(), "*.jar", false);
            if (jarUrls != null) {
                for (URL jarUrl : Collections.list(jarUrls)) {
                    classpath.add(newArchiveRuntimeClasspathEntry(new Path(FileLocator.toFileURL(jarUrl).getPath())).getMemento());
                }
            }

            workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
            workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH, false);

            ILaunchConfiguration config = workingCopy.doSave();
            DebugUITools.launch(config, mode);
        } catch (CoreException | IOException e) {
            GroovyCore.logException("Error running Groovy file: " + unit.getResource().getName(), e);
        }
    }

    protected Map<String, String> createLaunchProperties(IType runType, IJavaProject javaProject) {
        Map<String, String> launchConfigProperties = new HashMap<>();
        launchConfigProperties.put(ATTR_MAIN_TYPE_NAME,
            "org.codehaus.groovy.tools.GroovyStarter");
        launchConfigProperties.put(ATTR_PROJECT_NAME,
            javaProject.getElementName());
        launchConfigProperties.put(ATTR_VM_ARGUMENTS,
            "-Dgroovy.home=\"${groovy_home}\" -Djava.system.class.loader=groovy.lang.GroovyClassLoader");
        launchConfigProperties.put(ATTR_PROGRAM_ARGUMENTS,
            "--classpath " + generateClasspath(javaProject) + " --main " + mainArgs(runType, javaProject));
        launchConfigProperties.put(ATTR_WORKING_DIRECTORY,
            getWorkingDirectory(runType, javaProject));
        launchConfigProperties.put(GROOVY_TYPE_TO_RUN,
            Optional.ofNullable(runType).map(IType::getFullyQualifiedName).orElse(""));
        return launchConfigProperties;
    }

    protected final String getProjectLocation(IJavaElement elt) {
        return "${workspace_loc:" + elt.getJavaProject().getProject().getName() + "}";
    }

    protected final String getProjectLocation(IPath path) {
        if (path.segmentCount() > 0) {
            return "${workspace_loc:" + path.segment(0) + "}";
        } else {
            return "${workspace_loc}";
        }
    }

    private String getWorkingDirectory(IType runType, IJavaProject javaProject) {
        String workingDirPreference = GroovyPlugin.getDefault().getPreferenceStore()
            .getString(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY);

        switch (workingDirPreference) {
        case PreferenceConstants.GROOVY_SCRIPT_ECLIPSE_HOME:
            return "${eclipse_home}";
        case PreferenceConstants.GROOVY_SCRIPT_SCRIPT_LOC:
            if (runType != null) {
                try {
                    return "${workspace_loc:" + runType.getResource().getParent().getFullPath().toOSString().substring(1) + "}";
                } catch (Exception e) {
                    GroovyCore.logException("Unable to find the location of " + runType.getElementName(), e);
                }
            }
        }
        return null; // aka "${workspace_loc:project_name}"
    }

    private String generateClasspath(IJavaProject javaProject) {
        Set<String> sourceEntries = new TreeSet<>();
        Set<String> binEntries = new TreeSet<>();
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
            sb.replace(sb.length() - 1, sb.length(), "\"");
        }
        return sb.toString();
    }

    /**
     * Need to recursively walk the classpath and visit all dependent projects
     * Not looking at classpath containers yet.
     */
    private void addClasspathEntriesForProject(IJavaProject javaProject, Set<String> sourceEntries, Set<String> binEntries) {
        List<IJavaProject> dependingProjects = new ArrayList<>();
        try {
            IClasspathEntry[] entries = javaProject.getRawClasspath();
            for (IClasspathEntry entry : entries) {
                int kind = entry.getEntryKind();
                switch (kind) {
                case IClasspathEntry.CPE_LIBRARY:
                    IPath libPath = entry.getPath();
                    if (!isPathInWorkspace(libPath)) {
                        sourceEntries.add(libPath.toOSString());
                        break;
                    }
                    // fall through
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
     */
    private boolean isPathInWorkspace(IPath libPath) {
        if (!libPath.isAbsolute() || libPath.segmentCount() == 0) {
            return true;
        }
        return ResourcesPlugin.getWorkspace().getRoot().getProject(libPath.segment(0)).exists();
    }

    /**
     * Finds the runnable classnode in an array.  If more than one possible node is found,
     * will prompt the user to select one.
     *
     * @return Returns a classnode if found, or null if no classNode can be run.
     * @throws OperationCanceledException If the user selects cancel
     */
    private IType findClassToRun(IType[] types) {
        IType returnValue = null;
        List<IType> candidates = new ArrayList<>();
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
     * Finds a launch configration that matches the given properties or creates one.
     */
    protected ILaunchConfigurationWorkingCopy findOrCreateLaunchConfig(Map<String, String> launchProperties, String launchName) throws CoreException {
        String projectName = launchProperties.get(ATTR_PROJECT_NAME);
        ILaunchConfiguration config = findConfiguration(projectName, launchProperties.get(GROOVY_TYPE_TO_RUN));
        return (config != null ? config.getWorkingCopy() : createLaunchConfig(launchProperties, launchName));
    }

    /**
     * Creates a launch configuration for the given name and properties.
     */
    private ILaunchConfigurationWorkingCopy createLaunchConfig(Map<String, String> launchProperties, String launchName) throws CoreException {
        ILaunchConfigurationWorkingCopy workingCopy = getGroovyLaunchConfigType().newInstance(
            null, getLaunchManager().generateLaunchConfigurationName(launchName));

        for (Map.Entry<String, String> entry : launchProperties.entrySet()) {
            workingCopy.setAttribute(entry.getKey(), entry.getValue());
        }

        return workingCopy;
    }

    /**
     * Finds any launch configrations that match the project and type names.
     * If more than one match is found the user is prompted to select one.
     */
    private ILaunchConfiguration findConfiguration(String projectName, String mainTypeName) throws CoreException {
        ILaunchConfiguration returnValue = null;
        ILaunchConfigurationType configType = getGroovyLaunchConfigType();
        List<ILaunchConfiguration> candidateConfigs = new ArrayList<>();
        ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
        for (ILaunchConfiguration config : configs) {
            if (config.getAttribute(GROOVY_TYPE_TO_RUN, "").equals(mainTypeName) &&
                    config.getAttribute(ATTR_PROJECT_NAME, "").equals(projectName)) {
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
     * Prompts the user to select a launch configuration from {@code configList}.
     *
     * @param configList A List of ILaunchConfigrations for the user to pick from.
     * @return The ILaunchConfiguration that the user selected.
     */
    private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> configList) {
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        return LaunchShortcutHelper.chooseFromList(configList, labelProvider, title, text);
    }

    /**
     * Convenince method for getting the Launch Manager from the Debug Plugin.
     *
     * @return Returns the default Eclipse launch manager.
     */
    protected static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    protected static boolean isAtLeastGroovy(int major, int minor, int micro) {
        Version active = getActiveGroovyBundle().getVersion();
        Version target = new Version(major, minor, micro);
        return (active.compareTo(target) >= 0);
    }

    protected static boolean matchesScriptFilter(IResource resource) {
        return new ScriptFolderSelector(resource.getProject()).isScript(resource);
    }
}
