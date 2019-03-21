/*
 * Copyright 2009-2018 the original author or authors.
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
package org.codehaus.groovy.eclipse.refactoring.test.internal;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.eclipse.core.builder.GroovyClasspathContainer;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;
import org.osgi.framework.Bundle;

/**
 * Helper methods to set up a IJavaProject.
 */
public class JavaProjectHelper {

    private static final int MAX_RETRY = 5;

    /**
     * @param projectName name of the project
     * @param binFolderName name of the output folder (may be null)
     */
    public static IJavaProject createJavaProject(String projectName, String binFolderName) throws Exception {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(projectName);
        if (!project.exists()) {
            project.create(null);
        } else {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }

        if (!project.isOpen()) {
            project.open(null);
        }

        IPath outputLocation;
        if (binFolderName != null && binFolderName.length() > 0) {
            IFolder binFolder = project.getFolder(binFolderName);
            if (!binFolder.exists()) {
                CoreUtility.createFolder(binFolder, false, true, null);
            }
            outputLocation = binFolder.getFullPath();
        } else {
            outputLocation = project.getFullPath();
        }

        if (!project.hasNature(JavaCore.NATURE_ID)) {
            addNatureToProject(project, JavaCore.NATURE_ID, null);
        }

        IJavaProject jproject = JavaCore.create(project);
        jproject.setOutputLocation(outputLocation, null);
        removeFromClasspath(jproject, jproject.getPath());
        addToClasspath(jproject, JavaRuntime.getDefaultJREContainerEntry());
        //jproject.setOptions(putCompilerOptions(new HashMap<>(), JavaCore.VERSION_1_6));
        return jproject;
    }

    /**
     * @param projectName name of the project
     * @param binFolderName name of the output folder (may be null)
     */
    public static IJavaProject createGroovyProject(String projectName, String binFolderName) throws Exception {
        IJavaProject jproject = createJavaProject(projectName, binFolderName);

        if (!jproject.getProject().hasNature(GroovyNature.GROOVY_NATURE)) {
            addNatureToProject(jproject.getProject(), GroovyNature.GROOVY_NATURE, null);
        }
        addToClasspath(jproject, JavaCore.newContainerEntry(GroovyClasspathContainer.CONTAINER_ID));

        return jproject;
    }

    /**
     * @param options compiler options map to populate
     */
    public static Map<String, String> putCompilerOptions(Map<String, String> options, String compliance) {
        options.put(JavaCore.COMPILER_COMPLIANCE, compliance);
        options.put(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
        options.put(JavaCore.COMPILER_PB_ENUM_IDENTIFIER, JavaCore.ERROR);
        options.put(JavaCore.COMPILER_SOURCE, compliance);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, compliance);
        return options;
    }

    /**
     * Removes a IJavaElement
     *
     * @param elem The element to remove
     * @throws CoreException Removing failed
     * @see #ASSERT_NO_MIXED_LINE_DELIMIERS
     */
    public static void delete(final IJavaElement elem) throws Exception {
        ResourcesPlugin.getWorkspace().run(monitor -> {
            performDummySearch();
            if (elem instanceof IJavaProject) {
                IJavaProject jproject = (IJavaProject) elem;
                jproject.setRawClasspath(new IClasspathEntry[0], jproject.getProject().getFullPath(), null);
            }
            delete(elem.getResource());
        }, null);
        emptyDisplayLoop();
    }

    public static void delete(IResource resource) throws CoreException {
        for (int i= 0; i < MAX_RETRY; i++) {
            try {
                resource.delete(true, null);
                i= MAX_RETRY;
            } catch (CoreException e) {
                if (i == MAX_RETRY - 1) {
                    JavaPlugin.log(e);
                    throw e;
                }
                try {
                    Thread.sleep(1000); // sleep a second
                } catch (InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Removes all files in the project and sets the given classpath
     * @param jproject The project to clear
     * @param entries The default class path to set
     * @throws Exception Clearing the project failed
     */
    public static void clear(final IJavaProject jproject, final IClasspathEntry[] entries) throws Exception {
        performDummySearch();
        ResourcesPlugin.getWorkspace().run(monitor -> {
            jproject.setRawClasspath(entries, null);

            IResource[] resources= jproject.getProject().members();
            for (int i= 0; i < resources.length; i++) {
                if (!resources[i].getName().startsWith(".")) {
                    delete(resources[i]);
                }
            }
        }, null);

        JavaProjectHelper.emptyDisplayLoop();
    }

    public static void performDummySearch() throws CoreException {
        new SearchEngine().searchAllTypeNames(
            null,
            SearchPattern.R_EXACT_MATCH,
            "XXXXXXXXX".toCharArray(), // make sure we search a concrete name. This is faster according to Kent
            SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
            IJavaSearchConstants.CLASS,
            SearchEngine.createJavaSearchScope(new IJavaElement[0]),
            new TypeNameRequestor() {},
            IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
            null);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName) throws Exception {
        return addSourceContainer(jproject, containerName, new Path[0]);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param exclusionFilters Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] exclusionFilters) throws Exception {
        return addSourceContainer(jproject, containerName, new Path[0], exclusionFilters);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param inclusionFilters Inclusion filters to set
     * @param exclusionFilters Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] inclusionFilters, IPath[] exclusionFilters) throws Exception {
        return addSourceContainer(jproject, containerName, inclusionFilters, exclusionFilters, null);
    }

    /**
     * Adds a source container to a IJavaProject.
     * @param jproject The parent project
     * @param containerName The name of the new source container
     * @param inclusionFilters Inclusion filters to set
     * @param exclusionFilters Exclusion filters to set
     * @param outputLocation The location where class files are written to, <b>null</b> for project output folder
     * @return The handle to the new source container
     * @throws CoreException Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(IJavaProject jproject, String containerName, IPath[] inclusionFilters, IPath[] exclusionFilters, String outputLocation) throws Exception {
        IProject project= jproject.getProject();
        IContainer container= null;
        if (containerName == null || containerName.length() == 0) {
            container= project;
        } else {
            IFolder folder= project.getFolder(containerName);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            container= folder;
        }
        IPackageFragmentRoot root= jproject.getPackageFragmentRoot(container);

        IPath outputPath= null;
        if (outputLocation != null) {
            IFolder folder= project.getFolder(outputLocation);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            outputPath= folder.getFullPath();
        }
        IClasspathEntry cpe= JavaCore.newSourceEntry(root.getPath(), inclusionFilters, exclusionFilters, outputPath);
        addToClasspath(jproject, cpe);
        return root;
    }

    /**
     * Removes a source folder from a IJavaProject.
     * @param jproject The parent project
     * @param containerName Name of the source folder to remove
     * @throws CoreException Remove failed
     */
    public static void removeSourceContainer(IJavaProject jproject, String containerName) throws Exception {
        IFolder folder= jproject.getProject().getFolder(containerName);
        removeFromClasspath(jproject, folder.getFullPath());
        folder.delete(true, null);
    }

    /**
     * Adds a library entry to a IJavaProject.
     * @param jproject The parent project
     * @param path The path of the library to add
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path) throws Exception {
        return addLibrary(jproject, path, null, null);
    }

    /**
     * Adds a library entry with source attachment to a IJavaProject.
     * @param jproject The parent project
     * @param path The path of the library to add
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot) throws Exception {
        IClasspathEntry cpe= JavaCore.newLibraryEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        IResource workspaceResource= ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (workspaceResource != null) {
            return jproject.getPackageFragmentRoot(workspaceResource);
        }
        return jproject.getPackageFragmentRoot(path.toString());
    }

    /**
     * Copies the library into the project and adds it as library entry.
     * @param jproject The parent project
     * @param jarPath
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws IOException
     * @throws CoreException
     */
    public static IPackageFragmentRoot addLibraryWithImport(IJavaProject jproject, IPath jarPath, IPath sourceAttachPath, IPath sourceAttachRoot) throws Exception {
        IProject project = jproject.getProject();
        IFile newFile = project.getFile(jarPath.lastSegment());
        try (InputStream inputStream = new FileInputStream(jarPath.toFile())) {
            newFile.create(inputStream, true, null);
        }
        return addLibrary(jproject, newFile.getFullPath(), sourceAttachPath, sourceAttachRoot);
    }

    /**
     * Creates and adds a class folder to the class path.
     * @param jproject The parent project
     * @param containerName
     * @param sourceAttachPath The source attachment path
     * @param sourceAttachRoot The source attachment root path
     * @return The handle of the created root
     * @throws CoreException
     */
    public static IPackageFragmentRoot addClassFolder(IJavaProject jproject, String containerName, IPath sourceAttachPath, IPath sourceAttachRoot) throws Exception {
        IProject project= jproject.getProject();
        IContainer container= null;
        if (containerName == null || containerName.length() == 0) {
            container= project;
        } else {
            IFolder folder= project.getFolder(containerName);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            container= folder;
        }
        IClasspathEntry cpe= JavaCore.newLibraryEntry(container.getFullPath(), sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        return jproject.getPackageFragmentRoot(container);
    }

    /**
     * Adds a variable entry with source attachment to a IJavaProject.
     * Can return null if variable can not be resolved.
     * @param jproject The parent project
     * @param path The variable path
     * @param sourceAttachPath The source attachment path (variable path)
     * @param sourceAttachRoot The source attachment root path (variable path)
     * @return The added package fragment root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addVariableEntry(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot) throws Exception {
        IClasspathEntry cpe= JavaCore.newVariableEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        IPath resolvedPath= JavaCore.getResolvedVariablePath(path);
        if (resolvedPath != null) {
            return jproject.getPackageFragmentRoot(resolvedPath.toString());
        }
        return null;
    }

    /**
     * Adds a required project entry.
     * @param jproject Parent project
     * @param required Project to add to the build path
     * @throws JavaModelException Creation failed
     */
    public static void addRequiredProject(IJavaProject jproject, IJavaProject required) throws Exception {
        IClasspathEntry cpe= JavaCore.newProjectEntry(required.getProject().getFullPath());
        addToClasspath(jproject, cpe);
    }

    public static void removeFromClasspath(IJavaProject jproject, IPath path) throws Exception {
        IClasspathEntry[] oldEntries= jproject.getRawClasspath();
        int nEntries= oldEntries.length;
        List<IClasspathEntry> list= new ArrayList<>(nEntries);
        for (int i= 0 ; i < nEntries ; i++) {
            IClasspathEntry curr= oldEntries[i];
            if (!path.equals(curr.getPath())) {
                list.add(curr);
            }
        }
        IClasspathEntry[] newEntries= list.toArray(new IClasspathEntry[list.size()]);
        jproject.setRawClasspath(newEntries, null);
    }

    public static void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws Exception {
        IClasspathEntry[] oldEntries= jproject.getRawClasspath();
        for (int i= 0; i < oldEntries.length; i++) {
            if (oldEntries[i].equals(cpe)) {
                return;
            }
        }
        int nEntries= oldEntries.length;
        IClasspathEntry[] newEntries= new IClasspathEntry[nEntries + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
        newEntries[nEntries]= cpe;
        jproject.setRawClasspath(newEntries, null);
    }

    private static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws Exception {
        IProjectDescription description = proj.getDescription();
        String[] prevNatures= description.getNatureIds();
        String[] newNatures= new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length]= natureId;
        description.setNatureIds(newNatures);
        proj.setDescription(description, monitor);
    }

    /**
     * Imports resources from <code>bundleSourcePath</code> inside <code>bundle</code> into <code>importTarget</code>.
     *
     * @param importTarget the parent container
     * @param bundle the bundle
     * @param bundleSourcePath the path to a folder containing resources
     *
     * @throws CoreException import failed
     * @throws IOException import failed
     */
    public static void importResources(IContainer importTarget, Bundle bundle, String bundleSourcePath) throws Exception {
        Enumeration<String> entryPaths = bundle.getEntryPaths(bundleSourcePath);
        while (entryPaths.hasMoreElements()) {
            String path = entryPaths.nextElement();
            IPath name = new Path(path.substring(bundleSourcePath.length()));
            if (path.endsWith("/")) {
                IFolder folder = importTarget.getFolder(name);
                folder.create(false, true, null);
                importResources(folder, bundle, path);
            } else {
                URL url = bundle.getEntry(path);
                URLConnection con = url.openConnection(); con.setUseCaches(false);
                importTarget.getFile(name).create(con.getInputStream(), true, null);
            }
        }
    }

    public static void emptyDisplayLoop() {
        boolean showDebugInfo = false;

        Display display = Display.getCurrent();
        if (display != null) {
            if (showDebugInfo) {
                try {
                    Synchronizer synchronizer = display.getSynchronizer();
                    Field field = Synchronizer.class.getDeclaredField("messageCount");
                    field.setAccessible(true);
                    System.out.println("Processing " + field.getInt(synchronizer) + " messages in queue");
                } catch (Exception e) {
                    // ignore
                    System.out.println(e);
                }
            }
            while (display.readAndDispatch()) {
                /*loop*/
            }
        }
    }
}
