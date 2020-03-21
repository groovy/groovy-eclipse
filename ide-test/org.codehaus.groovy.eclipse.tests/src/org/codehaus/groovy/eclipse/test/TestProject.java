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
package org.codehaus.groovy.eclipse.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.codehaus.groovy.eclipse.core.model.GroovyRuntime;
import org.codehaus.groovy.runtime.IOGroovyMethods;
import org.codehaus.jdt.groovy.model.GroovyNature;
import org.eclipse.core.internal.events.BuildCommand;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.groovy.tests.SimpleProgressMonitor;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.groovy.core.util.ArrayUtils;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.launching.JavaRuntime;

public class TestProject {

    private final IProject project;

    private final IJavaProject javaProject;

    private IPackageFragmentRoot sourceFolder;

    public TestProject() throws Exception {
        this("TestProject");
    }

    public TestProject(String name) throws Exception {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        project = root.getProject(name);
        if (!project.exists()) {
            project.create(null);
        }
        project.open(null);

        javaProject = JavaCore.create(project);
        prepareForJava();
        prepareForGroovy();

        createOutputFolder(createBinFolder());
        sourceFolder = createSourceFolder();

        javaProject.setOption(CompilerOptions.OPTION_Source, "1.6");
        javaProject.setOption(CompilerOptions.OPTION_Compliance, "1.6");
        javaProject.setOption(CompilerOptions.OPTION_TargetPlatform, "1.6");
    }

    public IProject getProject() {
        return project;
    }

    public IJavaProject getJavaProject() {
        return javaProject;
    }

    public IPackageFragmentRoot getSourceFolder() {
        return sourceFolder;
    }

    public boolean hasGroovyLibraries() throws Exception {
        return GroovyRuntime.hasGroovyClasspathContainer(javaProject);
    }

    /** Adds base Java nature and classpath entries to project. */
    private void prepareForJava() throws Exception {
        addNature(JavaCore.NATURE_ID);

        javaProject.setRawClasspath(new IClasspathEntry[] {JavaRuntime.getDefaultJREContainerEntry()}, null);
    }

    /** Adds base Groovy nature and classpath entries to project. */
    private void prepareForGroovy() throws Exception {
        addNature(GroovyNature.GROOVY_NATURE);

        if (!GroovyRuntime.hasGroovyClasspathContainer(javaProject)) {
            GroovyRuntime.addGroovyClasspathContainer(javaProject);
        }
    }

    public void addBuilder(String newBuilder) throws Exception {
        ICommand buildCommand = new BuildCommand();
        buildCommand.setBuilderName(newBuilder);

        final IProjectDescription description = project.getDescription();
        description.setBuildSpec((ICommand[]) ArrayUtils.add(description.getBuildSpec(), 0, buildCommand));
        project.setDescription(description, null);
    }

    public void addNature(String natureId) throws Exception {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds((String[]) ArrayUtils.add(description.getNatureIds(), 0, natureId));
        project.setDescription(description, null);
    }

    public void removeNature(String natureId) throws Exception {
        final IProjectDescription description = project.getDescription();
        final String[] ids = description.getNatureIds();
        for (int i = 0, n = ids.length; i < n; i += 1) {
            if (ids[i].equals(natureId)) {
                description.setNatureIds((String[]) ArrayUtils.remove(ids, i));
                project.setDescription(description, null);
                return;
            }
        }
    }

    public void addClasspathEntry(IClasspathEntry entry) throws Exception {
        if (!GroovyRuntime.findClasspathEntry(javaProject, entry::equals).isPresent()) {
            GroovyRuntime.appendClasspathEntry(javaProject, entry);
        }
    }

    public void addExternalLibrary(IPath libraryPath) throws Exception {
        addClasspathEntry(JavaCore.newLibraryEntry(libraryPath, null, null));
    }

    public void addProjectReference(IJavaProject referent) throws Exception {
        addClasspathEntry(JavaCore.newProjectEntry(referent.getPath()));
    }

    public IPackageFragment createPackage(String name) throws Exception {
        return sourceFolder.createPackageFragment(name, true, null);
    }

    public void deletePackage(String name) throws CoreException {
        sourceFolder.getPackageFragment(name).delete(true, null);
    }

    private void appendPackage(StringBuilder b, IPackageFragment p, CharSequence source) {
        if (!p.isDefaultPackage() && (source.length() < 8 || !source.subSequence(0, 7).toString().equals("package"))) {
            b.append("package ");
            b.append(p.getElementName());
            b.append(";\n\n");
        }
    }

    public ICompilationUnit createJavaType(IPackageFragment packageFrag, String fileName, CharSequence source) throws Exception {
        StringBuilder buf = new StringBuilder();
        appendPackage(buf, packageFrag, source);
        buf.append(source);

        ICompilationUnit unit = packageFrag.createCompilationUnit(fileName, buf.toString(), false, null);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    public ICompilationUnit createJavaTypeAndPackage(String packageName, String fileName, CharSequence source) throws Exception {
        return createJavaType(createPackage(packageName), fileName, source);
    }

    public ICompilationUnit createGroovyTypeAndPackage(String packageName, String fileName, InputStream source) throws Exception {
        return createGroovyType(createPackage(packageName), fileName, IOGroovyMethods.getText(source));
    }

    public ICompilationUnit createGroovyTypeAndPackage(String packageName, String fileName, CharSequence source) throws Exception {
        return createGroovyType(createPackage(packageName), fileName, source);
    }

    public ICompilationUnit createGroovyType(IPackageFragment packageFrag, String fileName, CharSequence source) throws Exception {
        StringBuilder buf = new StringBuilder();
        appendPackage(buf, packageFrag, source);
        buf.append(source);

        ICompilationUnit unit = packageFrag.createCompilationUnit(fileName, buf.toString(), false, null);
        unit.becomeWorkingCopy(null);
        return unit;
    }

    public void dispose() throws Exception {
        deleteWorkingCopies();
        Util.delete(project);
    }

    public void deleteContents() throws Exception {
        deleteWorkingCopies();
        for (IPackageFragment frag : javaProject.getPackageFragments()) {
            if (!frag.isReadOnly()) {
                frag.delete(true, null);
            }
        }
    }

    private void deleteWorkingCopies() throws Exception {
        SynchronizationUtils.joinBackgroundActivities();

        ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
        if (workingCopies != null && workingCopies.length > 0) {
            for (ICompilationUnit workingCopy : workingCopies) {
                if (workingCopy.isWorkingCopy()) {
                    workingCopy.discardWorkingCopy();
                }
            }
        }
    }

    private IFolder createBinFolder() throws Exception {
        final IFolder binFolder = project.getFolder("bin");
        if (!binFolder.exists()) {
            ensureExists(binFolder);
        }
        return binFolder;
    }

    private void createOutputFolder(IFolder binFolder) throws Exception {
        IPath outputLocation = binFolder.getFullPath();
        javaProject.setOutputLocation(outputLocation, null);
    }

    private IPackageFragmentRoot createSourceFolder() throws Exception {
        return createSourceFolder("src", null, (IPath[]) null);
    }

    public IPackageFragmentRoot createSourceFolder(String path, String outPath, IPath... exclusionPatterns) throws Exception {
        IFolder folder = project.getFolder(path);
        if (!folder.exists()) {
            ensureExists(folder);
        }
        IClasspathEntry sourceFolderEntry = null;
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            if (entry.getPath().equals(folder.getFullPath())) {
                sourceFolderEntry = entry;
                break;
            }
        }
        if (sourceFolderEntry == null) {
            IPath outPathPath = (outPath == null ? null : getProject().getFullPath().append(outPath).makeAbsolute());
            sourceFolderEntry = JavaCore.newSourceEntry(folder.getFullPath(), exclusionPatterns, outPathPath);
            addClasspathEntry(sourceFolderEntry);
        }
        return javaProject.findPackageFragmentRoots(sourceFolderEntry)[0];
    }

    private void ensureExists(IFolder folder) throws Exception {
        if (folder.getParent().getType() == IResource.FOLDER && !folder.getParent().exists()) {
            ensureExists((IFolder) folder.getParent());
        }
        folder.create(false, true, null);
    }

    public void waitForIndexer() {
        SynchronizationUtils.waitForIndexingToComplete(getJavaProject());
    }

    public void fullBuild() throws Exception {
        SimpleProgressMonitor spm = new SimpleProgressMonitor("full build of " + getProject().getName());
        getProject().build(org.eclipse.core.resources.IncrementalProjectBuilder.FULL_BUILD, spm);
        spm.waitForCompletion();
    }

    public String getProblems() throws Exception {
        IMarker[] markers = getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
        if (markers == null || markers.length == 0) {
            return null;
        }
        boolean errorFound = false;
        StringBuilder sb = new StringBuilder("Problems:\n");
        for (IMarker marker : markers) {
            if (((Number) marker.getAttribute(IMarker.SEVERITY)).intValue() == IMarker.SEVERITY_ERROR) {
                errorFound = true;

                sb.append("  ");
                sb.append(marker.getResource().getName());
                sb.append(" : ");
                sb.append(marker.getAttribute(IMarker.LOCATION));
                sb.append(" : ");
                sb.append(marker.getAttribute(IMarker.MESSAGE));
                sb.append("\n");
            }
        }
        return errorFound ? sb.toString() : null;
    }

    public IFile createFile(String name, String contents) throws Exception {
        String encoding = null;
        try {
            encoding = project.getDefaultCharset(); // get project encoding as file is not accessible
        } catch (CoreException ce) {
            // use no encoding
        }
        InputStream stream = new ByteArrayInputStream(encoding == null ? contents.getBytes() : contents.getBytes(encoding));
        IFile file = project.getFolder("src").getFile(new Path(name));
        if (!file.getParent().exists()) {
            createFolder(file.getParent());
        }
        file.create(stream, true, null);
        return file;
    }

    private void createFolder(IContainer parent) throws Exception {
        if (!parent.getParent().exists()) {
            assertEquals("Project doesn't exist " + parent.getParent(), parent.getParent().getType(), IResource.FOLDER);
            createFolder(parent.getParent());
        }
        ((IFolder) parent).create(true, true, null);
    }

    public ICompilationUnit[] createUnits(String[] packages, String[] cuNames, String[] cuContents) throws Exception {
        ICompilationUnit[] units = new ICompilationUnit[packages.length];
        for (int i = 0, n = cuContents.length; i < n; i += 1) {
            units[i] = createPackage(packages[i]).createCompilationUnit(cuNames[i], cuContents[i], false, null);
        }
        return units;
    }

    public static void setAutoBuilding(boolean value) {
        try {
            IWorkspace w = ResourcesPlugin.getWorkspace();
            IWorkspaceDescription d = w.getDescription();
            d.setAutoBuilding(value);
            w.setDescription(d);
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isAutoBuilding() {
        IWorkspace w = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription d = w.getDescription();
        return d.isAutoBuilding();
    }
}
