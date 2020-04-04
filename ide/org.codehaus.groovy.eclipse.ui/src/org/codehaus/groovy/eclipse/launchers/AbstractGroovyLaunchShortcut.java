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
import static org.codehaus.groovy.eclipse.core.model.GroovyRuntime.DSLD_CONTAINER_ID;
import static org.eclipse.jdt.internal.corext.util.JavaModelUtil.getPackageFragmentRoot;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_CLASSPATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_EXCLUDE_TEST_CODE;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS;
import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY;
import static org.eclipse.jdt.launching.JavaRuntime.JRE_CONTAINER;
import static org.eclipse.jdt.launching.JavaRuntime.computeUnresolvedRuntimeClasspath;
import static org.eclipse.jdt.launching.JavaRuntime.newArchiveRuntimeClasspathEntry;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.groovy.core.util.ScriptFolderSelector;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.osgi.framework.Version;

public abstract class AbstractGroovyLaunchShortcut implements ILaunchShortcut {

    public static final String GROOVY_TYPE_TO_RUN = "org.codehaus.groovy.eclipse.launch.runType";

    /**
     * Returns the Groovy launch configuration type from the Launch Manager.
     */
    protected abstract ILaunchConfigurationType getGroovyLaunchConfigType();

    protected abstract String  applicationOrConsole();

    protected abstract boolean canLaunchWithNoType();

    protected String classToRun() {
        throw new IllegalStateException(getClass().getSimpleName() + " must override classToRun() or mainArgs(IType,IJavaProject)");
    }

    protected String mainArgs(final IType runType, final IJavaProject javaProject) {
        StringBuilder mainArgs = new StringBuilder(classToRun());
        if (runType != null) {
            try {
                mainArgs.append(" \"${workspace_loc:").append(runType.getResource().getFullPath().toOSString().substring(1)).append("}\"");
            } catch (NullPointerException e) {
                GroovyCore.logException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_failureToLaunch, applicationOrConsole()),
                    new IllegalArgumentException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_notFound, runType.getElementName())));
            }
        }
        return mainArgs.toString();
    }

    //--------------------------------------------------------------------------

    @Override
    public void launch(final IEditorPart editor, final String mode) {
        ICompilationUnit unit = Adapters.adapt(editor, GroovyCompilationUnit.class);
        if (unit != null) {
            launchGroovy(unit, unit.getJavaProject(), mode);
        }
    }

    @Override
    public void launch(final ISelection selection, final String mode) {
        ICompilationUnit unit = null;
        if (selection instanceof IStructuredSelection) {
            IJavaElement element = Adapters.adapt(((IStructuredSelection) selection).getFirstElement(), IJavaElement.class);
            if (element != null) {
                IType type = (IType) element.getAncestor(IJavaElement.TYPE);
                if (type != null) {
                    launchGroovy(type, type.getJavaProject(), mode); return;
                }
                unit = (ICompilationUnit) element.getAncestor(IJavaElement.COMPILATION_UNIT);
            }
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
            MessageDialog.openError(GroovyPlugin.getActiveWorkbenchShell(), LaunchShortcutHelper.GroovyLaunchShortcut_noSelection,
                javaProject != null ? LaunchShortcutHelper.GroovyLaunchShortcut_noSelection0 : LaunchShortcutHelper.GroovyLaunchShortcut_noSelection1);
        }
    }

    private IJavaProject extractJavaProject(final ISelection selection) {
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
     * Finds or creates a launch configuration for the given unit and starts it.
     */
    protected void launchGroovy(final ICompilationUnit unit, final IJavaProject javaProject, final String mode) {
        IType runType = null;
        if (unit != null) {
            try {
                IType[] types = Arrays.stream(unit.getAllTypes()).filter(GroovyProjectFacade::hasGroovyMainMethod).toArray(IType[]::new);

                if (types.length == 1) {
                    runType = types[0];
                } else if (types.length > 1) {
                    runType = LaunchShortcutHelper.chooseFromList(Arrays.asList(types), new JavaUILabelProvider(),
                        LaunchShortcutHelper.SelectMainTypeDialog_title, LaunchShortcutHelper.SelectMainTypeDialog_message);
                } else {
                    MessageDialog.openError(GroovyPlugin.getActiveWorkbenchShell(), LaunchShortcutHelper.GroovyLaunchShortcut_noSelection,
                                LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_notRunnable, applicationOrConsole()));
                    return;
                }
            } catch (JavaModelException e) {
                GroovyCore.logException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_failureToLaunch, applicationOrConsole()), e);
                return;
            }
        }
        launchGroovy(runType, javaProject, mode);
    }

    /**
     * Finds or creates a launch configuration for the given type and starts it.
     */
    protected void launchGroovy(final IType runType, final IJavaProject javaProject, final String mode) {
        try {
            boolean excludeTestCode = (runType != null && !hasTestAttribute(runType));
            Map<String, String> launchProperties = createLaunchProperties(runType, javaProject);
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

            List<String> sourcepath = new ArrayList<>();
            for (IRuntimeClasspathEntry entry : computeUnresolvedRuntimeClasspath(javaProject, excludeTestCode)) {
                sourcepath.add(entry.getMemento());
            }

            workingCopy.setAttribute(ATTR_CLASSPATH, classpath);
            workingCopy.setAttribute(ATTR_SOURCE_PATH, sourcepath);
            workingCopy.setAttribute(ATTR_DEFAULT_CLASSPATH, false);
            workingCopy.setAttribute(ATTR_DEFAULT_SOURCE_PATH, false);
            workingCopy.setAttribute(ATTR_EXCLUDE_TEST_CODE, excludeTestCode);

            ILaunchConfiguration config = workingCopy.doSave();
            DebugUITools.launch(config, mode);
        } catch (Exception e) {
            GroovyCore.logException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_failureToLaunch, applicationOrConsole()), e);
        }
    }

    protected Map<String, String> createLaunchProperties(final IType runType, final IJavaProject javaProject) {
        Map<String, String> launchConfigProperties = new HashMap<>();
        launchConfigProperties.put(ATTR_MAIN_TYPE_NAME,
            "org.codehaus.groovy.tools.GroovyStarter");
        launchConfigProperties.put(ATTR_PROJECT_NAME,
            javaProject.getElementName());
        launchConfigProperties.put(ATTR_VM_ARGUMENTS,
            "-Dgroovy.home=\"${groovy_home}\" -Djava.system.class.loader=groovy.lang.GroovyClassLoader");
        launchConfigProperties.put(ATTR_PROGRAM_ARGUMENTS,
            "--classpath " + getRuntimeClasspath(runType, javaProject) + " --main " + mainArgs(runType, javaProject));
        launchConfigProperties.put(ATTR_WORKING_DIRECTORY,
            getWorkingDirectory(runType, javaProject));
        launchConfigProperties.put(GROOVY_TYPE_TO_RUN,
            Optional.ofNullable(runType).map(IType::getFullyQualifiedName).orElse(""));
        return launchConfigProperties;
    }

    private String getRuntimeClasspath(final IType runType, final IJavaProject javaProject) {
        Set<IPath> classpath = new LinkedHashSet<>();
        Set<IJavaProject> done = new LinkedHashSet<>();
        Deque<IJavaProject> todo = new LinkedList<>(Arrays.asList(javaProject));
        try {
            boolean excludeTestCode = (runType != null && !hasTestAttribute(runType));
            do {
                IJavaProject nextProject = todo.removeFirst();
                if (!done.add(nextProject) || !nextProject.getProject().isAccessible()) {
                    continue;
                }
                for (IClasspathEntry entry : nextProject.getRawClasspath()) {
                    if ((excludeTestCode && entry.isTest()) || (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER &&
                            (DSLD_CONTAINER_ID.equals(entry.getPath().segment(0)) || JRE_CONTAINER.equals(entry.getPath().segment(0))))) {
                        continue;
                    }
                    switch (entry.getEntryKind()) {
                    case IClasspathEntry.CPE_SOURCE:
                        if (entry.getOutputLocation() != null) {
                            classpath.add(entry.getOutputLocation());
                        } else {
                            classpath.add(nextProject.getOutputLocation());
                        }
                        // fall through
                    case IClasspathEntry.CPE_LIBRARY:
                        classpath.add(entry.getPath());
                        break;
                    case IClasspathEntry.CPE_VARIABLE:
                        entry = JavaCore.getResolvedClasspathEntry(entry);
                        if (entry != null) classpath.add(entry.getPath());
                        break;
                    case IClasspathEntry.CPE_PROJECT:
                        String projectName = entry.getPath().segment(0);
                        todo.add(javaProject.getJavaModel().getJavaProject(projectName));
                        break;
                    case IClasspathEntry.CPE_CONTAINER:
                        IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), nextProject);
                        Arrays.stream(container.getClasspathEntries())
                            .filter(it -> !excludeTestCode || !it.isTest())
                            .map(IClasspathEntry::getPath).forEach(classpath::add);
                        break;
                    }
                }
            } while (!todo.isEmpty());
            //
        } catch (JavaModelException e) {
            GroovyCore.logException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_classpathError, applicationOrConsole()), e);
        }

        StringBuilder sb = new StringBuilder();
        for (IPath path : classpath) {
            if (path.isEmpty()) {
                sb.append("${workspace_loc}");
            } else {
                path = path.makeAbsolute();
                // TODO: fails if directory in filesystem path is also a project name in workspace
                if (!ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0)).exists()) {
                    sb.append(path.toOSString());
                } else {
                    sb.append("${workspace_loc:").append(path.segment(0)).append('}');
                    if (path.segmentCount() > 1) {
                        sb.append(File.separatorChar).append(path.removeFirstSegments(1).toOSString());
                    }
                }
            }
            sb.append(File.pathSeparatorChar);
        }
        if (sb.length() > 0) {
            sb.insert(0, '"').deleteCharAt(sb.length() - 1).append('"');
        }
        return sb.toString();
    }

    private String getWorkingDirectory(final IType runType, final IJavaProject javaProject) {
        String workingDirPreference = GroovyPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.GROOVY_SCRIPT_DEFAULT_WORKING_DIRECTORY);

        switch (workingDirPreference) {
        case PreferenceConstants.GROOVY_SCRIPT_ECLIPSE_HOME:
            return "${eclipse_home}";
        case PreferenceConstants.GROOVY_SCRIPT_SCRIPT_LOC:
            if (runType != null) {
                try {
                    return "${workspace_loc:" + runType.getResource().getParent().getFullPath().toOSString().substring(1) + "}";
                } catch (Exception e) {
                    GroovyCore.logException(LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_notFound, runType.getElementName()), e);
                }
            }
        }
        return null; // aka "${workspace_loc:project_name}"
    }

    /**
     * Finds a launch configration that matches the given name and properties or
     * creates one.
     */
    protected ILaunchConfigurationWorkingCopy findOrCreateLaunchConfig(final Map<String, String> launchProperties, final String launchName) throws CoreException {
        String projectName = launchProperties.get(ATTR_PROJECT_NAME);
        ILaunchConfiguration config = findConfiguration(projectName, launchProperties.get(GROOVY_TYPE_TO_RUN));
        return (config != null ? config.getWorkingCopy() : createLaunchConfig(launchProperties, launchName));
    }

    /**
     * Creates a launch configuration for the given name and properties.
     */
    private ILaunchConfigurationWorkingCopy createLaunchConfig(final Map<String, String> launchProperties, final String launchName) throws CoreException {
        ILaunchConfigurationWorkingCopy workingCopy = getGroovyLaunchConfigType().newInstance(
            null, getLaunchManager().generateLaunchConfigurationName(launchName));

        for (Map.Entry<String, String> entry : launchProperties.entrySet()) {
            workingCopy.setAttribute(entry.getKey(), entry.getValue());
        }

        return workingCopy;
    }

    /**
     * Finds launch configrations that match the given project and type names.
     * If more than one match is found, the user is prompted to select one.
     */
    private ILaunchConfiguration findConfiguration(final String projectName, final String mainTypeName) throws CoreException {
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
     * @param configList {@link ILaunchConfigration}s for the user to pick from
     */
    private ILaunchConfiguration chooseConfiguration(final List<ILaunchConfiguration> configList) {
        return LaunchShortcutHelper.chooseFromList(configList, DebugUITools.newDebugModelPresentation(),
            LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_title, applicationOrConsole()),
            LaunchShortcutHelper.bind(LaunchShortcutHelper.GroovyLaunchShortcut_message, applicationOrConsole()));
    }

    /**
     * Returns the default Launch Manager.
     */
    protected static ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }

    /**
     * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaApplicationLaunchShortcut#isTestCode(IType)
     */
    protected static boolean hasTestAttribute(final IJavaElement element) throws JavaModelException {
        return getPackageFragmentRoot(element).getResolvedClasspathEntry().isTest();
    }

    protected static boolean isAtLeastGroovy(final int major, final int minor, final int micro) {
        Version active = getActiveGroovyBundle().getVersion();
        Version target = new Version(major, minor, micro);
        return (active.compareTo(target) >= 0);
    }

    protected static boolean matchesScriptFilter(final IResource resource) {
        return new ScriptFolderSelector(resource.getProject()).isScript(resource);
    }
}
